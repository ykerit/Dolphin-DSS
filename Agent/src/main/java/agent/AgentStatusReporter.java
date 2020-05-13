package agent;

import agent.application.Application;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import agent.application.ApplicationState;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkState;
import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.message.*;
import agent.status.AgentAction;
import agent.status.AgentStatus;
import agent.status.AppWorkStatus;
import common.event.EventDispatcher;
import common.exception.DolphinException;
import common.exception.DolphinRuntimeException;
import common.resource.Resource;
import common.resource.ResourceCollector;
import common.resource.ResourceUtilization;
import common.service.AbstractService;
import common.struct.AgentId;
import common.util.HardwareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.util.IPAddress;
import org.greatfree.util.Tools;

import java.io.IOException;
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
    private final Map<String, Long> recentlyStoppedAppWork;
    private final Map<String, AppWorkState> pendingCompleteAppWork;

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
        this.agentId = new AgentId(Tools.getLocalIP());
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
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

    protected void registerWithDolphinMaster() throws IOException, DolphinException, RemoteReadException, ClassNotFoundException {
        RegisterAgentResponse registerResponse = null;

        synchronized (this.context) {
            RegisterAgentRequest request =
                    new RegisterAgentRequest(agentId,
                            totalResource,
                            physicalResource,
                            getAppWorkStatuses(),
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

        agentId = registerResponse.getAgentId();
        this.context.setAgentId(agentId);
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

    protected List<AppWorkStatus> getAppWorkStatuses() {
        List<AppWorkStatus> statuses = new ArrayList<>();

        for (AppWork appWork : this.context.getAppWorks().values()) {
            AppWorkId appWorkId = appWork.();
            ApplicationId applicationId = appWork.getAppId();
            AppWorkStatus appWorkStatus = appWork.cloneAndGetAppWorkStatus();
            if (appWorkStatus.getState() == AppWorkState.DONE) {

            }
        }

        return statuses;
    }

    protected AgentStatus getAgentStatus() {
        List<AppWorkStatus> appWorkStatuses = getAppWorkStatuses();
        ResourceUtilization agentUtilization = getAgentUtilization();
//        ResourceUtilization appWorkUtilization = getAppWorkUtilization();
        AgentStatus status = new AgentStatus(agentId,
                appWorkStatuses, createKeepAliveApplicationList(),
                null, agentUtilization);
        return status;
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

                    }
                } catch (Exception e) {
                    log.error("Agent Heartbeat send error");
                }

                try {
                    Thread.sleep(3000L);
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
