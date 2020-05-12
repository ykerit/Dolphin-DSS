package DolphinMaster.node;

import common.struct.ApplicationId;
import agent.status.AppWorkStatus;
import common.struct.AgentId;

import java.util.List;

public class AgentStartedEvent extends NodeEvent{
    private List<AppWorkStatus> appWorkStatuses;
    private List<ApplicationId> runningApplications;

    public AgentStartedEvent(AgentId id, List<AppWorkStatus> appWorkStatuses,
                             List<ApplicationId> runningApplications) {
        super(id, NodeEventType.STARTED);
        this.appWorkStatuses = appWorkStatuses;
        this.runningApplications = runningApplications;
    }

    public List<ApplicationId> getRunningApplications() {
        return runningApplications;
    }

    public List<AppWorkStatus> getAppWorkStatuses() {
        return appWorkStatuses;
    }
}
