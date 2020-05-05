package DolphinMaster.agentmanage;

import DolphinMaster.DolphinContext;
import DolphinMaster.servertask.NodeTask;
import agent.message.AgentHeartBeatRequest;
import agent.message.AgentHeartBeatResponse;
import agent.message.RegisterAgentRequest;
import agent.message.RegisterAgentResponse;
import agent.status.AgentAction;
import common.event.ActionType;
import common.exception.AgentException;
import common.service.AbstractService;
import common.struct.AgentId;
import common.util.SnowFlakeGenerator;
import config.DefaultServerConfig;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.server.container.ServerContainer;


import java.io.IOException;

public class AgentTrackerService extends AbstractService implements AgentTracker {

    private ServerContainer server;
    private final DolphinContext dolphinContext;
    private final AgentLivelinessMonitor livelinessMonitor;
    private final AgentListManage listManage;

    public AgentTrackerService(DolphinContext dolphinContext,
                               AgentLivelinessMonitor livelinessMonitor,
                               AgentListManage agentListManage) {
        super(AgentTrackerService.class.getName());
        this.dolphinContext = dolphinContext;
        this.livelinessMonitor = livelinessMonitor;
        this.listManage = agentListManage;
    }

    @Override
    protected void serviceInit() throws Exception {
        try {
            this.server = new ServerContainer(DefaultServerConfig.NODE_TRACKER_PORT, new NodeTask(this.dolphinContext));
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        try {
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        try {
            this.server.stop(2000L);
        } catch (ClassNotFoundException | IOException | InterruptedException | RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }

    @Override
    public RegisterAgentResponse registerAgentManage(RegisterAgentRequest request) throws AgentException {
        AgentId agentId = request.getAgentId();
        if (agentId != null) {
            long id = SnowFlakeGenerator.GEN().nextId();
            agentId.setAgentKey(id);
            listManage.addInclude(id, agentId);
            return new RegisterAgentResponse(agentId, dolphinContext
                    .getSecurityManage()
                    .genToken("agent" + id, getName(), 1000L * 60));
        } else {
            throw new AgentException("Register agentId is null");
        }
    }

    @Override
    public AgentHeartBeatResponse agentHeartBeat(AgentHeartBeatRequest request) {
        if (dolphinContext.getSecurityManage().checkExpire(request.getToken())) {
            return new AgentHeartBeatResponse(AgentAction.NORMAL,
                    dolphinContext
                            .getSecurityManage()
                            .genToken("agent" + request.getAgentStatus().getAgentId(), getName(), 1000L * 60));
        }
        this.livelinessMonitor.addMonitored(request.getAgentStatus().getAgentId().getAgentKey());
        return new AgentHeartBeatResponse(AgentAction.NORMAL, null);
    }
}
