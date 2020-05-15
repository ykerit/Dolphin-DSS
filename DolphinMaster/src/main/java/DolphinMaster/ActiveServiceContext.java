package DolphinMaster;

import DolphinMaster.app.AMLiveLinessMonitor;
import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import DolphinMaster.scheduler.ResourceScheduler;
import common.struct.AgentId;
import common.struct.ApplicationId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ActiveServiceContext {
    private final ConcurrentMap<ApplicationId, App> applications = new ConcurrentHashMap<>();
    private final ConcurrentMap<AgentId, Node> nodes = new ConcurrentHashMap<>();
    private final ConcurrentMap<AgentId, Node> inactiveNodes = new ConcurrentHashMap<>();

    private AMLiveLinessMonitor amLiveLinessMonitor;
    private ResourceScheduler scheduler;

    public ConcurrentMap<ApplicationId, App> getApplications() {
        return applications;
    }

    public ConcurrentMap<AgentId, Node> getNodes() {
        return nodes;
    }

    public ConcurrentMap<AgentId, Node> getInactiveNodes() {
        return inactiveNodes;
    }

    public ActiveServiceContext() {
    }

    public ActiveServiceContext(AMLiveLinessMonitor amLiveLinessMonitor) {
        this.amLiveLinessMonitor = amLiveLinessMonitor;
    }

    public AMLiveLinessMonitor getAmLiveLinessMonitor() {
        return amLiveLinessMonitor;
    }

    public void setAmLiveLinessMonitor(AMLiveLinessMonitor amLiveLinessMonitor) {
        this.amLiveLinessMonitor = amLiveLinessMonitor;
    }

    public void setScheduler(ResourceScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceScheduler getScheduler() {
        return scheduler;
    }
}
