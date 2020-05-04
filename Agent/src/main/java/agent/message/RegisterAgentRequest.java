package agent.message;

import common.resource.Resource;
import common.struct.AgentId;
import message.MessageID;
import org.greatfree.message.container.Request;

public class RegisterAgentRequest extends Request {
    private AgentId agentId;
    private Resource resource;
    private Resource physicalResource;

    public RegisterAgentRequest(AgentId id, Resource resource, Resource physicalResource) {
        super(MessageID.REGISTER_AGENT_REQUEST);
        this.agentId = id;
        this.resource = resource;
        this.physicalResource = physicalResource;
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
}
