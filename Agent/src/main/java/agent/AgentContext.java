package agent;

import agent.agent_status_report.AgentStatusPollService;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.struct.AgentID;
import config.Configuration;
import org.greatfree.util.IPAddress;

public class AgentContext {
    private ServiceContext serviceContext;
    private Agent agent;
    private AgentStatusPollService agentStatusPollService;
    private AgentID agentID;
    private String token;

    public AgentContext() {
        this.serviceContext = new ServiceContext();
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public AgentStatusPollService getAgentStatusPollService() {
        return agentStatusPollService;
    }

    public void setAgentStatusPollService(AgentStatusPollService agentStatusPollService) {
        this.agentStatusPollService = agentStatusPollService;
    }

    public EventDispatcher getAgentDispatcher() {
        return this.serviceContext.getDispatcher();
    }

    public void setAgentDispatcher(EventDispatcher eventDispatcher) {
        this.serviceContext.setDispatcher(eventDispatcher);
    }

    public Configuration getConfiguration() {
        return serviceContext.getConfiguration();
    }

    protected void setConfiguration(Configuration configuration) {
        serviceContext.setConfiguration(configuration);
    }

    public IPAddress getRemote() {
        return this.serviceContext.getConfiguration().getDolphinMasterNodeHost();
    }

    public void setAgentID(AgentID agentID) {
        this.agentID = agentID;
    }

    public AgentID getAgentID() {
        return agentID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
