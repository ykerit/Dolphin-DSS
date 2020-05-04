package agent.message;

import common.struct.AgentId;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class RegisterAgentResponse extends ServerMessage {
    private AgentId agentId;
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public RegisterAgentResponse(AgentId agentID, String token) {
        super(MessageID.REGISTER_AGENT_RESPONSE);
        this.agentId = agentID;
        this.token = token;
    }

    public AgentId getAgentId() {
        return agentId;
    }
}
