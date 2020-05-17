package agent;

import agent.appworkmanage.*;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.exception.DolphinRuntimeException;
import common.service.ChaosService;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class AgentManager extends ChaosService implements EventProcessor<AgentEvent> {
    private Context context;
    private EventDispatcher dispatcher;
    private AgentStatusReporter statusReporter;
    private AgentResourceMonitor resourceMonitor;
    private AgentManageMetrics metrics;
    private AppWorkManagerImp appWorkManager;

    private static final Logger log = LogManager.getLogger(AgentManager.class.getName());


    public AgentManager() {
        super(AgentManager.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        this.context = new Context();
        this.context.setConfiguration(new Configuration());
        // -----------Client init------------
        try {
            StandaloneClient.CS().init();
        } catch (ClassNotFoundException | RemoteReadException | IOException e) {
            e.printStackTrace();
        }
        // ----------Event Dispatcher---------
        this.dispatcher = new EventDispatcher("Agent Manager Dispatcher");
        addService(this.dispatcher);
        this.context.setAgentDispatcher(this.dispatcher);

        // ----------Agent Metrics ----------
        this.metrics = createMetrics();
        this.context.setMetrics(this.metrics);

        // ----------Agent Status Poll Service-------
        this.statusReporter = createAgentStatusReporterService(context, dispatcher, metrics);
        addService(statusReporter);
        context.setAgentStatusReporter(statusReporter);

        // ----------Agent Resource Monitor -----------
        this.resourceMonitor = createAgentResourceMonitor();
        addService(this.resourceMonitor);
        context.setAgentResourceMonitor(resourceMonitor);

        // ----------AppWork Executor----------------
        AppWorkExecutor executor = createAppWorkExecutor();
        try {
            executor.init(context);
        } catch (IOException e) {
            throw new DolphinRuntimeException("Failed to init AppWork Executor", e);
        }
        context.setAppWorkExecutor(executor);

        // ----------AppWork Manager ---------------
        appWorkManager = createAppWorkManage(context, executor);
        addService(appWorkManager);
        context.setAppWorkManager(appWorkManager);


        // ----------Event Register-------------
        this.dispatcher.register(AppWorkManagerEventType.class, appWorkManager);
        this.dispatcher.register(AgentEventType.class, this);

        this.context.getAppWorkExecutor().start();
        this.context.setAgentManager(this);
        super.serviceInit();
    }

    @Override
    protected void serviceStop() throws Exception {
        // ------------Client Dispose-----------
        try {
            StandaloneClient.CS().dispose();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (this.context != null) {
            this.context.getAppWorkExecutor().stop();
        }
        super.serviceStop();
    }

    private AgentManageMetrics createMetrics() {
        return new AgentManageMetrics();
    }

    private AgentStatusReporter createAgentStatusReporterService(Context context, EventDispatcher dispatcher, AgentManageMetrics metrics) {
        return new AgentStatusReporter(context, dispatcher, metrics);
    }

    private AppWorkExecutorImp createAppWorkExecutor() {
        return new AppWorkExecutorImp();
    }

    private AgentResourceMonitor createAgentResourceMonitor() {
        return new AgentResourceMonitor(this.context);
    }

    protected AppWorkManagerImp createAppWorkManage(Context context, AppWorkExecutor executor) {
        return new AppWorkManagerImp(context, executor);
    }

    protected void shutdown() {
        new Thread() {
            @Override
            public void run() {
                try {
                    AgentManager.this.stop();
                } catch (Exception e) {
                    log.error("Error while shutdown AgentManage", e);
                } finally {
                    System.exit(0);
                }
            }
        };
    }

    protected void rsync() {
        // 1. cleanup AppWork
        // 2. reboot Agent status reporter
    }

    @Override
    public void process(AgentEvent event) {
        switch (event.getType()) {
            case SHUTDOWN:
                shutdown();
                break;
            case RSYNC:
                rsync();
                break;
            default:
                break;
        }
    }
}
