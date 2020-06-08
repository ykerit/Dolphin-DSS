package agent.appworkmanage.launcher;

import agent.Context;
import agent.appworkmanage.application.Application;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import agent.appworkmanage.appwork.AppWork;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import common.event.EventDispatcher;
import common.service.AbstractService;
import common.struct.AppWorkId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppWorkLauncherPool extends AbstractService implements AbstractAppWorkLauncher {

    private static final Logger log = LogManager.getLogger(AppWorkLauncherPool.class.getName());

    private Context context;
    private AppWorkExecutor executor;
    private EventDispatcher dispatcher;
    private AppWorkManagerImp appWorkManager;

    public ExecutorService launcherPool = Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder()
                            .setNameFormat("AppWorkLauncher %d")
                            .build());

    public final ConcurrentMap<AppWorkId, AppWorkLauncher> running = new ConcurrentHashMap<>();

    public AppWorkLauncherPool() {
        super(AppWorkLauncherPool.class.getName());
    }

    @Override
    public void init(Context context, EventDispatcher dispatcher, AppWorkExecutor executor, AppWorkManagerImp appWorkManager) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.executor = executor;
        this.appWorkManager = appWorkManager;
    }

    @Override
    public void process(AppWorkLauncherPoolEvent event) {
        AppWork appWork = event.getAppWork();
        AppWorkId id = appWork.getAppWorkId();
        switch (event.getType()) {
            case LAUNCHER_APP_WORK:
                Application application = context.getApplications().get(id.getApplicationId());
                AppWorkLauncher launcher =
                        new AppWorkLauncher(context, dispatcher, executor, application, appWork, appWorkManager);
                launcherPool.submit(launcher);
                running.put(id, launcher);
                break;
            case RELAUNCHED_APP_WORK:
                application = context.getApplications().get(id.getApplicationId());
                break;
            case CLEANUP_APP_WORK:
                break;
            case CLEANUP_APP_WORK_FOR_REINIT:
                break;
            case SIGNAL_APP_WORK:
                break;
            default:
                break;
        }
    }
}
