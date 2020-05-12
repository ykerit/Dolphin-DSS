package agent.message;

import common.struct.ApplicationId;
import agent.status.AppWorkStatus;
import common.resource.Resource;
import common.struct.AgentId;
import api.MessageID;
import org.greatfree.message.container.Request;

import java.util.List;

public class RegisterAgentRequest extends Request {
    private AgentId agentId;
    private Resource resource;
    private Resource physicalResource;

    private List<AppWorkStatus> appWorkStatuses;
    private List<ApplicationId> runningApplications;


    public RegisterAgentRequest(AgentId id,
                                Resource resource,
                                Resource physicalResource,
                                List<AppWorkStatus> appWorkStatuses,
                                List<ApplicationId> runningApplications) {
        super(MessageID.REGISTER_AGENT_REQUEST);
        this.agentId = id;
        this.resource = resource;
        this.physicalResource = physicalResource;
        this.appWorkStatuses = appWorkStatuses;
        this.runningApplications = runningApplications;
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public Resource getResource() {
        return resource;
    }

    public Resource getPhysicalResource() {
        return physicalResource;
    }

    public List<AppWorkStatus> getAppWorkStatuses() {
        return appWorkStatuses;
    }

    public List<ApplicationId> getRunningApplications() {
        return runningApplications;
    }
}
