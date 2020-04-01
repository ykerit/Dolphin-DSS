package message.agent_master_message;

import common.struct.AgentID;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class RegisterAgentResponse extends ServerMessage {
    private AgentID agentID;
    public RegisterAgentResponse(AgentID agentID) {
        super(MessageID.REGISTER_AGENT_RESPONSE);
        this.agentID = agentID;
    }

    public AgentID getAgentID() {
        return agentID;
    }

    public void setAgentID(AgentID agentID) {
        this.agentID = agentID;
    }
}
