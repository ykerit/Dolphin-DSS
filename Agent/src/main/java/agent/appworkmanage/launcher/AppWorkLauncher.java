package agent.appworkmanage.launcher;

import agent.Context;
import agent.appworkmanage.application.Application;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import agent.appworkmanage.appwork.*;
import agent.context.AppWorkPrepareContext;
import agent.context.AppWorkStartContext;
import common.context.AppWorkLaunchContext;
import common.event.EventDispatcher;
import common.struct.AppWorkId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppWorkLauncher implements Callable<Integer> {

    private static final Logger log = LogManager.getLogger(AppWorkLauncher.class);

    protected final Application application;
    protected final EventDispatcher dispatcher;
    protected final AppWorkExecutor executor;
    protected final AppWork appWork;

    private final Context context;
    private final AppWorkManagerImp appWorkManager;
    private final Lock launchLock = new ReentrantLock();
    private AtomicBoolean appWorkAlreadyLaunched = new AtomicBoolean(false);
    private AtomicBoolean completed = new AtomicBoolean(false);
    private long maxWaitKillTime = 2000L;

    public AppWorkLauncher(Context context,
                           EventDispatcher dispatcher,
                           AppWorkExecutor executor,
                           Application app,
                           AppWork appWork,
                           AppWorkManagerImp appWorkManager) {
        this.application = app;
        this.dispatcher = dispatcher;
        this.executor = executor;
        this.appWork = appWork;
        this.context = context;
        this.appWorkManager = appWorkManager;
    }

    @Override
    public Integer call() throws Exception {
        if (!validateAppWorkState()) {
            return 0;
        }
        final AppWorkLaunchContext launchContext = appWork.getAppWorkLaunchContext();
        AppWorkId appWorkId = appWork.getAppWorkId();
        String appWorkIdStr = appWorkId.toString();
        final List<String> command = launchContext.getCommands();
        int ret = -1;

        final String user = appWork.getUser();
        ret = launchAppWork(new AppWorkStartContext.Builder()
                .setAppWork(appWork)
                .setUser(user)
                .setAppId(application.getApplicationId().toString())
                .setWorkspace(null)
                .setAppLocalDirs(null)
                .setAppWorkScriptPath(null)
                .setFileCacheDirs(null)
                .build());
        return ret;
    }

    private void prepareAppWork(Map<Path, List<String>> localizeResource,
                                List<String> appWorkLocalDirs) throws IOException {
        executor.prepareAppWork(new AppWorkPrepareContext.Builder()
                .setAppWork(appWork)
                .setUser(appWork.getUser())
                .setAppWorkLocalDirs(appWorkLocalDirs)
                .setLocalizeResource(localizeResource)
                .setCommands(appWork.getAppWorkLaunchContext().getCommands())
                .build());
    }

    protected boolean validateAppWorkState() {
        if (appWork.getAppWorkState() == AppWorkState.KILLING) {
            dispatcher.getEventProcessor().
                    process(new AppWorkExitEvent(appWork.getAppWorkId(),
                            AppWorkEventType.APP_WORK_EXIT_KILLED, -1, "AppWork terminated before launch"));
            return false;
        }
        return true;
    }

    protected int prepareForLaunch(AppWorkStartContext ctx) {
        AppWorkId appWorkId = appWork.getAppWorkId();
        dispatcher.getEventProcessor().process(new AppWorkEvent(appWorkId, AppWorkEventType.APP_WORK_LAUNCHED));
        if (!appWorkAlreadyLaunched.compareAndSet(false, true)) {
            log.info("AppWork {} not launched as cleanup", appWorkId);
            return -1;
        } else {
        }
        return ExitCode.SUCCESS.getCode();
    }

    protected int launchAppWork(AppWorkStartContext ctx) throws IOException {
        int launchPrep = prepareForLaunch(ctx);
        if (launchPrep == 0) {
            launchLock.lock();
            try {
                executor.launchAppWork(ctx);
            } finally {
                launchLock.unlock();
            }
        }
        return launchPrep;
    }

    protected Map<Path, List<String>> getLocalizeResource() {
        Map<Path, List<String>> localResource = appWork.getLocalizeResource();
        return localResource;
    }
}
