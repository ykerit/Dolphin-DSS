package DolphinMaster.app;

import DolphinMaster.DolphinContext;
import DolphinMaster.scheduler.event.AppAddedSchedulerEvent;
import common.context.ApplicationSubmission;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.struct.AgentId;
import common.struct.ApplicationId;
import common.struct.Priority;
import common.struct.RemoteAppWork;
import common.util.SystemClock;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

    private final ApplicationId applicationId;
    private final DolphinContext context;
    private final Configuration configuration;
    private final String user;
    private final String name;
    private final ApplicationSubmission submission;
    private final EventDispatcher dispatcher;
    private final StringBuilder tips = new StringBuilder();
    private final String applicationType;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final long submitTime;
    private final Set<String> applicationTags;
    private Map<String, String> applicationEnv = new HashMap<>();
    private RemoteAppWork masterAppWork;

    private SystemClock clock;

    private long startTime;
    private long launchTime = 0;
    private long finishTime = 0;

    private String poolName;
    private Set<AgentId> runNodes = new ConcurrentSkipListSet<>();
    private Priority applicationPriority;
    private EventProcessor processor;
    private AppState state;

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
                  Set<String> applicationTags) {
        this.applicationId = applicationId;
        this.context = context;
        this.configuration = configuration;
        this.name = name;
        this.poolName = pool;
        this.user = user;
        this.submission = submission;
        this.submitTime = submitTime;
        this.applicationType = applicationType;
        this.dispatcher = context.getDolphinDispatcher();
        this.processor = dispatcher.getEventProcessor();
        this.applicationTags = applicationTags;
        this.state = AppState.NEW;
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
    public AppState getState() {
        readLock.lock();
        try {
            return state;
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
        final AppState state = getState();
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
            switch (event.getType()) {
                case START:
                    addApplicationToScheduler();
                    break;
                case STATUS_UPDATE:
                    break;
                case APP_ACCEPTED:

            }
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

    private void addApplicationToScheduler() {
        processor.process(new AppAddedSchedulerEvent(this.applicationId,
                this.poolName, this.user, this.applicationPriority));
    }

    // AppState update will make event
    private void updateAppState(AppState oldState, AppState newState) {
        if (oldState.equals(AppState.NEW) && newState.equals(AppState.SUBMITTED)) {
            // when
        }
        if (oldState.equals(AppState.SUBMITTED) && newState.equals(AppState.ACCEPTED)) {

        }
    }
}
