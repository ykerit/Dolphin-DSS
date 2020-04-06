package message.agent_master_message;

import message.MessageID;
import org.greatfree.message.container.Request;

public class HeartBeatRequest extends Request {
    private String token;
    private long agentID;
    public HeartBeatRequest(long agentID, String token) {
        super(MessageID.HEART_BEAT_REQUEST);
        this.agentID = agentID;
        this.token = token;
    }

    public long getAgentID() {
        return agentID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
