package DolphinMaster.app.event;

import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import common.struct.AgentId;
import common.struct.ApplicationId;

public class AppRunningOnNodeEvent extends AppEvent {
    private final AgentId agentId;
    public AppRunningOnNodeEvent(ApplicationId appId, AgentId agentId) {
        super(appId, AppEventType.APP_RUNNING_ON_NODE);
        this.agentId = agentId;
    }

    public AgentId getAgentId() {
        return agentId;
    }
}

