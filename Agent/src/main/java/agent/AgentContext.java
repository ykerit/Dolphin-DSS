package agent;

import agent.agentstatusreport.AgentStatusPollService;
import agent.application.Application;
import agent.appworkmanage.appwork.AppWork;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.struct.AgentID;
import config.Configuration;
import org.greatfree.util.IPAddress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AgentContext {
    private ServiceContext serviceContext;
    private Agent agent;
    private AgentID agentID;
    private AgentStatusPollService agentStatusPollService;
    private ConcurrentMap<Long, Application> applications = new ConcurrentHashMap<>();
    private ConcurrentMap<String, AppWork> appWorks = new ConcurrentHashMap<>();

    public ConcurrentMap<Long, Application> getApplications() {
        return applications;
    }

    public ConcurrentMap<String, AppWork> getAppWorks() {
        return appWorks;
    }

    private String token;

    public AgentContext() {
        this.serviceContext = new ServiceContext();
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
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
