package agent;

import agent.appworkmanage.application.Application;
import agent.appworkmanage.application.ApplicationImp;
import common.struct.*;
import agent.appworkmanage.application.ApplicationState;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkState;
import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.message.*;
import agent.status.AgentAction;
import agent.status.AgentStatus;
import common.event.EventDispatcher;
import common.exception.DolphinException;
import common.exception.DolphinRuntimeException;
import common.resource.Resource;
import common.resource.ResourceCollector;
import common.resource.ResourceUtilization;
import common.service.AbstractService;
import common.util.HardwareUtils;
import config.DefaultServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.util.IPAddress;
import org.greatfree.util.Tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class AgentStatusReporter extends AbstractService {

    private static final Logger log = LogManager.getLogger(AgentStatusReporter.class.getName());

    private final Context context;
    private final EventDispatcher dispatcher;

    private AgentId agentId;
    private Resource totalResource;
    private Resource physicalResource;

    private final AgentManageMetrics metrics;

    private Map<ApplicationId, Long> appTokenKeepAliveMap = new HashMap<>();

    // AppWork
    private final Map<AppWorkId, Long> recentlyStoppedAppWork;
    private final Map<AppWorkId, AppWorkStatus> pendingCompleteAppWork;
    private final static long DURATION_TO_TRACK_STOPPED_APP_WORK = 600000;
    private Set<AppWorkId> pendingAppWorksToRemove = new HashSet<>();

    // status manage
    private Thread statusReporter;
    private Runnable statusReporterRunnable;
    private boolean failedToConnect = false;
    private boolean registerSuccess = false;
    private volatile boolean shutdown = false;

    public AgentStatusReporter(Context context, EventDispatcher dispatcher, AgentManageMetrics metrics) {
        super(AgentStatusReporter.class.getName());
        this.metrics = metrics;
        this.context = context;
        this.dispatcher = dispatcher;
        this.recentlyStoppedAppWork = new LinkedHashMap<>();
        this.pendingCompleteAppWork = new HashMap<>();
    }

    @Override
    protected void serviceInit() throws Exception {
        ResourceCollector collector = ResourceCollector.newInstance();
        this.totalResource = HardwareUtils.getNodeResources(collector, context.getConfiguration());
        long memoryMB = totalResource.getMemorySize();
        int virtualCores = totalResource.getVCore();
        long physicalMemoryMB = memoryMB;
        int physicalCores = virtualCores;
        if (collector != null) {
            physicalCores = collector.getNumProcessors();
            physicalMemoryMB = collector.getMemorySize() / (1024 * 1024);
        }
        this.physicalResource = Resource.newInstance(physicalMemoryMB, physicalCores);

        log.info("AgentManager resources is: {}", totalResource);
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        agentId = buildAgentId();
        context.setAgentId(agentId);
        log.info("Agent id assigned is: " + this.agentId);
        try {
            registerWithDolphinMaster();
            super.serviceStart();
            startStatusReporter();
        } catch (Exception e) {
            String errMsg = "Failed to start AgentStatusReporter";
            log.error(errMsg);
            throw new DolphinRuntimeException(e);
        }
    }

    @Override
    protected void serviceStop() throws Exception {
        if (registerSuccess && !shutdown) {
            unregisterDolphin();
        }
        this.shutdown = true;
        super.serviceStop();
    }

    private AgentId buildAgentId() throws IOException {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("can't get host address!");
        }
        String hostname = address.getCanonicalHostName();
        AgentId ret = new AgentId();
        ret.setHostname(hostname);
        ret.setIP(Tools.getLocalIP());
        ret.setCommandPort(DefaultServerConfig.AGENT_PORT);
        return ret;
    }

    private boolean isApplicationStopped(ApplicationId applicationId) {
        if (!this.context.getApplications().containsKey(applicationId)) {
            return true;
        }
        ApplicationState applicationState = this.context.
                getApplications().get(applicationId).getAppState();
        if (applicationState == ApplicationState.FINISHING_APP_WORKS_WAIT
        || applicationState == ApplicationState.APPLICATION_RESOURCES_CLEANINGUP
        || applicationState == ApplicationState.FINISHED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAppWorkRecentlyStopped(AppWorkId appWorkId) {
        synchronized (recentlyStoppedAppWork) {
            return recentlyStoppedAppWork.containsKey(appWorkId);
        }
    }

    public void clearFinishedAppWorks() {
        synchronized (recentlyStoppedAppWork) {
            recentlyStoppedAppWork.clear();
        }
    }

    public void removeVeryOldStoppedAppWorks() {
        synchronized (recentlyStoppedAppWork) {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<AppWorkId, Long>> iterator = recentlyStoppedAppWork.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<AppWorkId, Long> entry = iterator.next();
                AppWorkId appWorkId = entry.getKey();
                if (entry.getValue() < currentTime) {
                    if (context.getAppWorks().containsKey(appWorkId)) {
                        iterator.remove();
                    }
                } else {
                    break;
                }
            }
        }
    }

    public void addCompletedAppWork(AppWorkId appWorkId) {
        synchronized (recentlyStoppedAppWork) {
            removeVeryOldStoppedAppWorks();
            if (!recentlyStoppedAppWork.containsKey(appWorkId)) {
                recentlyStoppedAppWork.put(appWorkId, System.currentTimeMillis() + DURATION_TO_TRACK_STOPPED_APP_WORK);
            }
        }
    }

    protected void registerWithDolphinMaster() throws IOException, DolphinException, RemoteReadException, ClassNotFoundException {
        RegisterAgentResponse registerResponse = null;

        synchronized (this.context) {
            RegisterAgentRequest request =
                    new RegisterAgentRequest(agentId,
                            totalResource,
                            physicalResource,
                            getAgentAppWorkStatuses(),
                            getRunningApplications());

            IPAddress remote = this.context.getRemote();
            registerResponse = (RegisterAgentResponse) StandaloneClient
                    .CS()
                    .read(remote.getIP(), remote.getPort(), request);
        }
        if (AgentAction.SHUTDOWN.equals(registerResponse.getAction())) {
            String message = "Receive from  DolphMaster message: " + registerResponse.getTips();
            throw new DolphinRuntimeException("Receive SHUTDOWN signal from DolphinMaster:" + message);
        }
        String masterKey = registerResponse.getToken();
        if (masterKey != null) {
            this.context.setToken(masterKey);
        }
        this.registerSuccess = true;

        StringBuffer successfulRegisterMsg = new StringBuffer();

        successfulRegisterMsg.append("Registered with DolphinMaster as ").append(agentId.toString());

        log.info(successfulRegisterMsg.toString());
    }

    protected void unregisterDolphin() {
        UnregisterAgentNotification notification = new UnregisterAgentNotification(this.agentId);
        try {
            StandaloneClient.CS().syncNotify(this.context.getRemote().getIP(),
                    this.context.getRemote().getPort(),
                    notification);
            log.info("Successfully Unregister the agent " + this.agentId + " with DolphinMaster");
        } catch (IOException | InterruptedException e) {
            log.warn("UnregisterDolphin failed agent is " + this.agentId);
        }
    }

    protected void startStatusReporter() {
        statusReporterRunnable = new StatusReporterRunnable();
        statusReporter = new Thread(statusReporterRunnable, "Agent Status Reporter");
        statusReporter.start();
    }

    private ResourceUtilization getAgentUtilization() {
        AgentResourceMonitor monitor = this.context.getAgentResourceMonitor();
        return monitor.getUtilization();
    }

    private ResourceUtilization getAppWorkUtilization() {
        AppWorkMonitor appWorkMonitor = this.context.getAppWorkManager().getAppWorkMonitor();
        return appWorkMonitor.getAppWorkUtilization();
    }

    // has question
    private void trackAppForKeepAlive(ApplicationId appId) {
        long nextTime = System.currentTimeMillis();
        appTokenKeepAliveMap.put(appId, nextTime);
    }

    private List<ApplicationId> createKeepAliveApplicationList() {
        List<ApplicationId> apps = new ArrayList<>();

        for (Iterator<Map.Entry<ApplicationId, Long>> iterator
             = this.appTokenKeepAliveMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<ApplicationId, Long> e = iterator.next();
            ApplicationId appId = e.getKey();
            Long nextKeepAlive = e.getValue();
            if (!this.context.getApplications().containsKey(appId)) {
                iterator.remove();
            } else if (System.currentTimeMillis() > nextKeepAlive) {
                apps.add(appId);
                trackAppForKeepAlive(appId);
            }
        }
        return apps;
    }

    protected List<ApplicationId> getRunningApplications() {
        List<ApplicationId> runningApps = new ArrayList<>();
        for (Map.Entry<ApplicationId, Application> appEntry : this.context.getApplications().entrySet()) {
            if (appEntry.getValue().getAppState() != ApplicationState.FINISHED) {
                runningApps.add(appEntry.getKey());
            }
        }
        return runningApps;
    }

    // Remote AppWork status
    protected List<AppWorkStatus> getAppWorkStatuses() {
        List<AppWorkStatus> statuses = new ArrayList<>();

        for (AppWork appWork : this.context.getAppWorks().values()) {
            AppWorkId appWorkId = appWork.getAppWorkId();
            ApplicationId applicationId = appWorkId.getApplicationId();
            AppWorkStatus appWorkStatus = appWork.cloneAndGetAppWorkStatus();
            if (appWorkStatus.getState() == RemoteAppWorkState.COMPLETE) {
                if (isApplicationStopped(applicationId)) {
                    log.debug("{} is completing, remove {} from agent context",
                            applicationId, appWorkId);
                    context.getAppWorks().remove(appWorkId);
                    pendingCompleteAppWork.put(appWorkId, appWorkStatus);
                } else {
                    if (!isAppWorkRecentlyStopped(appWorkId)) {
                        pendingCompleteAppWork.put(appWorkId, appWorkStatus);
                    }
                }
                addCompletedAppWork(appWorkId);
            } else {
                statuses.add(appWorkStatus);
            }
        }
        statuses.addAll(pendingCompleteAppWork.values());
        return statuses;
    }

    protected AgentStatus getAgentStatus() {
        List<AppWorkStatus> appWorkStatuses = getAppWorkStatuses();
        ResourceUtilization agentUtilization = getAgentUtilization();
        ResourceUtilization appWorkUtilization = getAppWorkUtilization();
        AgentStatus status = new AgentStatus(agentId,
                appWorkStatuses, createKeepAliveApplicationList(),
                appWorkUtilization, agentUtilization);
        return status;
    }

    private List<AgentAppWorkStatus> getAgentAppWorkStatuses() {
        List<AgentAppWorkStatus> appWorkStatuses = new ArrayList<>();
        for (AppWork appWork : this.context.getAppWorks().values()) {
            AppWorkId appWorkId = appWork.getAppWorkId();
            ApplicationId applicationId = appWorkId.getApplicationId();
            if (!this.context.getApplications().containsKey(applicationId)) {
                context.getAppWorks().remove(appWorkId);
                continue;
            }
            AgentAppWorkStatus status = appWork.getAgentAppWorkStatus();
            appWorkStatuses.add(status);
            if (status.getAppWorkState() == RemoteAppWorkState.COMPLETE) {
                addCompletedAppWork(appWorkId);
            }
        }
        log.info("Sending out " + appWorkStatuses.size() + " Agent AppWork statuses: " + appWorkStatuses);
        return appWorkStatuses;
    }

    private boolean processActionCommand(AgentHeartBeatResponse response) {
        if (response.getAction() == AgentAction.SHUTDOWN) {
            log.warn("Received SHUTDOWN action from DolphinMaster");
            dispatcher.getEventProcessor().process(new AgentEvent(AgentEventType.SHUTDOWN));
            return true;
        }
        if (response.getAction() == AgentAction.RSYNC) {
            log.warn("Agent is out of sync with DolphinMaster");
            dispatcher.getEventProcessor().process(new AgentEvent(AgentEventType.RSYNC));
            return true;
        }
        return false;
    }

    public void removeOrTrackCompletedAppWorks(List<AppWorkId> appWorkIds) {
        Set<AppWorkId> removedAppWorks = new HashSet<>();
        pendingAppWorksToRemove.addAll(appWorkIds);
        Iterator<AppWorkId> iterator = pendingAppWorksToRemove.iterator();
        while (iterator.hasNext()) {
            AppWorkId appWorkId = iterator.next();
            AppWork appWork = context.getAppWorks().get(appWorkId);
            if (appWork == null) {
                iterator.remove();
            } else if (appWork.getAppWorkState().equals(AppWorkState.DONE)){
                context.getAppWorks().remove(appWorkId);
                removedAppWorks.addAll(appWorkIds);
                iterator.remove();
            }
        }
        if (!removedAppWorks.isEmpty()) {
            log.info("Removed AppWorks from context: ", removedAppWorks);
        }
        pendingAppWorksToRemove.clear();
    }


    private class StatusReporterRunnable implements Runnable {

        @Override
        public void run() {
            while (!shutdown) {
                AgentHeartBeatResponse response = null;
                AgentStatus status = getAgentStatus();
                AgentHeartBeatRequest request =
                        new AgentHeartBeatRequest(status, AgentStatusReporter.this.context.getToken());
                try {
                    response = (AgentHeartBeatResponse) StandaloneClient.CS().read(
                            AgentStatusReporter.this.context.getRemote().getIP(),
                            AgentStatusReporter.this.context.getRemote().getPort(),
                            request);
                    updateToken(response);

                    if (!processActionCommand(response)) {
                        removeOrTrackCompletedAppWorks(response.getAppWorksToBeRemoved());
                        List<AppWorkId> appWorkToBeCleanup = response.getAppWorksToCleanup();
                        if (appWorkToBeCleanup != null) {
                            dispatcher.getEventProcessor().process(new CompletedAppWorksEvent(appWorkToBeCleanup));
                        }

                        List<ApplicationId> appToCleanup = response.getApplicationsToCleanup();
                        if (appToCleanup != null) {
                            dispatcher.getEventProcessor().process(new CompletedAppsEvent(appToCleanup));
                        }

                        List<RemoteAppWork> appWorkToUpdate = response.getAppWorksToUpdate();
                        if (appWorkToUpdate != null) {
                            dispatcher.getEventProcessor().process(new UpdateAppWorksEvent(appWorkToUpdate));
                        }
                    }
                } catch (Exception e) {
                    log.error("Agent Heartbeat send error");
                }

                try {
                    Thread.sleep(context.getConfiguration().getAgentSendHeartBeatPeriod());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void updateToken(AgentHeartBeatResponse response) {
            String token = response.getMasterToken();
            if (token != null) {
                AgentStatusReporter.this.context.setToken(token);
            }
        }
    }
}
