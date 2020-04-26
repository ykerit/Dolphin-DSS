package DolphinMaster.agentmanage;

import message.agent_master_message.HeartBeatRequest;
import message.agent_master_message.HeartBeatResponse;
import message.agent_master_message.RegisterAgentRequest;
import message.agent_master_message.RegisterAgentResponse;

public interface AgentTracker {
    RegisterAgentResponse registerAgentManage(RegisterAgentRequest request);

    HeartBeatResponse agentHeartBeat(HeartBeatRequest request);
}
