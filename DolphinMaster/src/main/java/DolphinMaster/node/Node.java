package DolphinMaster.node;

import common.resource.Resource;
import common.struct.AgentId;

public interface Node {
    AgentId getNodeId();

    String getHostName();

    String getNodeAddress();

    Resource getTotalCapability();

    Resource getPhysicalResource();

    void rsyncCapability();

    NodeState getState();
}
