package DolphinMaster.amlauncher;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import common.event.EventProcessor;
import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;

import java.util.concurrent.*;

public class AppMasterLauncher extends AbstractService implements EventProcessor<AMLauncherEvent> {
    private static final Logger log = LogManager.getLogger(AppMasterLauncher.class);

    private ThreadPoolExecutor launcherPool;
    private LauncherThread launcherThread;

    private final BlockingQueue<Runnable> amEvents = new LinkedBlockingQueue<>();

    private final DolphinContext context;


    public AppMasterLauncher(DolphinContext context) {
        super(AppMasterLauncher.class.getName());
        this.context = context;
        this.launcherThread = new LauncherThread();
    }

    @Override
    protected void serviceInit() throws Exception {

        ThreadFactory tf = new ThreadFactoryBuilder()
                .setNameFormat("AppMasterLauncher #%d")
                .build();

        launcherPool = new ThreadPoolExecutor(10, 10,
                1, TimeUnit.HOURS, new LinkedBlockingQueue<>());
        launcherPool.setThreadFactory(tf);

        StandaloneClient.CS().init();

        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        launcherThread.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        launcherThread.interrupt();
        launcherThread.join();
        launcherPool.shutdown();
        super.serviceStop();
    }

    @Override
    public void process(AMLauncherEvent event) {
        App app = event.getApp();
        switch (event.getType()) {
            case LAUNCH:
                launch(app);
                break;
            case CLEANUP:
                cleanup(app);
                break;
            default:
                break;
        }
    }

    private Runnable createLauncher(App app, AMLauncherEventType type) {
        Runnable launch = new AMLauncher(context, app, type, context.getConfiguration());
        return launch;
    }

    private void launch(App app) {
        Runnable launcher = createLauncher(app, AMLauncherEventType.LAUNCH);
        amEvents.add(launcher);
    }

    private void cleanup(App app) {
        Runnable launcher = createLauncher(app, AMLauncherEventType.CLEANUP);
        amEvents.add(launcher);
    }

    private class LauncherThread extends Thread {

        public LauncherThread() {
            super("AppMaster Launcher");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                Runnable toLaunch;
                try {
                    toLaunch = amEvents.take();
                    launcherPool.execute(toLaunch);
                } catch (InterruptedException e) {
                    log.warn(this.getClass().getName() + " interrupted. Returning.");
                    return;
                }
            }
        }
    }

}
