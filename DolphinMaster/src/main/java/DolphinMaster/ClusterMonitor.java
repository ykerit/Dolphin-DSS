package DolphinMaster;

import DolphinMaster.node.Node;
import agent.status.AppWorkStatus;
import common.resource.Resource;

import java.util.List;

public interface ClusterMonitor {

    void addNode(List<AppWorkStatus> appWorkStates, Node node);

    void removeNode(Node node);

    void updateNode(Node node);

    void updateNodeResource(Node node, Resource resource);
}
