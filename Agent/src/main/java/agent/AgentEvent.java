package agent;

import common.event.AbstractEvent;

public class AgentEvent extends AbstractEvent<AgentEventType> {
    public AgentEvent(AgentEventType agentEventType) {
        super(agentEventType);
    }
}
