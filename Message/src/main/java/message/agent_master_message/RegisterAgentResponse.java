package message.agent_master_message;

import common.struct.AgentID;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class RegisterAgentResponse extends ServerMessage {
    private AgentID agentID;
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public RegisterAgentResponse(AgentID agentID, String token) {
        super(MessageID.REGISTER_AGENT_RESPONSE);
        this.agentID = agentID;
        this.token = token;
    }

    public AgentID getAgentID() {
        return agentID;
    }
}
