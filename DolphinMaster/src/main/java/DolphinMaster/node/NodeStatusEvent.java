package DolphinMaster.node;

import agent.status.AgentStatus;
import common.resource.ResourceUtilization;
import common.struct.AgentId;
import common.struct.AppWorkStatus;
import common.struct.ApplicationId;

import java.util.List;

public class NodeStatusEvent extends NodeEvent {
    private final AgentStatus nodeStatus;

    public NodeStatusEvent(AgentId id, AgentStatus status) {
        super(id, NodeEventType.STATUS_UPDATE);
        this.nodeStatus = status;
    }

    public List<AppWorkStatus> getAppWorks() {
        return nodeStatus.getAppWorkStatuses();
    }

    public List<ApplicationId> getKeepAliveAppIds() {
        return nodeStatus.getKeepAliveApplications();
    }

    public ResourceUtilization getAppWorkUtilization() {
        return nodeStatus.getAppWorkResourceUtilization();
    }

    public ResourceUtilization getNodeUtilization() {
        return nodeStatus.getAgentResourceUtilization();
    }
}
