package DolphinMaster.node;

import common.resource.Resource;
import common.struct.AgentId;

public class NodeResourceUpdateEvent extends NodeEvent {
    private final Resource updateResource;

    public NodeResourceUpdateEvent(AgentId id, Resource updateResource) {
        super(id, NodeEventType.RESOURCE_UPDATE);
        this.updateResource = updateResource;
    }

    public Resource getUpdateResource() {
        return updateResource;
    }
}
