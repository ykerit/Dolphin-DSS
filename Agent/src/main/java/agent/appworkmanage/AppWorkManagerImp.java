package agent.appworkmanage;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkEvent;
import agent.appworkmanage.appwork.AppWorkEventType;
import agent.appworkmanage.launcher.*;
import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.appworkmanage.monitor.AppWorkMonitorEventType;
import agent.appworkmanage.monitor.AppWorkMonitorImp;
import agent.appworkmanage.scheduler.AppWorkScheduler;
import agent.appworkmanage.scheduler.AppWorkSchedulerEventType;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.ChaosService;
import common.struct.AppWorkId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentMap;

public class AppWorkManagerImp extends ChaosService implements AppWorkManager {

    private static final Logger log = LogManager.getLogger(AppWorkManagerImp.class.getName());

    private final Context context;
    private final AppWorkMonitor monitor;
    private final AbstractAppWorkLauncher appWorkLauncher;
    private final EventDispatcher dispatcher;
    private final AppWorkScheduler scheduler;

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

        dispatcher.register(AppWorkEventType.class, new AppWorkEventProcess());
        dispatcher.register(AppWorkMonitorEventType.class, monitor);
        dispatcher.register(AppWorkLauncherPoolEventType.class, appWorkLauncher);
        dispatcher.register(AppWorkSchedulerEventType.class, scheduler);

        addService(dispatcher);
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
}
