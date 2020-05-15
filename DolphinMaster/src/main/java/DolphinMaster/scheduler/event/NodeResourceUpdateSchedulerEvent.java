package DolphinMaster.scheduler.event;

import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import common.resource.Resource;

public class NodeResourceUpdateSchedulerEvent extends SchedulerEvent {
    private final Node node;
    private Resource updateResource;
    public NodeResourceUpdateSchedulerEvent(Node node, Resource updateResource) {
        super(SchedulerEventType.NODE_RESOURCE_UPDATE);
        this.node = node;
        this.updateResource = updateResource;
    }

    public Node getNode() {
        return node;
    }

    public Resource getUpdateResource() {
        return updateResource;
    }
}
