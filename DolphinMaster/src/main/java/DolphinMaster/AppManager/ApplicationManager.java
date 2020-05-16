package DolphinMaster.AppManager;

import DolphinMaster.AppMasterService;
import DolphinMaster.ApplicationPlacementContext;
import DolphinMaster.DolphinContext;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import DolphinMaster.app.AppImp;
import DolphinMaster.scheduler.FairScheduler;
import DolphinMaster.scheduler.ResourcePoolImp;
import DolphinMaster.scheduler.ResourceScheduler;
import common.context.ApplicationSubmission;
import common.event.EventProcessor;
import common.exception.DolphinException;
import common.struct.ApplicationId;
import config.Configuration;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

// manages the list of applications
public class ApplicationManager implements EventProcessor<ApplicationManagerEvent> {
    private static final Logger log = LogManager.getLogger(ApplicationManager.class);

    protected LinkedList<ApplicationId> completedApps = new LinkedList<>();

    private final DolphinContext context;
    private final AppMasterService masterService;
    private final ResourceScheduler scheduler;
    private Configuration configuration;

    public ApplicationManager(DolphinContext context, ResourceScheduler scheduler,
                              AppMasterService masterService, Configuration configuration) {
        this.context = context;
        this.masterService = masterService;
        this.scheduler = scheduler;
        this.configuration = configuration;
    }

    static class ApplicationSummary {
        static final Logger log = LogManager.getLogger(ApplicationSummary.class);
        static final String EQUALS = "=";

        static class SummaryBuilder {
            final StringBuilder builder = new StringBuilder();

            SummaryBuilder add(String key, long value) {
                return _add(key, Long.toString(value));
            }

            <T> SummaryBuilder add(String key, T value) {
                String last = String.valueOf(value);
                return _add(key, last);
            }

            SummaryBuilder _add(String key, String value) {
                if (builder.length() > 0) {
                    builder.append(StringUtil.COMMA);
                }
                builder.append(key).append(EQUALS).append(value);
                return this;
            }

            @Override
            public String toString() {
                return builder.toString();
            }
        }

        public static SummaryBuilder createAppSummary() {
            SummaryBuilder summary = new SummaryBuilder();
            return summary;
        }
    }

    protected synchronized int getCompletedAppsListSize() {
        return completedApps.size();
    }

    protected synchronized void finishApplication(ApplicationId applicationId) {
        if (applicationId == null) {
            log.error("AppManager received completed application id is null, skipping!");
        } else {
            completedApps.add(applicationId);
        }
    }

    public void submitApplication(ApplicationSubmission submission, long submitTime, String user) throws DolphinException {
        ApplicationId applicationId = submission.getApplicationId();
        // 1. createApp
        // 2. context.getApps().putIfAbsent()
        AppImp application = createAndPopulateNewApp(submission, submitTime, user, -1);
        this.context.getDolphinDispatcher().
                getEventProcessor().process(new AppEvent(applicationId, AppEventType.START));
    }

    private AppImp createAndPopulateNewApp(ApplicationSubmission submission, long submitTime,
                                           String user, long startTime) throws DolphinException {
        ApplicationId applicationId = submission.getApplicationId();
        String poolName = submission.getPool();
        ResourcePoolImp pool = ((FairScheduler) scheduler).getPoolManager().getPool(poolName);
        while (pool == null) {
            int sepIndex = poolName.lastIndexOf(".");
            poolName = poolName.substring(0, sepIndex);
            pool = ((FairScheduler) scheduler).getPoolManager().getPool(poolName);
        }
        log.debug("application can submit the pool: " + poolName);
        AppImp application = new AppImp(applicationId,
                context, configuration,
                submission.getApplicationName(), user,
                submission.getPool(), submission,
                submitTime, submission.getApplicationType(),
                startTime, submission.getApplicationTags());

        if (context.getApps().putIfAbsent(applicationId, application) != null) {
            String message = "Application with id " + applicationId + " is already existed";
            log.warn(message);
            throw new DolphinException(message);
        }
        return application;
    }

    private void copyPlacemenToSubmission(ApplicationPlacementContext context, ApplicationSubmission submission) {
        if (context != null && !context.getPool().equals(submission.getPool())) {
            log.info("Placed application with id:" + submission.getApplicationId() + "in pool: " +
                    context.getPool() + ", original submission pool was: " + submission.getPool());
            submission.setPool(context.getPool());
        }
    }

    @Override
    public void process(ApplicationManagerEvent event) {
        ApplicationId applicationId = event.getApplicationId();
        log.debug("AppManager processing event for {} of type {}", applicationId, event.getType());
        switch (event.getType()) {
            case APP_COMPLETED:
                finishApplication(applicationId);
                break;
        }
    }
}
