package agent.appworkmanage.appwork;

import agent.Context;
import agent.appworkmanage.Localize.LocalResourceRequest;
import agent.appworkmanage.application.ApplicationAppWorkFinishedEvent;
import agent.appworkmanage.launcher.AppWorkLauncherPoolEvent;
import agent.appworkmanage.launcher.AppWorkLauncherPoolEventType;
import common.context.AppWorkLaunchContext;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.resource.Resource;
import common.resource.Resources;
import common.struct.*;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* In the future, will support task interrupt and resume
 ** the status represents future;
 */

public class AppWorkImp implements AppWork {

    private static final Logger log = LogManager.getLogger(AppWorkImp.class.getName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EventDispatcher dispatcher;
    private final Lock readLock;
    private final Lock writeLock;
    private volatile AppWorkLaunchContext launchContext;
    private final AppWorkId appWorkId;
    private final String user;
    private int exitCode = AppWorkExitStatus.INVALID;
    private final StringBuilder tips;
    private boolean isLaunched;
    private long appWorkLocalizationStartTime;
    private long appWorkLaunchStartTime;
    private RemoteAppWork remoteAppWork;

    private final Context context;
    private final Configuration configuration;
    private final long startTime;

    private volatile boolean isMarkeForKilling = false;
    private volatile boolean isReInitializing = false;

    private String workspace;

    private final UntypedStateMachineBuilder appWorkStateMachineBuilder;
    private final UntypedStateMachine appWorkStateMachine;

    public AppWorkImp(Configuration configuration,
                      EventDispatcher dispatcher,
                      AppWorkLaunchContext launchContext,
                      RemoteAppWork remoteAppWork,
                      Context context,
                      long startTime,
                      String user) {
        this.startTime = startTime;
        this.configuration = configuration;
        this.dispatcher = dispatcher;
        this.launchContext = launchContext;
        this.remoteAppWork = remoteAppWork;

        this.appWorkId = this.remoteAppWork.getAppWorkId();

        this.user = user;
        this.tips = new StringBuilder();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.context = context;

        appWorkStateMachineBuilder = StateMachineBuilderFactory.create(AppWorkStateMachine.class);

        appWorkStateMachineBuilder.externalTransitions()
                .from(AppWorkState.NEW)
                .toAmong(AppWorkState.LOCALIZING, AppWorkState.LOCALIZATION_FAILED, AppWorkState.DONE)
                .onEach(AppWorkEventType.INIT_APP_WORK, AppWorkEventType.APP_WORK_LOCALIZING_FAILED, AppWorkEventType.KILL_APP_WORK)
                .callMethod("toLocalizing|toLocalizationFailed|toKillOnNew");

        appWorkStateMachineBuilder.externalTransitions()
                .from(AppWorkState.LOCALIZING)
                .toAmong(AppWorkState.LOCALIZED, AppWorkState.KILLING, AppWorkState.LOCALIZATION_FAILED)
                .onEach(AppWorkEventType.RESOURCE_LOCALIZED, AppWorkEventType.KILL_APP_WORK, AppWorkEventType.RESOURCE_FAILED);

        appWorkStateMachineBuilder.externalTransitions()
                .from(AppWorkState.LOCALIZED)
                .toAmong(AppWorkState.RUNNING, AppWorkState.KILLING, AppWorkState.EXITED_WITH_FAILURE)
                .onEach(AppWorkEventType.APP_WORK_LAUNCHED, AppWorkEventType.KILL_APP_WORK, AppWorkEventType.APP_WORK_EXIT_WITH_FAILURE);

        appWorkStateMachineBuilder.externalTransitions()
                .from(AppWorkState.RUNNING)
                .toAmong(AppWorkState.EXITED_WITH_SUCCESS, AppWorkState.KILLING, AppWorkState.EXITED_WITH_FAILURE)
                .onEach(AppWorkEventType.APP_WORK_EXIT_WITH_SUCCESS, AppWorkEventType.KILL_APP_WORK, AppWorkEventType.APP_WORK_EXIT_WITH_FAILURE);

        appWorkStateMachineBuilder.externalTransitions()
                .from(AppWorkState.KILLING)
                .toAmong(AppWorkState.APP_WORK_CLEANUP_AFTER_KILL, AppWorkState.EXITED_WITH_SUCCESS, AppWorkState.EXITED_WITH_FAILURE)
                .onEach(AppWorkEventType.APP_WORK_EXIT_KILLED, AppWorkEventType.APP_WORK_EXIT_WITH_SUCCESS, AppWorkEventType.APP_WORK_EXIT_WITH_FAILURE);

        appWorkStateMachineBuilder.externalTransition()
                .from(AppWorkState.EXITED_WITH_SUCCESS)
                .to(AppWorkState.DONE)
                .on(AppWorkEventType.APP_WORK_RESOURCE_CLEANUP);

        appWorkStateMachineBuilder.externalTransition()
                .from(AppWorkState.EXITED_WITH_FAILURE)
                .to(AppWorkState.DONE)
                .on(AppWorkEventType.APP_WORK_RESOURCE_CLEANUP);

        appWorkStateMachineBuilder.externalTransition()
                .from(AppWorkState.LOCALIZATION_FAILED)
                .to(AppWorkState.DONE)
                .on(AppWorkEventType.APP_WORK_RESOURCE_CLEANUP);

        appWorkStateMachineBuilder.externalTransition()
                .from(AppWorkState.APP_WORK_CLEANUP_AFTER_KILL)
                .to(AppWorkState.DONE)
                .on(AppWorkEventType.APP_WORK_RESOURCE_CLEANUP);

        appWorkStateMachineBuilder.externalTransition()
                .from(AppWorkState.NEW)
                .to(AppWorkState.DONE)
                .on(AppWorkEventType.KILL_APP_WORK);

        appWorkStateMachine = appWorkStateMachineBuilder.newStateMachine(AppWorkState.NEW);
        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        appWorkStateMachine.accept(visitor);
        visitor.convertDotFile("/Users/yuankai/AppWorkStateMachine");
    }


    @Override
    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    @Override
    public long getAppWorkStartTime() {
        return this.startTime;
    }

    @Override
    public long getAppWorkLaunchedTime() {
        return this.appWorkLaunchStartTime;
    }

    @Override
    public Resource getResource() {
        return Resources.clone(remoteAppWork.getResource());
    }

    @Override
    public String getUser() {
        this.readLock.lock();
        try {
            return this.user;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return launchContext;
    }

    @Override
    public AppWorkState getAppWorkState() {
        readLock.lock();
        try {
            return (AppWorkState) appWorkStateMachine.getCurrentState();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AgentAppWorkStatus getAgentAppWorkStatus() {
        readLock.lock();
        try {
            AgentAppWorkStatus appWorkStatus = new AgentAppWorkStatus(appWorkId,
                    getCurrentState(),
                    getResource(),
                    tips.toString(),
                    exitCode,
                    getPriority(),
                    startTime);
            return appWorkStatus;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getWorkDir() {
        return workspace;
    }

    @Override
    public void setWorkDir(String workDir) {
        this.workspace = workDir;
    }

    @Override
    public boolean isRunning() {
        return getAppWorkState() == AppWorkState.RUNNING;
    }

    @Override
    public Priority getPriority() {
        return remoteAppWork.getPriority();
    }

    @Override
    public void setIsReInitializing(boolean isReInitializing) {
        if (this.isReInitializing && !isReInitializing) {

        }
        this.isReInitializing = isReInitializing;
    }

    @Override
    public boolean isReInitializing() {
        return isReInitializing;
    }

    @Override
    public boolean isMarkedForKilling() {
        return isMarkeForKilling;
    }

    @Override
    public void sendLaunchEvent() {
        AppWorkLauncherPoolEventType launcherEvent = AppWorkLauncherPoolEventType.LAUNCHER_APP_WORK;
        appWorkLaunchStartTime = System.currentTimeMillis();
        dispatcher.getEventProcessor().process(new AppWorkLauncherPoolEvent(this, launcherEvent));
    }

    @Override
    public void sendKillEvent(int exitStatus, String description) {
        this.isMarkeForKilling = true;
        dispatcher.getEventProcessor().process(new AppWorkKillEvent(appWorkId, exitStatus, description));
    }

    @Override
    public boolean isAppWorkInFinalStates() {
        AppWorkState state = getAppWorkState();
        return state == AppWorkState.KILLING || state == AppWorkState.DONE
                || state == AppWorkState.LOCALIZATION_FAILED
                || state == AppWorkState.APP_WORK_CLEANUP_AFTER_KILL
                || state == AppWorkState.EXITED_WITH_FAILURE
                || state == AppWorkState.EXITED_WITH_SUCCESS;
    }

    private void sendFinishedEvents() {
        EventProcessor processor = dispatcher.getEventProcessor();
        AppWorkStatus appWorkStatus = cloneAndGetAppWorkStatus();
        processor.process(new ApplicationAppWorkFinishedEvent(appWorkStatus, startTime));

        // remove AppWork form the resource-monitor
    }


    @Override
    public AppWorkStatus cloneAndGetAppWorkStatus() {
        return null;
    }

    @Override
    public Map<Path, List<String>> getLocalizeResource() {
        return null;
    }

    public RemoteAppWorkState getCurrentState() {
        switch (getAppWorkState()) {
            case NEW:
            case LOCALIZING:
            case LOCALIZATION_FAILED:
            case RUNNING:
            case RELAUNCHING:
            case REINITIALIZING:
            case REINITIALIZING_AWAITING_KILL:
            case EXITED_WITH_FAILURE:
            case EXITED_WITH_SUCCESS:
            case KILLING:
            case APP_WORK_CLEANUP_AFTER_KILL:
                return RemoteAppWorkState.RUNNING;
            case DONE:
            default:
                return RemoteAppWorkState.COMPLETE;
        }
    }

    private void addTips(String... tips) {
        for (String s : tips) {
            this.tips.append("[" + dateFormat.format(new Date()) + "]" + s);
        }
    }

    @Override
    public void process(AppWorkEvent event) {
        writeLock.lock();
        try {
            appWorkStateMachine.fire(event.getType(), event);
        } finally {
            writeLock.unlock();
        }
    }

    @StateMachineParameters(stateType = AppWorkState.class, eventType = AppWorkEventType.class, contextType = Channel.class)
    static class AppWorkStateMachine extends AbstractUntypedStateMachine {

        protected void toLocalizing(AppWorkState from, AppWorkState to, AppWorkEventType type, Channel ch) {
            final AppWorkLaunchContext launchContext = ch.appWork.launchContext;
            ch.appWork.appWorkLocalizationStartTime = System.currentTimeMillis();

        }

        protected void toLocalizationFailed(AppWorkState from, AppWorkState to, AppWorkEventType type, Channel ch) {
            log.warn("Failed to parse resoure request");
            // AppWork Localization cleanup

        }

        protected void toKillOnNew(AppWorkState from, AppWorkState to, AppWorkEventType type, Channel ch) {
            AppWorkKillEvent killEvent = (AppWorkKillEvent) ch.event;
            ch.appWork.exitCode = killEvent.getExitStatus();
            ch.appWork.addTips(killEvent.getDescription() + "\n");
            ch.appWork.addTips("AppWork is killed before being launched.\n");
        }

        protected void toLocalized(AppWorkState from, AppWorkState to, AppWorkEventType type, Channel ch) {
            AppWorkResourceLocalizedEvent resourceLocalizedEvent = (AppWorkResourceLocalizedEvent) ch.event;
            LocalResourceRequest resourceRequest = resourceLocalizedEvent.getResourceRequest();
            Path location = resourceLocalizedEvent.getLocation();

        }
    }

    class Channel {
        final AppWorkImp appWork;
        final AppWorkEvent event;

        public Channel(AppWorkImp appWork, AppWorkEvent event) {
            this.appWork = appWork;
            this.event = event;
        }
    }
}
