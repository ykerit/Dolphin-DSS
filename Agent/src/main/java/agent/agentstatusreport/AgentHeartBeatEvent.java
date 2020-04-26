package agent.agentstatusreport;

import common.event.AbstractEvent;
import message.agent_master_message.HeartBeatRequest;

public class AgentHeartBeatEvent extends AbstractEvent<HeartBeatEventType> {
    private HeartBeatRequest heartBeatRequest;
    public AgentHeartBeatEvent(HeartBeatEventType heartBeatEventType, HeartBeatRequest request) {
        super(heartBeatEventType);
        this.heartBeatRequest = request;
    }

    public HeartBeatRequest getHeartBeatRequest() {
        return heartBeatRequest;
    }
}
