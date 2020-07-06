package DolphinMaster.agentmanage;

import DolphinMaster.DolphinContext;
import DolphinMaster.node.*;
import DolphinMaster.security.SecurityManager;
import DolphinMaster.servertask.AgentTask;
import agent.message.AgentHeartBeatRequest;
import agent.message.AgentHeartBeatResponse;
import agent.message.RegisterAgentRequest;
import agent.message.RegisterAgentResponse;
import agent.status.AgentAction;
import agent.status.AgentStatus;
import common.exception.DolphinException;
import common.exception.DolphinRuntimeException;
import common.resource.Resource;
import common.service.AbstractService;
import common.struct.AgentId;
import config.DefaultServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.server.container.ServerContainer;

import java.io.IOException;

public class AgentTrackerService extends AbstractService implements AgentTracker {
    private static final Logger log = LogManager.getLogger(AgentTrackerService.class.getName());

    private ServerContainer server;
    private final DolphinContext dolphinContext;
    private final AgentLivelinessMonitor livelinessMonitor;
    private final AgentListManage listManage;
    private final SecurityManager securityManager;

    private int minAllocMB;
    private int minAllocVCores;

    public AgentTrackerService(DolphinContext dolphinContext,
                               AgentLivelinessMonitor livelinessMonitor,
                               AgentListManage agentListManage,
                               SecurityManager securityManager) {
        super(AgentTrackerService.class.getName());
        this.dolphinContext = dolphinContext;
        this.livelinessMonitor = livelinessMonitor;
        this.listManage = agentListManage;
        this.securityManager = securityManager;
    }

    @Override
    protected void serviceInit() throws Exception {
        try {
            this.server = new ServerContainer(DefaultServerConfig.NODE_TRACKER_PORT, new AgentTask(this));
        } catch (IOException e) {
            throw new DolphinRuntimeException("Failed to create server container", e);
        }
        minAllocMB = 1000;
        minAllocVCores = 1;

        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        this.server.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        this.server.stop(2000L);
        super.serviceStop();
    }

    @Override
    public RegisterAgentResponse registerAgentManager(RegisterAgentRequest request) throws DolphinException {
        AgentId agentId = request.getAgentId();
        String hostname = agentId.getHostname();
        Resource capability = request.getResource();
        Resource physicalResource = request.getPhysicalResource();

        RegisterAgentResponse response = new RegisterAgentResponse();
        if (capability.getMemorySize() < minAllocMB) {
            String message = "Available resource of node is less than the minimum allocated resource unit：" +
                    "Node resource is: " + capability + "; minAllocMB is: " + minAllocMB + "mb and minVCores is: "
                    + minAllocVCores;
            log.info(message);
            response.setAction(AgentAction.SHUTDOWN);
            response.setTips(message);
            return response;
        }
        String masterToken = securityManager.genToken(agentId.toString(), getName());
        response.setToken(masterToken);
        if (!isValidNode(agentId)) {
            String errMsg = "Not allowed AgentManager AgentId: " +
                    agentId +
                    ", hostname: " +
                    agentId.getHostname();
            log.info(errMsg);
            response.setAction(AgentAction.SHUTDOWN);
            response.setTips(errMsg);
            return response;
        }
        Node node = new NodeImp(agentId, dolphinContext,
                hostname, agentId.getCommandPort(),
                capability, physicalResource);
        Node oldNode = this.dolphinContext.getNodes().putIfAbsent(agentId, node);
        if (oldNode == null) {
            AgentStartedEvent startedEvent = new AgentStartedEvent(agentId,
                    request.getAppWorkStatuses(),
                    request.getRunningApplications());
            this.dolphinContext.getDolphinDispatcher().getEventProcessor().process(startedEvent);
        } else {
            // nodes is exist agent, so now agent is reconnect
            this.livelinessMonitor.removeMonitored(agentId);
            log.info("Reconnect the node is {}", agentId);
            if (request.getRunningApplications().isEmpty() && node.getState() != NodeState.DECOMMISSIONED) {
                switch (node.getState()) {
                    case RUNNING:
                        break;
                    case UNHEALTHY:
                        break;
                }
                this.dolphinContext.getNodes().put(agentId, node);
                this.dolphinContext.getDolphinDispatcher()
                        .getEventProcessor().process(new AgentStartedEvent(agentId, null, null));
            } else {

            }
        }
        this.livelinessMonitor.addMonitored(agentId);
        StringBuilder message = new StringBuilder();
        message.append("NodeManager from node ").append(hostname)
                .append("registered with capability: ").append(capability);
        message.append(", assigned agentId ").append(agentId);
        log.info(message.toString());
        response.setAction(AgentAction.NORMAL);
        return response;
    }

    @Override
    public void unregisterAgentManager() throws DolphinException {

    }

    @Override
    public AgentHeartBeatResponse agentHeartBeat(AgentHeartBeatRequest request) {
        AgentStatus status = request.getAgentStatus();
        AgentId agentId = status.getAgentId();

        AgentHeartBeatResponse response = new AgentHeartBeatResponse();
//        if (!isValidNode(agentId)) {
//            String errMsg = "Not allowed AgentManager AgentId: " + agentId + ", hostname: " + agentId.getHostname();
//            log.info(errMsg);
//            response.setAction(AgentAction.SHUTDOWN);
//            response.setTips(errMsg);
//            return response;
//        }

        AgentStatusEvent statusEvent = new AgentStatusEvent(agentId, status);
        this.dolphinContext.getDolphinDispatcher().getEventProcessor().process(statusEvent);

        Node node = dolphinContext.getNodes().get(agentId);
        if (node == null) {
            String message = "Node not found re syncing" + agentId;
            response.setTips(message);
            response.setAction(AgentAction.RSYNC);
            return response;
        }
        populate(request, response);
        node.setAndUpdateAgentHeartbeatResponse(response);
        return response;
    }

    private boolean isValidNode(AgentId id) {
        return !isNodeInDecommissioning(id);
    }

    private boolean isNodeInDecommissioning(AgentId agentId) {
        Node node = this.dolphinContext.getNodes().get(agentId);
        if (node != null) {
            NodeState state = node.getState();
            if (state == NodeState.DECOMMISSIONED || state == NodeState.RUNNING) {
                return true;
            }
        }
        return false;
    }

    private void populate(AgentHeartBeatRequest request, AgentHeartBeatResponse response) {
        AgentId id = request.getAgentStatus().getAgentId();
        String nextKey = securityManager.genToken(id.toString(), getName());
        if (securityManager.checkExpire(request.getToken())) {
            response.setMasterToken(nextKey);
        }
    }
}
