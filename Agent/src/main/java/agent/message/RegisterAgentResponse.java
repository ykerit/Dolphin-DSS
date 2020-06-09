package agent.message;

import agent.status.AgentAction;
import common.struct.AgentId;
import api.MessageID;
import org.greatfree.message.ServerMessage;

public class RegisterAgentResponse extends ServerMessage {
    private String token;
    private AgentAction action;
    private String tips;

    public RegisterAgentResponse() {
        super(MessageID.REGISTER_AGENT_RESPONSE);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public AgentAction getAction() {
        return action;
    }

    public void setAction(AgentAction action) {
        this.action = action;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
