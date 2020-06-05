package DolphinMaster;

import DolphinMaster.agentmanage.AgentListManage;
import DolphinMaster.agentmanage.AgentTrackerService;
import DolphinMaster.app.AMLiveLinessMonitor;
import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.security.SecurityManager;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.service.ServiceState;
import common.struct.AgentId;
import common.struct.ApplicationId;
import config.Configuration;

import java.util.concurrent.ConcurrentMap;

public class DolphinContext implements AppMasterServiceContext {
    private ServiceContext serviceContext;
    private ActiveServiceContext activeServiceContext;
    private DolphinMaster dolphinMaster;

    public DolphinContext() {
        this.serviceContext = new ServiceContext();
        this.activeServiceContext = new ActiveServiceContext();
    }

    public DolphinContext(EventDispatcher dispatcher, Configuration configuration) {
        this.serviceContext = new ServiceContext();
        this.activeServiceContext = new ActiveServiceContext();
        setConfiguration(configuration);
        setDolphinDispatcher(dispatcher);
    }

    public DolphinMaster getDolphinMaster() {
        return this.dolphinMaster;
    }

    protected void setDolphinMaster(DolphinMaster dm) {
        this.dolphinMaster = dm;
    }

    public EventDispatcher getDolphinDispatcher() {
        return this.serviceContext.getDispatcher();
    }

    protected void setDolphinDispatcher(EventDispatcher dispatcher) {
        this.serviceContext.setDispatcher(dispatcher);
    }

    public Configuration getConfiguration() {
        return this.serviceContext.getConfiguration();
    }

    protected void setConfiguration(Configuration configuration) {
        this.serviceContext.setConfiguration(configuration);
    }

    public ServiceState getServiceState() {
        return this.serviceContext.getStatus();
    }

    protected void setServiceState(ServiceState serviceState) {
        this.serviceContext.setStatus(serviceState);
    }

    public AgentTrackerService getAgentTrackerService() {
        return activeServiceContext.getAgentTrackerService();
    }

    protected void setAgentTrackerService(AgentTrackerService agentTrackerService) {
        activeServiceContext.setAgentTrackerService(agentTrackerService);
    }

    public ConcurrentMap<AgentId, Node> getNodes() {
        return activeServiceContext.getNodes();
    }

    public ConcurrentMap<ApplicationId, App> getApps() {
        return activeServiceContext.getApplications();
    }

    public AMLiveLinessMonitor getAMLiveLinessMonitor() {
        return activeServiceContext.getAmLiveLinessMonitor();
    }

    public void setAMLiveLinessMonitor(AMLiveLinessMonitor amLiveLinessMonitor) {
        activeServiceContext.setAmLiveLinessMonitor(amLiveLinessMonitor);
    }

    public AgentListManage getAgentListManage() {
        return activeServiceContext.getAgentListManage();
    }

    public void setAgentListManage(AgentListManage agentListManage) {
        activeServiceContext.setAgentListManage(agentListManage);
    }

    public void setScheduler(ResourceScheduler scheduler) {
        activeServiceContext.setScheduler(scheduler);
    }

    public ResourceScheduler getScheduler() {
        return activeServiceContext.getScheduler();
    }

    public AppMasterService getAppMasterService() {
        return activeServiceContext.getAppMasterService();
    }

    public void setAppMasterService(AppMasterService appMasterService) {
        activeServiceContext.setAppMasterService(appMasterService);
    }

    public ClientService getClientService() {
        return activeServiceContext.getClientService();
    }

    public void setClientService(ClientService clientService) {
        this.activeServiceContext.setClientService(clientService);
    }
}
