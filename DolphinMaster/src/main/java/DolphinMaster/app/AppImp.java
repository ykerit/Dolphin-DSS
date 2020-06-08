package DolphinMaster.app;

import DolphinMaster.DolphinContext;
import DolphinMaster.amlauncher.AMLauncherEvent;
import DolphinMaster.amlauncher.AMLauncherEventType;
import DolphinMaster.scheduler.Allocation;
import DolphinMaster.scheduler.PoolInfo;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.scheduler.event.AppAddedSchedulerEvent;
import DolphinMaster.schedulerunit.SchedulerUnitImp;
import api.app_master_message.ResourceRequest;
import common.context.ApplicationSubmission;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.struct.*;
import common.util.SystemClock;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AppImp implements App {

    private static final Logger log = LogManager.getLogger(AppImp.class);
    private static final EnumSet<AppState> COMPLETE_APP_STATES =
            EnumSet.of(AppState.FINISHED, AppState.FINISHING, AppState.FAILED, AppState.KILLED, AppState.KILLING);

    private static final String STATE_CHANGE_MESSAGE =
            "%s state change form %s tp %s on event = %s";

    public final static Priority AM_APP_WORK_PRIORITY = Priority.newInstance(0);

    private final ApplicationId applicationId;
    private final DolphinContext context;
    private final Configuration configuration;
    private final String user;
    private final String name;
    private final ApplicationSubmission submission;
    private final ResourceScheduler scheduler;
    private final EventDispatcher dispatcher;
    private final StringBuilder tips = new StringBuilder();
    private final String applicationType;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final long submitTime;
    private final Set<String> applicationTags;
    private final List<ResourceRequest> amReqs;
    private Map<String, String> applicationEnv = new HashMap<>();
    private volatile RemoteAppWork masterAppWork;

    private SystemClock clock;

    private long startTime;
    private long launchTime = 0;
    private long finishTime = 0;
    private long scheduledTime = 0;
    private long appWorkAllocatedTime = 0;
    private long launchAMStartTime = 0;
    private long launchAMEndTime = 0;

    private String poolName;
    private Set<AgentId> runNodes = new ConcurrentSkipListSet<>();
    private Priority applicationPriority;
    private EventProcessor processor;
    private AppState state;
    private final UntypedStateMachineBuilder appStateMachineBuilder;
    private final UntypedStateMachine appStateMachine;

    public AppImp(ApplicationId applicationId,
                  DolphinContext context,
                  Configuration configuration,
                  String name,
                  String user,
                  String pool,
                  ApplicationSubmission submission,
                  long submitTime,
                  String applicationType,
                  long startTime,
                  Set<String> applicationTags,
                  List<ResourceRequest> amReqs) {
        this.applicationId = applicationId;
        this.context = context;
        this.configuration = configuration;
        this.name = name;
        this.poolName = pool;
        this.user = user;
        this.submission = submission;
        this.submitTime = submitTime;
        this.amReqs = amReqs;
        this.applicationType = applicationType;
        this.dispatcher = context.getDolphinDispatcher();
        this.processor = dispatcher.getEventProcessor();
        this.scheduler = context.getScheduler();
        this.applicationTags = applicationTags;
        this.clock = SystemClock.getInstance();

        if (startTime <= 0) {
            this.startTime = this.clock.getTime();
        } else {
            this.startTime = startTime;
        }
        if (submission.getPriority() != null) {
            this.applicationPriority = Priority.newInstance(submission.getPriority().getPriority());
        } else {
            this.applicationPriority = Priority.newInstance(0);
        }
        if (submission.getAppMasterSpec() != null) {
            applicationEnv.putAll(submission.getAppMasterSpec().getEnvironment());
        }
        appStateMachineBuilder = StateMachineBuilderFactory.create(AppStateMachine.class);

        appStateMachineBuilder.externalTransition()
                .from(AppState.NEW)
                .to(AppState.SUBMITTED)
                .on(AppEventType.START);

        appStateMachineBuilder.externalTransition()
                .from(AppState.SUBMITTED)
                .to(AppState.ACCEPTED)
                .on(AppEventType.APP_ACCEPTED);

        appStateMachineBuilder.externalTransition()
                .from(AppState.ACCEPTED)
                .to(AppState.SCHEDULED)
                .on(AppEventType.SCHEDULED);

        appStateMachineBuilder.externalTransition()
                .from(AppState.SCHEDULED)
                .to(AppState.ALLOCATED)
                .on(AppEventType.APP_WORK_ALLOCATE);

        appStateMachineBuilder.externalTransition()
                .from(AppState.ALLOCATED)
                .to(AppState.LAUNCHED)
                .on(AppEventType.LAUNCHED)
                .callMethod("launchAM");

        appStateMachineBuilder.externalTransition()
                .from(AppState.LAUNCHED)
                .to(AppState.RUNNING)
                .on(AppEventType.AM_REGISTER);

        appStateMachineBuilder.externalTransition()
                .from(AppState.ACCEPTED)
                .to(AppState.KILLED)
                .on(AppEventType.KILL);

        appStateMachineBuilder.externalTransition()
                .from(AppState.ACCEPTED)
                .to(AppState.FAILED)
                .on(AppEventType.APP_REJECTED);

        appStateMachineBuilder.externalTransition()
                .from(AppState.RUNNING)
                .to(AppState.KILLED)
                .on(AppEventType.KILL);

        appStateMachineBuilder.externalTransition()
                .from(AppState.RUNNING)
                .to(AppState.FAILED)
                .on(AppEventType.APP_REJECTED);

        appStateMachineBuilder.externalTransition()
                .from(AppState.RUNNING)
                .to(AppState.FINISHING)
                .on(AppEventType.APP_WORK_FINISHED);

        appStateMachineBuilder.onEntry(AppState.SUBMITTED)
                .callMethod("addApplicationToScheduler");

        appStateMachineBuilder.onEntry(AppState.SCHEDULED)
                .callMethod("amScheduler");

        appStateMachineBuilder.onEntry(AppState.ALLOCATED)
                .callMethod("amAppWorkAllocation");

        appStateMachine = appStateMachineBuilder.newStateMachine(AppState.NEW);
        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        appStateMachine.accept(visitor);
        visitor.convertDotFile("/Users/yuankai/AppStateMachine");
    }

    @Override
    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Override
    public ApplicationSubmission getApplicationSubmission() {
        return submission;
    }

    @Override
    public AppState getAppState() {
        readLock.lock();
        try {
            return (AppState) appStateMachine.getCurrentState();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public String getPool() {
        return poolName;
    }

    @Override
    public void setPool(String name) {
        this.poolName = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ApplicationReport createAndGetApplicationReport(String user) {
        return null;
    }

    @Override
    public long getFinishTime() {
        this.readLock.lock();
        try {
            return finishTime;
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public long getStartTime() {
        this.readLock.lock();
        try {
            return startTime;
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public long getSubmitTime() {
        return submitTime;
    }

    @Override
    public long getLaunchTime() {
        readLock.lock();
        try {
            return launchTime;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public StringBuilder getTips() {
        this.readLock.lock();
        try {
            if (tips.length() == 0) {

            }
            return tips;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public Set<String> getApplicationTags() {
        return applicationTags;
    }

    @Override
    public Set<AgentId> getRunNodes() {
        return runNodes;
    }

    @Override
    public Priority getApplicationPriority() {
        return applicationPriority;
    }

    @Override
    public boolean isAppInCompletedStates() {
        final AppState state = getAppState();
        return state == AppState.FINISHED || state == AppState.FINISHING
                || state == AppState.FAILED || state == AppState.KILLED
                || state == AppState.KILLING;
    }

    @Override
    public Map<String, String> getApplicationEnvs() {
        return applicationEnv;
    }

    @Override
    public void process(AppEvent event) {
        writeLock.lock();
        try {
            ApplicationId appId = event.getAppId();
            log.debug("Process event for {} of type {}", appId, event.getType());
            appStateMachine.fire(event.getType(), this);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public RemoteAppWork getMasterAppWork() {
        return masterAppWork;
    }

    @Override
    public void setMasterAppWork(RemoteAppWork masterAppWork) {
        this.masterAppWork = masterAppWork;
    }

    private void launchAppMaster() {
        launchAMStartTime = System.currentTimeMillis();
        processor.process(new AMLauncherEvent(AMLauncherEventType.LAUNCH, this));
    }

    private void appMasterLaunched() {
        context.getAMLiveLinessMonitor().addMonitored(applicationId);
    }

    private static final List<ResourceRequest> EMPTY_APP_WORK_REQUESTS = new ArrayList<>();
    private static final List<AppWorkId> EMPTY_APP_WORK_RELEASES = new ArrayList<>();

    @StateMachineParameters(stateType = AppState.class, eventType = AppEventType.class, contextType = AppImp.class)
    static class AppStateMachine extends AbstractUntypedStateMachine {

        protected void addApplicationToScheduler(AppState from, AppState to, AppEventType type, AppImp appImp) {
            appImp.processor.process(new AppAddedSchedulerEvent(appImp.applicationId,
                    appImp.poolName, appImp.user, appImp.applicationPriority));
        }

        protected void amScheduler(AppState from, AppState to, AppEventType type, AppImp appImp) {
            PoolInfo poolInfo = null;
            for (ResourceRequest amReq : appImp.amReqs) {
                String pool = appImp.getPool();
                if (poolInfo == null) {
                    try {
                        poolInfo = appImp.scheduler.getPoolInfo(pool, false, false);
                    } catch (IOException e) {
                        log.error("Could not find pool for application: ", e);
                        appImp.processor.process(new AppEvent(appImp.applicationId, AppEventType.RECOVER));
                    }
                }
            }
            Allocation amAppWorkAllocation = appImp.scheduler.allocate(appImp.applicationId, appImp.amReqs, EMPTY_APP_WORK_RELEASES);
            if (amAppWorkAllocation != null && amAppWorkAllocation.getAppWorks() != null) {
                assert amAppWorkAllocation.getAppWorks().size() == 0;
            }
            appImp.scheduledTime = System.currentTimeMillis();
        }

        protected void amAppWorkAllocation(AppState from, AppState to, AppEventType type, AppImp appImp) {
            Allocation amAppWorkAllocation = appImp.scheduler.allocate(appImp.applicationId, EMPTY_APP_WORK_REQUESTS, EMPTY_APP_WORK_RELEASES);

            if (amAppWorkAllocation.getAppWorks().size() == 0) {
                return;
            }

            RemoteAppWork amAppWork = amAppWorkAllocation.getAppWorks().get(0);
            SchedulerUnitImp masterAppWork = (SchedulerUnitImp) appImp.scheduler.getSchedulerUnit(amAppWork.getAppWorkId());
            if (masterAppWork == null) {
                return;
            }
            appImp.setMasterAppWork(amAppWork);
            masterAppWork.setAMAppWork(true);
            appImp.getApplicationSubmission().setResource(appImp.getMasterAppWork().getResource());
            appImp.appWorkAllocatedTime = System.currentTimeMillis();
            long allocationDelay = appImp.appWorkAllocatedTime - appImp.scheduledTime;

        }

        protected void launchAM(AppState from, AppState to, AppEventType type, AppImp appImp) {
            appImp.launchAMEndTime = System.currentTimeMillis();
            long delay = appImp.launchAMEndTime - appImp.launchAMStartTime;

            appImp.launchAppMaster();
        }

    }
}
