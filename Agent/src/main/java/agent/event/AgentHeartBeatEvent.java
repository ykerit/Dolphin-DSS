package agent.event;

import common.event.AbstractEvent;
import message.agent_master_message.HeartBeatRequest;
import org.greatfree.util.IPAddress;

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
