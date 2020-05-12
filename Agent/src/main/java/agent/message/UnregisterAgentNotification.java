package agent.message;

import common.struct.AgentId;
import api.MessageID;
import org.greatfree.message.container.Notification;

public class UnregisterAgentNotification extends Notification {
    AgentId agentId;
    public UnregisterAgentNotification(AgentId id) {
        super(MessageID.UNREGISTER_AGENT_NOTIFICATION);
        this.agentId = id;
    }

    public AgentId getAgentId() {
        return agentId;
    }
}
