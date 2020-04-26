package agent.agentstatusreport;

import agent.AgentContext;
import common.resource.ResourceUsage;
import common.service.AbstractService;
import common.struct.AgentID;
import message.agent_master_message.HeartBeatRequest;
import message.agent_master_message.RegisterAgentRequest;
import message.agent_master_message.RegisterAgentResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.util.Tools;

import java.io.IOException;
import java.net.SocketException;

public class AgentStatusPollService extends AbstractService {
    private static final Logger log = LogManager.getLogger(AgentStatusPollService.class.getName());
    private final AgentContext agentContext;
    private AgentID localHost;
    private boolean isRegistered = false;

    // resource usage in startup
    private double cpu;
    private double memory;

    public AgentStatusPollService(AgentContext agentContext) {
        super(AgentStatusPollService.class.getName());
        this.agentContext = agentContext;
    }

    @Override
    protected void serviceInit() {
        try {
            localHost = new AgentID(Tools.getLocalIP(), 9006);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        cpu = ResourceUsage.getCpuUsage();
        memory = ResourceUsage.getMemoryUsage();
        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        registerDolphinMaster();
        if (isRegistered && agentContext.getToken() != null) {
            this.agentContext.getAgentDispatcher().getEventProcessor()
                    .process(new AgentHeartBeatEvent(HeartBeatEventType.T_START,
                            new HeartBeatRequest(localHost.getAgentKey(), agentContext.getToken())));
        }
        super.serviceStart();
    }

    protected void registerDolphinMaster() {
        RegisterAgentRequest registerAgentRequest =
                new RegisterAgentRequest(localHost, cpu, memory);
        RegisterAgentResponse registerAgentResponse = null;
        try {
            registerAgentResponse = (RegisterAgentResponse) StandaloneClient.CS()
                    .read(agentContext.getRemote().getIP(),
                            agentContext.getRemote().getPort(),
                            registerAgentRequest);
        } catch (ClassNotFoundException | RemoteReadException | IOException e) {
            e.printStackTrace();
        }

        String token = registerAgentResponse.getToken();
        agentContext.setToken(token);
        log.info("Node key: {} token; {}", localHost.getAgentKey(), token);
        this.agentContext.setAgentID(localHost);
        isRegistered = true;
    }
}
