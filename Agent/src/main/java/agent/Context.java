package agent;

import agent.application.Application;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import agent.appworkmanage.appwork.AppWork;
import common.context.ServiceContext;
import common.event.EventDispatcher;
import common.struct.AgentId;
import config.Configuration;
import org.greatfree.util.IPAddress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Context {
    private ServiceContext serviceContext;
    private AgentManager agentManager;
    private AgentId agentId;
    private AgentStatusReporter agentStatusReporter;
    private ConcurrentMap<ApplicationId, Application> applications = new ConcurrentHashMap<>();
    private ConcurrentMap<AppWorkId, AppWork> appWorks = new ConcurrentSkipListMap<>();
    private AppWorkExecutor executor;
    private AgentResourceMonitor monitor;
    private AppWorkManagerImp appWorkManager;
    private AgentManageMetrics metrics;

    public AppWorkExecutor getAppWorkExecutor() {
        return executor;
    }

    public void setAppWorkExecutor(AppWorkExecutor executor) {
        this.executor = executor;
    }

    public ConcurrentMap<ApplicationId, Application> getApplications() {
        return applications;
    }

    public ConcurrentMap<AppWorkId, AppWork> getAppWorks() {
        return appWorks;
    }

    private String token;

    public Context() {
        this.serviceContext = new ServiceContext();
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public EventDispatcher getAgentDispatcher() {
        return this.serviceContext.getDispatcher();
    }

    public void setAgentDispatcher(EventDispatcher specialDispatcher) {
        this.serviceContext.setDispatcher(specialDispatcher);
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

    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AgentStatusReporter getAgentStatusReporterService() {
        return agentStatusReporter;
    }

    public void setAgentStatusReporter(AgentStatusReporter agentStatusReporter) {
        this.agentStatusReporter = agentStatusReporter;
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

    public AgentManageMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(AgentManageMetrics metrics) {
        this.metrics = metrics;
    }
}
