package DolphinMaster.agentmanage;

import agent.message.AgentHeartBeatRequest;
import agent.message.AgentHeartBeatResponse;
import agent.message.RegisterAgentRequest;
import agent.message.RegisterAgentResponse;
import common.exception.AgentException;
import common.exception.DolphinException;

public interface AgentTracker {
    RegisterAgentResponse registerAgentManager(RegisterAgentRequest request) throws DolphinException;

    void unregisterAgentManager() throws DolphinException;

    AgentHeartBeatResponse agentHeartBeat(AgentHeartBeatRequest request);
}
