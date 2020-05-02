package agent;

import agent.agentstatusreport.ActionProcessor;
import agent.agentstatusreport.AgentHeartBeat;
import agent.agentstatusreport.AgentStatusPollService;
import agent.agentstatusreport.HeartBeatEventType;
import agent.appworkmanage.*;
import common.event.ActionType;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.exception.DolphinRuntimeException;
import common.service.ChaosService;
import config.Configuration;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class Agent extends ChaosService implements EventProcessor<AgentEvent> {
    private Context context;
    private EventDispatcher eventDispatcher;
    private AgentStatusPollService agentStatusPollService;
    private AgentHeartBeat agentHeartBeat;
    private AgentResourceMonitor resourceMonitor;
    private AppWorkManagerImp appWorkManager;


    public Agent() {
        super(Agent.class.getName());
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
        this.eventDispatcher = new EventDispatcher();
        addService(this.eventDispatcher);
        this.context.setAgentDispatcher(this.eventDispatcher);

        // ----------Agent Status Poll Service-------
        this.agentStatusPollService = createAgentStatusPollService();
        addService(agentStatusPollService);
        context.setAgentStatusPollService(agentStatusPollService);

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
        this.eventDispatcher.register(HeartBeatEventType.class, new AgentHeartBeat(this.context));
        this.eventDispatcher.register(ActionType.class, new ActionProcessor(this.context));
        this.eventDispatcher.register(AppWorkManagerEventType.class, appWorkManager);

        this.context.setAgent(this);
        this.context.getAppWorkExecutor().start();
        super.serviceInit();
    }

    @Override
    protected void serviceStop() {
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

    private AgentStatusPollService createAgentStatusPollService() {
        return new AgentStatusPollService(this.context);
    }

    private AgentResourceMonitor createAgentResourceMonitor() {
        return new AgentResourceMonitor(this.context);
    }

    protected AppWorkExecutor createAppWorkExecutor() {
        return new DefaultAppWorkExecutor();
    }

    protected AppWorkManagerImp createAppWorkManage(Context context, AppWorkExecutor executor) {
        return new AppWorkManagerImp(context, executor);
    }

    @Override
    public void process(AgentEvent event) {

    }
}
