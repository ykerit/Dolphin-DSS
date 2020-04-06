package DolphinMaster.agent_manage;

import DolphinMaster.DolphinContext;
import common.event.ActionType;
import common.struct.AgentID;
import common.util.SnowFlakeGenerator;
import config.ServerConfig;
import DolphinMaster.server_task.NodeTask;
import common.service.AbstractService;
import message.agent_master_message.HeartBeatRequest;
import message.agent_master_message.HeartBeatResponse;
import message.agent_master_message.RegisterAgentRequest;
import message.agent_master_message.RegisterAgentResponse;
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
    protected void serviceInit() {
        try {
            this.server = new ServerContainer(ServerConfig.NODE_TRACKER_PORT, new NodeTask(this.dolphinContext));
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
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
    protected void serviceStop() {
        try {
            this.server.stop(2000L);
        } catch (ClassNotFoundException | IOException | InterruptedException | RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }

    @Override
    public RegisterAgentResponse registerAgentManage(RegisterAgentRequest request) {
        AgentID agentID = request.getHost();
        long id = SnowFlakeGenerator.GEN().nextId();
        agentID.setAgentKey(id);
        listManage.addInclude(id, agentID);
        return new RegisterAgentResponse(agentID, dolphinContext
                .getSecurityManage()
                .genToken("agent"+id, getName(), 1000L * 60));
    }

    @Override
    public HeartBeatResponse agentHeartBeat(HeartBeatRequest request) {
        if (dolphinContext.getSecurityManage().checkExpire(request.getToken())) {
            return new HeartBeatResponse(ActionType.EXPIRE_TOKEN,
                    dolphinContext
                            .getSecurityManage()
                            .genToken("agent" + request.getAgentID(), getName(), 1000L * 60));
        }
        this.livelinessMonitor.addMonitored(request.getAgentID());
        return new HeartBeatResponse(ActionType.NONE, null);
    }
}
