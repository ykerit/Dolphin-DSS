package message.agent_master_message;

import message.MessageID;
import org.greatfree.message.container.Request;

public class HeartBeatRequest extends Request {
    private long agentID;
    public HeartBeatRequest(long agentID) {
        super(MessageID.HEART_BEAT_REQUEST);
    }

    public long getAgentID() {
        return agentID;
    }
}
