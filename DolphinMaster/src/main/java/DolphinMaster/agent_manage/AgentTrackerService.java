package DolphinMaster.agent_manage;

import DolphinMaster.DolphinContext;
import common.struct.AgentID;
import common.util.LivelinessMonitor;
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }

    @Override
    public RegisterAgentResponse registerAgentManage(RegisterAgentRequest request) {
        AgentID agentID = request.getHost();
        agentID.setAgentKey(SnowFlakeGenerator.GEN().nextId());
        return new RegisterAgentResponse(agentID);
    }

    @Override
    public HeartBeatResponse agentHeartBeat(HeartBeatRequest request) {
        this.livelinessMonitor.addMonitored(request.getAgentID());
        return new HeartBeatResponse();
    }
}
