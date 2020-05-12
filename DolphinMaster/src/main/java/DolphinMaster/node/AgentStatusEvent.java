package DolphinMaster.node;

import agent.status.AgentStatus;
import common.struct.AgentId;

public class AgentStatusEvent extends NodeEvent {
    private final AgentStatus status;
    public AgentStatusEvent(AgentId id, AgentStatus status) {
        super(id, NodeEventType.STATUS_UPDATE);
        this.status = status;
    }
}
