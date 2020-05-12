package DolphinMaster.node;

import common.event.AbstractEvent;
import common.struct.AgentId;

public class NodeEvent extends AbstractEvent<NodeEventType> {
    private final AgentId agentId;
    public NodeEvent(AgentId id, NodeEventType nodeEventType) {
        super(nodeEventType);
        this.agentId = id;
    }

    public AgentId getAgentId() {
        return agentId;
    }
}
