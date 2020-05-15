package common.struct;

import common.resource.Resource;

import java.io.Serializable;
import java.util.Set;

public class RemoteAppWork implements Comparable<RemoteAppWork>, Serializable {
    private AppWorkId appWorkId;
    private AgentId agentId;
    private Resource resource;
    private Priority priority;
    private Set<String> allocationTags;

    public RemoteAppWork(AppWorkId appWorkId, AgentId agentId, Resource resource, Priority priority, Set<String> allocationTags) {
        this.appWorkId = appWorkId;
        this.agentId = agentId;
        this.resource = resource;
        this.priority = priority;
        this.allocationTags = allocationTags;
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public void setAppWorkId(AppWorkId appWorkId) {
        this.appWorkId = appWorkId;
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Set<String> getAllocationTags() {
        return allocationTags;
    }

    public void setAllocationTags(Set<String> allocationTags) {
        this.allocationTags = allocationTags;
    }

    @Override
    public String toString() {
        return "RemoteAppWork[" +
                "appWorkId=" + appWorkId +
                ", agentId=" + agentId +
                ", resource=" + resource +
                ", priority=" + priority +
                ", allocationTags=" + allocationTags +
                ']';
    }

    @Override
    public int compareTo(RemoteAppWork o) {
        if (this.getAppWorkId().compareTo(o.getAppWorkId()) == 0) {
            if (this.getAgentId().compareTo(o.getAgentId()) == 0) {
                return this.getResource().compareTo(o.getResource());
            } else {
                return this.getAgentId().compareTo(o.getAgentId());
            }
        } else {
            return this.getAgentId().compareTo(o.getAgentId());
        }
    }
}
