package agent.appworkmanage.launcher;

import agent.Context;
import agent.application.Application;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkState;
import agent.context.AppWorkPrepareContext;
import common.event.EventDispatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppWorkLauncher implements Callable<Integer> {

    protected final Application application;
    protected final EventDispatcher dispatcher;
    protected final AppWorkExecutor executor;
    protected final AppWork appWork;

    private final Context context;
    private final AppWorkManagerImp appWorkManager;

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
        return null;
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
            return false;
        }
        return true;
    }
}
