package DolphinMaster;

import DolphinMaster.agentmanage.AgentTrackerService;
import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import DolphinMaster.security.SecurityManager;
import agent.application.Application;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.service.ServiceState;
import common.struct.AgentId;
import common.struct.ApplicationId;
import config.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DolphinContext {
    private ServiceContext serviceContext;
    private ActiveServiceContext activeServiceContext;
    private DolphinMaster dolphinMaster;
    private AgentTrackerService agentTrackerService;
    private SecurityManager securityManager;



    public DolphinContext() {
        this.serviceContext = new ServiceContext();
        this.activeServiceContext = new ActiveServiceContext();
    }

    public DolphinContext(EventDispatcher dispatcher, Configuration configuration) {
        this();
        setConfiguration(configuration);
        this.setDolphinDispatcher(dispatcher);
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
        return agentTrackerService;
    }

    protected void setAgentTrackerService(AgentTrackerService agentTrackerService) {
        this.agentTrackerService = agentTrackerService;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public ConcurrentMap<AgentId, Node> getNodes() {
        return activeServiceContext.getNodes();
    }

    public ConcurrentMap<ApplicationId, App> getApps() {
        return activeServiceContext.getApplications();
    }

}
