package agent.message;

import agent.status.AgentStatus;
import message.MessageID;
import org.greatfree.message.container.Request;

public class AgentHeartBeatRequest extends Request {
    private String token;
    private AgentStatus agentStatus;
    public AgentHeartBeatRequest(AgentStatus status, String token) {
        super(MessageID.HEART_BEAT_REQUEST);
        this.agentStatus = status;
        this.token = token;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(AgentStatus agentStatus) {
        this.agentStatus = agentStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
