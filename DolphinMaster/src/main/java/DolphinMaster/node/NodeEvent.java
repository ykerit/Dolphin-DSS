package DolphinMaster.node;

import common.event.AbstractEvent;
import common.struct.AgentId;

public class NodeEvent extends AbstractEvent<NodeEventType> {
    public NodeEvent(AgentId id, NodeEventType nodeEventType) {
        super(nodeEventType);
    }
}
