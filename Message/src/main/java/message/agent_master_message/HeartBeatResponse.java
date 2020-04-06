package message.agent_master_message;

import common.event.ActionType;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class HeartBeatResponse extends ServerMessage {
    private ActionType action;
    private Object data;

    public HeartBeatResponse(ActionType action, Object object) {
        super(MessageID.HEART_BEAT_RESPONSE);
        this.action = action;
        this.data = object;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
