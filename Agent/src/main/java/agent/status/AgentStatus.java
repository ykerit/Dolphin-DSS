package agent.status;

import common.struct.ApplicationId;
import common.resource.ResourceUtilization;
import common.struct.AgentId;

import java.io.Serializable;
import java.util.List;

public class AgentStatus implements Serializable {
    private final AgentId agentId;
    private final List<AppWorkStatus> appWorkStatuses;
    private final List<ApplicationId> keepAliveApplications;
    private final ResourceUtilization appWorkResourceUtilization;
    private final ResourceUtilization agentResourceUtilization;

    public AgentStatus(AgentId agentId,
                       List<AppWorkStatus> appWorkStatuses,
                       List<ApplicationId> keepAliveApplications,
                       ResourceUtilization appWorkResourceUtilization,
                       ResourceUtilization agentResourceUtilization) {
        this.agentId = agentId;
        this.appWorkStatuses = appWorkStatuses;
        this.keepAliveApplications = keepAliveApplications;
        this.appWorkResourceUtilization = appWorkResourceUtilization;
        this.agentResourceUtilization = agentResourceUtilization;
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public List<ApplicationId> getKeepAliveApplications() {
        return keepAliveApplications;
    }

    public ResourceUtilization getAppWorkResourceUtilization() {
        return appWorkResourceUtilization;
    }

    public ResourceUtilization getAgentResourceUtilization() {
        return agentResourceUtilization;
    }

    public List<AppWorkStatus> getAppWorkStatuses() {
        return appWorkStatuses;
    }
}
