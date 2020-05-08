package agent.message;

import agent.status.AgentAction;
import common.event.ActionType;
import message.MessageID;
import org.greatfree.message.ServerMessage;

public class AgentHeartBeatResponse extends ServerMessage {
    private AgentAction action;
    private String masterToken;
    private String tips;

    public AgentHeartBeatResponse() {
        super(MessageID.HEART_BEAT_RESPONSE);
    }

    public AgentHeartBeatResponse(AgentAction action, String masterToken) {
        super(MessageID.HEART_BEAT_RESPONSE);
        this.action = action;
        this.masterToken = masterToken;
    }

    public AgentAction getAction() {
        return action;
    }

    public void setAction(AgentAction action) {
        this.action = action;
    }

    public String getMasterToken() {
        return masterToken;
    }

    public void setMasterToken(String masterToken) {
        this.masterToken = masterToken;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
