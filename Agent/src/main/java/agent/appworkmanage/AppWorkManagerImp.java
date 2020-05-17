package agent.appworkmanage;

import agent.CompletedAppWorksEvent;
import agent.CompletedAppsEvent;
import agent.Context;
import agent.UpdateAppWorksEvent;
import agent.application.Application;
import agent.application.ApplicationEvent;
import agent.application.ApplicationEventType;
import agent.application.ApplicationFinishedEvent;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkEvent;
import agent.appworkmanage.appwork.AppWorkEventType;
import agent.appworkmanage.appwork.AppWorkKillEvent;
import agent.appworkmanage.launcher.AbstractAppWorkLauncher;
import agent.appworkmanage.launcher.AppWorkLauncherPool;
import agent.appworkmanage.launcher.AppWorkLauncherPoolEventType;
import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.appworkmanage.monitor.AppWorkMonitorEventType;
import agent.appworkmanage.monitor.AppWorkMonitorImp;
import agent.appworkmanage.scheduler.AppWorkScheduler;
import agent.appworkmanage.scheduler.AppWorkSchedulerEventType;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.ChaosService;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AppWorkManagerImp extends ChaosService implements AppWorkManager {

    private static final Logger log = LogManager.getLogger(AppWorkManagerImp.class.getName());

    private final Context context;
    private final AppWorkMonitor monitor;
    private final AbstractAppWorkLauncher appWorkLauncher;
    private final EventDispatcher dispatcher;
    private final AppWorkScheduler scheduler;

    private final Lock readLock;
    private final Lock writeLock;

    public AppWorkManagerImp(Context context, AppWorkExecutor executor) {
        super(AppWorkManagerImp.class.getName());
        this.context = context;
        this.dispatcher = new EventDispatcher("AppWorkManage Dispatcher");

        appWorkLauncher = createAppWorkLauncher(context, executor);
        addService(appWorkLauncher);

        scheduler = createAppWorkScheduler(context);
        addService(scheduler);

        monitor = createAppWorkMonitor();
        addService(monitor);

        dispatcher.register(ApplicationEventType.class, new ApplicationEventDispatcher());
        dispatcher.register(AppWorkEventType.class, new AppWorkEventDispatcher());

        dispatcher.register(AppWorkEventType.class, new AppWorkEventProcess());
        dispatcher.register(AppWorkMonitorEventType.class, monitor);
        dispatcher.register(AppWorkLauncherPoolEventType.class, appWorkLauncher);
        dispatcher.register(AppWorkSchedulerEventType.class, scheduler);

        addService(dispatcher);

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    protected void serviceInit() throws Exception {
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
    }

    @Override
    public AppWorkMonitor getAppWorkMonitor() {
        return null;
    }

    @Override
    public AppWorkScheduler getAppWorkScheduler() {
        return null;
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
                    dispatcher.getEventProcessor().process(new ApplicationFinishedEvent(applicationId, "Aplication kiiled"));
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
                    dispatcher.getEventProcessor().process(new AppWorkKillEvent(appWorkId));
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

    // pending
    protected AppWorkScheduler createAppWorkScheduler(Context context) {
        return new AppWorkScheduler(context, dispatcher, 100);
    }

    class AppWorkEventProcess implements EventProcessor<AppWorkEvent> {

        @Override
        public void process(AppWorkEvent event) {
            ConcurrentMap<AppWorkId, AppWork> appWorks = AppWorkManagerImp.this.context.getAppWorks();
            AppWork work = appWorks.get(event.getAppWorkId());
            if (work != null) {
                work.process(event);
            } else {
                log.warn("Event: {} can't send event to AppWork:{}", event, event.getAppWorkId());
            }
        }
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
