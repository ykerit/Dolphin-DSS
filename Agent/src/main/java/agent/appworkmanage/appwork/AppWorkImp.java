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
import common.struct.AppWorkExitStatus;
import common.struct.AppWorkId;
import common.struct.AppWorkStatus;
import common.struct.Priority;
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
import java.util.Set;
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
    private boolean isPaused;
    private long appWorkLocalizationStartTime;
    private long appWorkLaunchStartTime;
    private AppWorkExecType type;

    private final Context context;
    private final Configuration configuration;
    private final long startTime;

    private volatile boolean isMarkeForKilling = false;

    private Path workspace;

    private final UntypedStateMachineBuilder appWorkStateMachineBuilder;
    private final UntypedStateMachine appWorkStateMachine;

    public AppWorkImp(Configuration configuration,
                      EventDispatcher dispatcher,
                      AppWorkLaunchContext launchContext,
                      Context context,
                      long startTime,
                      String user,
                      AppWorkId appWorkId) {
        this.startTime = startTime;
        this.configuration = configuration;
        this.dispatcher = dispatcher;
        this.launchContext = launchContext;
        this.appWorkId = appWorkId;

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
        return null;
    }

    @Override
    public void setAppWorkId(AppWorkId appWorkId) {

    }

    @Override
    public String getAppWorkStartTime() {
        return null;
    }

    @Override
    public String getAppWorkLaunchedTime() {
        return null;
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public void setResource(Resource resource) {

    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return launchContext;
    }

    @Override
    public AppWorkState getAppWorkState() {
        return null;
    }

    @Override
    public Path getWorkDir() {
        return null;
    }

    @Override
    public void setWorkDir() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public Priority getPriority() {
        return null;
    }

    @Override
    public void setPriority() {

    }

    @Override
    public Set<String> getAllocationTags() {
        return null;
    }

    @Override
    public void setAllocationTags(Set<String> allocationTags) {

    }

    @Override
    public AppWorkExecType getExecType() {
        return type;
    }

    @Override
    public void setIsReInitializing(boolean isReInitializing) {

    }

    @Override
    public boolean isReInitializing() {
        return false;
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

    private void sendFinishedEvents() {
        EventProcessor processor = dispatcher.getEventProcessor();
        AppWorkStatus appWorkStatus = cloneAndGetAppWorkStatus();
        processor.process(new ApplicationAppWorkFinishedEvent(appWorkStatus, startTime));

        // remove AppWork form the resource-monitor
    }

    @Override
    public boolean isRecovering() {
        return false;
    }

    @Override
    public AppWorkStatus cloneAndGetAppWorkStatus() {
        return null;
    }

    @Override
    public Map<Path, List<String>> getLocalizeResource() {
        return null;
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

    @StateMachineParameters(stateType = AppWorkState.class, eventType = AppWorkEventType.class, contextType = AppWorkEvent.class)
    class AppWorkStateMachine extends AbstractUntypedStateMachine {

        protected void toLocalizing(AppWorkState from, AppWorkState to, AppWorkEventType type, AppWorkEvent event) {
            final AppWorkLaunchContext launchContext = AppWorkImp.this.launchContext;
            AppWorkImp.this.appWorkLocalizationStartTime = System.currentTimeMillis();

        }

        protected void toLocalizationFailed(AppWorkState from, AppWorkState to, AppWorkEventType type, AppWorkEvent event) {
            log.warn("Failed to parse resoure request");
            // AppWork Localization cleanup

        }

        protected void toKillOnNew(AppWorkState from, AppWorkState to, AppWorkEventType type, AppWorkEvent event) {
            AppWorkKillEvent killEvent = (AppWorkKillEvent) event;
            AppWorkImp.this.exitCode = killEvent.getExitStatus();
            AppWorkImp.this.addTips(killEvent.getDescription() + "\n");
            AppWorkImp.this.addTips("AppWork is killed before being launched.\n");
        }

        protected void toLocalized(AppWorkState from, AppWorkState to, AppWorkEventType type, AppWorkEvent event) {
            AppWorkResourceLocalizedEvent resourceLocalizedEvent = (AppWorkResourceLocalizedEvent) event;
            LocalResourceRequest resourceRequest = resourceLocalizedEvent.getResourceRequest();
            Path location = resourceLocalizedEvent.getLocation();

        }
    }
}
