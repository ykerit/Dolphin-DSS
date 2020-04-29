package agent;

import agent.agentstatusreport.ActionProcessor;
import agent.agentstatusreport.AgentHeartBeat;
import agent.agentstatusreport.AgentStatusPollService;
import agent.agentstatusreport.HeartBeatEventType;
import common.event.ActionType;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.ChaosService;
import config.Configuration;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class Agent extends ChaosService implements EventProcessor<AgentEvent> {
    private AgentContext agentContext;
    private EventDispatcher eventDispatcher;
    private AgentStatusPollService agentStatusPollService;
    private AgentHeartBeat agentHeartBeat;

    public Agent() {
        super(Agent.class.getName());
    }

    @Override
    protected void serviceInit() {
        this.agentContext = new AgentContext();
        this.agentContext.setConfiguration(new Configuration());
        // -----------Client init------------
        try {
            StandaloneClient.CS().init();
        } catch (ClassNotFoundException | RemoteReadException | IOException e) {
            e.printStackTrace();
        }
        // ----------Event Dispatcher---------
        this.eventDispatcher = new EventDispatcher();
        addService(this.eventDispatcher);
        this.agentContext.setAgentDispatcher(this.eventDispatcher);

        // ----------Agent Status Poll Service-------
        this.agentStatusPollService = createAgentStatusPollService();
        addService(agentStatusPollService);

        this.agentContext.setAgent(this);

        // ----------Event Register-------------
        this.eventDispatcher.register(HeartBeatEventType.class, new AgentHeartBeat(this.agentContext));
        this.eventDispatcher.register(ActionType.class, new ActionProcessor(this.agentContext));

        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        // ------------Client Dispose-----------
        try {
            StandaloneClient.CS().dispose();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }

    private AgentStatusPollService createAgentStatusPollService() {
        return new AgentStatusPollService(this.agentContext);
    }

    @Override
    public void process(AgentEvent event) {

    }
}
