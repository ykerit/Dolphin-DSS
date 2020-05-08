package agent.message;

import agent.status.AgentAction;
import common.struct.AgentId;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class RegisterAgentResponse extends ServerMessage {
    private AgentId agentId;
    private String token;
    private AgentAction action;
    private String tips;

    public RegisterAgentResponse() {
        super(MessageID.REGISTER_AGENT_RESPONSE);
    }

    public RegisterAgentResponse(AgentId agentID, String token) {
        super(MessageID.REGISTER_AGENT_RESPONSE);
        this.agentId = agentID;
        this.token = token;
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
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
