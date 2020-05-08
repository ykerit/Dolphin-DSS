package DolphinMaster.agentmanage;

import DolphinMaster.node.NodeEvent;
import DolphinMaster.node.NodeEventType;
import common.event.Event;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.struct.AgentId;
import common.util.LivelinessMonitor;
import config.Configuration;

public class AgentLivelinessMonitor extends LivelinessMonitor<AgentId> {
    private final Configuration configuration;
    private EventProcessor<Event> eventProcessor;
    public AgentLivelinessMonitor(Configuration configuration, EventDispatcher dispatcher) {
        super(AgentLivelinessMonitor.class.getName());
        this.configuration = configuration;
        eventProcessor = dispatcher.getEventProcessor();
    }

    @Override
    protected void serviceInit() throws Exception {
        setExpireFrequency(configuration.getAgentHeartBeatTimeOutFrequency());
        setExpireInterval(configuration.getAgentHeartBeatTimeOut());
        setMonitorInterval(configuration.getAgentMonitorInterval());
        super.serviceInit();
    }

    @Override
    protected void expire(AgentId key) {
        eventProcessor.process(new NodeEvent(key, NodeEventType.EXPIRE));
    }


}
