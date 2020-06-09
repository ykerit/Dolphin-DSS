package DolphinMaster.node;

import common.struct.AgentAppWorkStatus;
import common.struct.ApplicationId;
import common.struct.AppWorkStatus;
import common.struct.AgentId;

import java.util.List;

public class AgentStartedEvent extends NodeEvent{
    private List<AgentAppWorkStatus> appWorkStatuses;
    private List<ApplicationId> runningApplications;

    public AgentStartedEvent(AgentId id, List<AgentAppWorkStatus> appWorkStatuses,
                             List<ApplicationId> runningApplications) {
        super(id, NodeEventType.STARTED);
        this.appWorkStatuses = appWorkStatuses;
        this.runningApplications = runningApplications;
    }

    public List<ApplicationId> getRunningApplications() {
        return runningApplications;
    }

    public List<AgentAppWorkStatus> getAppWorkStatuses() {
        return appWorkStatuses;
    }
}
