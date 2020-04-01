package DolphinMaster;

import DolphinMaster.agent_manage.AgentTrackerService;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.service.ServiceState;
import config.Configuration;

public class DolphinContext {
    private ServiceContext serviceContext;
    private DolphinMaster dolphinMaster;
    private AgentTrackerService agentTrackerService;

    public DolphinContext() {
        this.serviceContext = new ServiceContext();
    }

    public DolphinContext(EventDispatcher dispatcher, Configuration configuration) {
        this();
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
}
