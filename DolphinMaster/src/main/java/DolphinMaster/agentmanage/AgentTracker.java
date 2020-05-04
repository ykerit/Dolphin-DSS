package DolphinMaster.agentmanage;

import common.exception.AgentException;
import message.agent_master_message.AgentHeartBeatRequest;
import message.agent_master_message.AgentHeartBeatResponse;
import message.agent_master_message.RegisterAgentRequest;
import message.agent_master_message.RegisterAgentResponse;

public interface AgentTracker {
    RegisterAgentResponse registerAgentManage(RegisterAgentRequest request) throws AgentException;

    AgentHeartBeatResponse agentHeartBeat(AgentHeartBeatRequest request);
}
