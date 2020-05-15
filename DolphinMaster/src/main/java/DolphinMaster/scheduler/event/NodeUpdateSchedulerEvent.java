package DolphinMaster.scheduler.event;

import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;

public class NodeUpdateSchedulerEvent extends SchedulerEvent {
    private final Node node;
    public NodeUpdateSchedulerEvent(Node node) {
        super(SchedulerEventType.NODE_UPDATE);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
