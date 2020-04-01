package message.agent_master_message;

import message.MessageID;
import org.greatfree.message.ServerMessage;

public class HeartBeatResponse extends ServerMessage {
    public HeartBeatResponse() {
        super(MessageID.HEART_BEAT_RESPONSE);
    }
}
