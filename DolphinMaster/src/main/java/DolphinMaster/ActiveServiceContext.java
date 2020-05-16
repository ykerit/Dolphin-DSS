package DolphinMaster;

import DolphinMaster.agentmanage.AgentListManage;
import DolphinMaster.agentmanage.AgentTrackerService;
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
    private AgentListManage agentListManage;
    private AgentTrackerService agentTrackerService;
    private AppMasterService appMasterService;
    private ClientService clientService;

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

    public AgentListManage getAgentListManage() {
        return agentListManage;
    }

    public void setAgentListManage(AgentListManage agentListManage) {
        this.agentListManage = agentListManage;
    }

    public AgentTrackerService getAgentTrackerService() {
        return agentTrackerService;
    }

    public void setAgentTrackerService(AgentTrackerService agentTrackerService) {
        this.agentTrackerService = agentTrackerService;
    }

    public AppMasterService getAppMasterService() {
        return appMasterService;
    }

    public void setAppMasterService(AppMasterService appMasterService) {
        this.appMasterService = appMasterService;
    }

    public ClientService getClientService() {
        return clientService;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }
}
