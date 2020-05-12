package DolphinMaster;

import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import common.struct.AgentId;
import common.struct.ApplicationId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ActiveServiceContext {
    private final ConcurrentMap<ApplicationId, App> applications = new ConcurrentHashMap<>();
    private final ConcurrentMap<AgentId, Node> nodes = new ConcurrentHashMap<>();
    private final ConcurrentMap<AgentId, Node> inactiveNodes = new ConcurrentHashMap<>();

    public ConcurrentMap<ApplicationId, App> getApplications() {
        return applications;
    }

    public ConcurrentMap<AgentId, Node> getNodes() {
        return nodes;
    }

    public ConcurrentMap<AgentId, Node> getInactiveNodes() {
        return inactiveNodes;
    }
}
