package DolphinMaster.agentmanage;

import agent.message.AgentHeartBeatRequest;
import agent.message.AgentHeartBeatResponse;
import agent.message.RegisterAgentRequest;
import agent.message.RegisterAgentResponse;
import common.exception.AgentException;

public interface AgentTracker {
    RegisterAgentResponse registerAgentManage(RegisterAgentRequest request) throws AgentException;

    AgentHeartBeatResponse agentHeartBeat(AgentHeartBeatRequest request);
}
