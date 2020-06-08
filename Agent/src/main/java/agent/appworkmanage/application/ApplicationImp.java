package agent.appworkmanage.application;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkInitEvent;
import agent.appworkmanage.appwork.AppWorkKillEvent;
import agent.appworkmanage.appwork.AppWorkState;
import common.event.EventDispatcher;
import common.struct.AppWorkExitStatus;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationImp implements Application {

    private static final Logger log = LogManager.getLogger(ApplicationImp.class);

    private final EventDispatcher dispatcher;
    private final String user;
    private final ApplicationId applicationId;
    private final Lock readLock;
    private final Lock writeLock;
    private final Context context;

    Map<AppWorkId, AppWork> appWorks = new ConcurrentHashMap<>();

    private final UntypedStateMachineBuilder appStateMachineBuilder;
    private final UntypedStateMachine appStateMachine;

    public ApplicationImp(EventDispatcher dispatcher,
                          String user,
                          ApplicationId applicationId,
                          Context context) {
        this.dispatcher = dispatcher;
        this.user = user;
        this.applicationId = applicationId;
        this.context = context;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();

        appStateMachineBuilder = StateMachineBuilderFactory.create(ApplicationImp.AppStateMachine.class);

        appStateMachineBuilder.externalTransition()
                .from(ApplicationState.NEW)
                .to(ApplicationState.NEW)
                .on(ApplicationEventType.INIT_APP_WORK)
                .callMethod("initAppWork");

        appStateMachineBuilder.externalTransition()
                .from(ApplicationState.NEW)
                .to(ApplicationState.INITING)
                .on(ApplicationEventType.INIT_APPLICATION);

        appStateMachineBuilder.externalTransitions()
                .from(ApplicationState.INITING)
                .toAmong(ApplicationState.APPLICATION_RESOURCES_CLEANINGUP,
                        ApplicationState.FINISHING_APP_WORKS_WAIT,
                        ApplicationState.RUNNING)
                .onEach(ApplicationEventType.FINISH_APPLICATION_NONE, ApplicationEventType.FINISH_APPLICATION, ApplicationEventType.APPLICATION_INITED)
                .callMethod("appFinishNone|appFinish|appInitDone");

        appStateMachineBuilder.externalTransitions()
                .from(ApplicationState.RUNNING)
                .toAmong(ApplicationState.FINISHING_APP_WORKS_WAIT,
                        ApplicationState.APPLICATION_RESOURCES_CLEANINGUP)
                .onEach(ApplicationEventType.FINISH_APPLICATION_NONE, ApplicationEventType.FINISH_APPLICATION)
                .callMethod("appFinishNone|appFinish");

        appStateMachineBuilder.externalTransition()
                .from(ApplicationState.FINISHING_APP_WORKS_WAIT)
                .to(ApplicationState.APPLICATION_RESOURCES_CLEANINGUP)
                .on(ApplicationEventType.APPLICATION_APP_WORK_FINISHED);

        appStateMachineBuilder.externalTransition()
                .from(ApplicationState.APPLICATION_RESOURCES_CLEANINGUP)
                .to(ApplicationState.FINISHED)
                .on(ApplicationEventType.APPLICATION_RESOURCES_CLEANEDUP);

        appStateMachine = appStateMachineBuilder.newStateMachine(AppWorkState.NEW);
        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        appStateMachine.accept(visitor);
        visitor.convertDotFile("/Users/yuankai/AgentAppStateMachine");
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public ApplicationState getAppState() {
        readLock.lock();
        try {
            return (ApplicationState) this.appStateMachine.getCurrentState();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Override
    public Map<AppWorkId, AppWork> getAppWorks() {
        readLock.lock();
        try {
            return this.appWorks;
        } finally {
            readLock.unlock();
        }
    }

    private void handleAppFinishWithAppWorksCleanup() {

    }

    @Override
    public void process(ApplicationEvent event) {
        writeLock.lock();
        try {
            ApplicationId applicationId = event.getApplicationId();
            log.debug("Processing {} of type {}", applicationId, event.getType());
            appStateMachine.fire(event.getType(), event);
        } finally {
            writeLock.unlock();
        }
    }

    @StateMachineParameters(stateType = ApplicationState.class, eventType = ApplicationEventType.class, contextType = ApplicationEvent.class)
    class AppStateMachine extends AbstractUntypedStateMachine {

        protected void initAppWork(ApplicationState from, ApplicationState to, ApplicationEventType type, ApplicationEvent event) {
            ApplicationAppWorkInitEvent initEvent = (ApplicationAppWorkInitEvent) event;
            AppWork appWork = initEvent.getAppWork();
            ApplicationImp.this.appWorks.put(appWork.getAppWorkId(), appWork);
            log.info("Adding " + appWork.getAppWorkId() + " to application " + ApplicationImp.this.toString());
        }

        protected void appFinishNone(ApplicationState from, ApplicationState to, ApplicationEventType type, ApplicationEvent event) {
            ApplicationFinishedEvent finishedEvent = (ApplicationFinishedEvent) event;
            if (ApplicationImp.this.appWorks.isEmpty()) {
                ApplicationImp.this.handleAppFinishWithAppWorksCleanup();
            }
        }

        protected void appFinish(ApplicationState from, ApplicationState to, ApplicationEventType type, ApplicationEvent event) {
            ApplicationFinishedEvent finishedEvent = (ApplicationFinishedEvent) event;
            if (ApplicationImp.this.appWorks.isEmpty()) {
                return;
            }
            for (AppWorkId appWorkId : ApplicationImp.this.appWorks.keySet()) {
                ApplicationImp.this.dispatcher.getEventProcessor()
                        .process(new AppWorkKillEvent(appWorkId, AppWorkExitStatus.KILLED_AFTER_APP_COMPLETION,
                                "AppWork killed on application finish event " + finishedEvent.getTips()));
            }
        }

        protected void appInitDone(ApplicationState from, ApplicationState to, ApplicationEventType type, ApplicationEvent event) {
            for (AppWork appWork : ApplicationImp.this.appWorks.values()) {
                ApplicationImp.this.dispatcher.getEventProcessor().process(new AppWorkInitEvent(appWork.getAppWorkId()));
            }
        }
    }
}
