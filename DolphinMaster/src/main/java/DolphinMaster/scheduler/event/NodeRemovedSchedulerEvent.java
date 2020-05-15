package DolphinMaster.scheduler.event;

import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;

public class NodeRemovedSchedulerEvent extends SchedulerEvent {

    private final Node node;

    public NodeRemovedSchedulerEvent(Node node) {
        super(SchedulerEventType.NODE_REMOVED);
        this.node = node;
    }

    public Node getRemovedNode() {
        return node;
    }
}
