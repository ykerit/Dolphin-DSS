package agent;

import agent.agentstatusreport.AgentStatusPollService;
import agent.application.Application;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import agent.appworkmanage.appwork.AppWork;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.struct.AgentID;
import config.Configuration;
import org.greatfree.util.IPAddress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Context {
    private ServiceContext serviceContext;
    private Agent agent;
    private AgentID agentID;
    private AgentStatusPollService agentStatusPollService;
    private ConcurrentMap<Long, Application> applications = new ConcurrentHashMap<>();
    private ConcurrentMap<String, AppWork> appWorks = new ConcurrentSkipListMap<>();
    private AppWorkExecutor executor;
    private AgentResourceMonitor monitor;
    private AppWorkManagerImp appWorkManager;

    public AppWorkExecutor getAppWorkExecutor() {
        return executor;
    }

    public void setAppWorkExecutor(AppWorkExecutor executor) {
        this.executor = executor;
    }

    public ConcurrentMap<Long, Application> getApplications() {
        return applications;
    }

    public ConcurrentMap<String, AppWork> getAppWorks() {
        return appWorks;
    }

    private String token;

    public Context() {
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

    public AgentStatusPollService getAgentStatusPollService() {
        return agentStatusPollService;
    }

    public void setAgentStatusPollService(AgentStatusPollService agentStatusPollService) {
        this.agentStatusPollService = agentStatusPollService;
    }

    public AgentResourceMonitor getAgentResourceMonitor() {
        return monitor;
    }

    public void setAgentResourceMonitor(AgentResourceMonitor monitor) {
        this.monitor = monitor;
    }

    public AppWorkManagerImp getAppWorkManager() {
        return appWorkManager;
    }

    public void setAppWorkManager(AppWorkManagerImp appWorkManager) {
        this.appWorkManager = appWorkManager;
    }
}
