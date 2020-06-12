package agent.appworkmanage;

import agent.*;
import agent.appworkmanage.application.*;
import agent.appworkmanage.appwork.*;
import agent.appworkmanage.launcher.AbstractAppWorkLauncher;
import agent.appworkmanage.launcher.AppWorkLauncherPool;
import agent.appworkmanage.launcher.AppWorkLauncherPoolEventType;
import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.appworkmanage.monitor.AppWorkMonitorEventType;
import agent.appworkmanage.monitor.AppWorkMonitorImp;
import api.app_master_message.*;
import common.context.AppWorkLaunchContext;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.exception.DolphinException;
import common.resource.LocalResource;
import common.service.ChaosService;
import common.struct.AppWorkExitStatus;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;
import config.DefaultServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.server.container.ServerContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AppWorkManagerImp extends ChaosService implements AppWorkManager {

    private static final Logger log = LogManager.getLogger(AppWorkManagerImp.class.getName());

    private final Context context;
    private final AppWorkMonitor monitor;
    private final AbstractAppWorkLauncher appWorkLauncher;
    private final EventDispatcher dispatcher;
    private ServerContainer server;

    protected final AgentStatusReporter statusReporter;

    private boolean serviceStopped = false;

    private final Lock readLock;
    private final Lock writeLock;

    public AppWorkManagerImp(Context context, AppWorkExecutor executor, AgentStatusReporter statusReporter) {
        super(AppWorkManagerImp.class.getName());
        this.context = context;
        this.dispatcher = new EventDispatcher("AppWorkManage Dispatcher");

        appWorkLauncher = createAppWorkLauncher(context, executor);
        addService(appWorkLauncher);
        this.statusReporter = statusReporter;

        monitor = createAppWorkMonitor();
        addService(monitor);

        dispatcher.register(ApplicationEventType.class, new ApplicationEventDispatcher());
        dispatcher.register(AppWorkEventType.class, new AppWorkEventDispatcher());

        dispatcher.register(AppWorkMonitorEventType.class, monitor);
        dispatcher.register(AppWorkLauncherPoolEventType.class, appWorkLauncher);

        addService(dispatcher);

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    protected void serviceInit() throws Exception {
        server = new ServerContainer(DefaultServerConfig.AGENT_PORT, new AppWorkTask());
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        server.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        writeLock.lock();
        try {
            serviceStopped = true;
            if (context != null) {
                // cleanup work
            }
        } finally {
            writeLock.unlock();
        }
        if (server != null) {
            server.stop(1000L);
        }
        super.serviceStop();
    }

    @Override
    public AppWorkMonitor getAppWorkMonitor() {
        return monitor;
    }

    @Override
    public StartAppWorksResponse startAppWorks(StartAppWorksRequest requests) throws DolphinException, IOException {
        List<AppWorkId> succeededAppWorks = new ArrayList<>();
        synchronized (this.context) {
            for (StartAppWorkRequest request : requests.getRequests()) {
                AppWorkId appWorkId = request.getAppWork().getAppWorkId();
                try {
                    startAppWorkInterval(request, "root");
                    succeededAppWorks.add(appWorkId);
                } catch (DolphinException e) {
                    throw e;
                }
            }
        }
        return new StartAppWorksResponse(succeededAppWorks);
    }

    @Override
    public StopAppWorkResponse stopAppWorks(StopAppWorkRequest request) throws DolphinException, IOException {
        return null;
    }

    @Override
    public GetAppWorkStatusesResponse getAppWorkStatuses(GetAppWorkStatusesRequest request) throws DolphinException, IOException {
        return null;
    }

    protected void startAppWorkInterval(StartAppWorkRequest request, String remoteUser) throws DolphinException, IOException {
        RemoteAppWork remoteAppWork = request.getAppWork();
        AppWorkId appWorkId = remoteAppWork.getAppWorkId();
        String appWorkIdStr = appWorkId.toString();
        String user = request.getApplicationSubmitter();

        log.info("Start request for " + appWorkIdStr + "by user " + remoteUser);
        AppWorkLaunchContext launchContext = request.getAppWorkLaunchContext();
        for (Map.Entry<String, LocalResource> rsrc : launchContext
                .getLocalResource().entrySet()) {
            if (rsrc.getValue() == null || rsrc.getValue().getResource() == null) {
                throw new DolphinException("Null resource URL for local resource");
            } else if (rsrc.getValue().getType() == null) {
                throw new DolphinException("Null resource type for local resource "
                        + rsrc.getKey() + " : " + rsrc.getValue());
            }
        }

        long appWorkStartTime = System.currentTimeMillis();
        AppWork appWork = new AppWorkImp(this.context.getConfiguration(),
                this.dispatcher,
                launchContext,
                remoteAppWork,
                context,
                appWorkStartTime,
                user);
        ApplicationId applicationId = appWorkId.getApplicationId();
        if (context.getAppWorks().putIfAbsent(appWorkId, appWork) != null) {
            throw new DolphinException("AppWork " + appWorkIdStr + " already is running on this node");
        }
        this.readLock.lock();
        try {
            if (!isServiceStopped()) {
                if (!context.getApplications().containsKey(applicationId)) {
                    Application application = new ApplicationImp(dispatcher, user, applicationId, context);
                    if (context.getApplications().putIfAbsent(applicationId, application) == null) {
                        log.info("Creating a new Application for app " + applicationId);
                        dispatcher.getEventProcessor().process(new ApplicationEvent(applicationId, ApplicationEventType.INIT_APPLICATION));
                    }
                }
                dispatcher.getEventProcessor().process(new ApplicationAppWorkInitEvent(appWork));
            } else {
                throw new DolphinException("AppWork start failed as the Agent is in the process of shutting down");
            }

        } finally {
            this.readLock.unlock();
        }
    }

    protected boolean isServiceStopped() {
        return serviceStopped;
    }

    @Override
    public void process(AppWorkManagerEvent event) {
        log.debug("Processing event: {}", event.getType());
        switch (event.getType()) {
            case FINISH_APP:
                CompletedAppsEvent appFinishedEvent = (CompletedAppsEvent) event;
                for (ApplicationId applicationId : appFinishedEvent.getAppsToCleanup()) {
                    Application app = this.context.getApplications().get(applicationId);
                    if (app == null) {
                        log.info("could't find application: {} while process FINISH_APP event.", applicationId);
                        continue;
                    }
                    dispatcher.getEventProcessor().process(new ApplicationFinishedEvent(applicationId, "Application killed"));
                }
                break;
            case FINISH_APP_WORK:
                CompletedAppWorksEvent appWorksFinishedEvent = (CompletedAppWorksEvent) event;
                for (AppWorkId appWorkId : appWorksFinishedEvent.getAppWorkIds()) {
                    ApplicationId applicationId = appWorkId.getApplicationId();
                    Application app = context.getApplications().get(applicationId);
                    if (app == null) {
                        log.warn("could't find application: {} while process FINISH_APP_WORK.", applicationId);
                        continue;
                    }
                    AppWork appWork = app.getAppWorks().get(appWorkId);
                    if (appWork == null) {
                        log.warn("could't find AppWork: {} while process FINISH_APP_WORK", appWorkId);
                        continue;
                    }
                    dispatcher.getEventProcessor().process(new AppWorkKillEvent(appWorkId, AppWorkExitStatus.KILLED_BY_DOLPHIN_MASTER, "AppWork killed by DolphinMaster"));
                }
                break;
            case UPDATE_APP_WORK:
                UpdateAppWorksEvent updateAppWorkEvent = (UpdateAppWorksEvent) event;
                for (RemoteAppWork remoteAppWork : updateAppWorkEvent.getAppWorksToUpdate()) {
                    updateAppWorkInternal(remoteAppWork);
                }
                break;
            case SIGNAL_APP_WORK:
                break;

        }
    }

    private void updateAppWorkInternal(RemoteAppWork appWork) {

    }

    protected AbstractAppWorkLauncher createAppWorkLauncher(Context ctx, AppWorkExecutor executor) {
        AbstractAppWorkLauncher launcher = new AppWorkLauncherPool();
        launcher.init(ctx, this.dispatcher, executor, this);
        return launcher;
    }

    protected AppWorkMonitor createAppWorkMonitor() {
        return new AppWorkMonitorImp();
    }

    class AppWorkEventDispatcher implements EventProcessor<AppWorkEvent> {

        @Override
        public void process(AppWorkEvent event) {
            Map<AppWorkId, AppWork> appWorks = context.getAppWorks();
            AppWork appWork = appWorks.get(event.getAppWorkId());
            if (appWork != null) {
                appWork.process(event);
            } else {
                log.info("Event" + event + " not find AppWork: " + event.getAppWorkId());
            }
        }
    }

    class ApplicationEventDispatcher implements EventProcessor<ApplicationEvent> {
        @Override
        public void process(ApplicationEvent event) {
            Application app = context.getApplications().get(event.getApplicationId());
            if (app != null) {
                app.process(event);
            } else {
                log.warn("Event " + event + " not find application "
                        + event.getApplicationId());
            }
        }
    }
}
