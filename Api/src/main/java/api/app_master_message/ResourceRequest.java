package api.app_master_message;

import common.resource.Resource;
import common.struct.Priority;

import java.io.Serializable;
import java.util.Objects;

public class ResourceRequest implements Serializable, Comparable<ResourceRequest> {
    private Priority priority;
    private Resource capability;
    private int numAppWorks;

    public Priority getPriority() {
        return priority;
    }

    public Resource getCapability() {
        return capability;
    }

    public void setCapability(Resource capability) {
        this.capability = capability;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getNumAppWorks() {
        return numAppWorks;
    }

    public void setNumAppWorks(int numAppWorks) {
        this.numAppWorks = numAppWorks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceRequest that = (ResourceRequest) o;
        return numAppWorks == that.numAppWorks &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(capability, that.capability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, capability, numAppWorks);
    }

    @Override
    public int compareTo(ResourceRequest o) {
        return 0;
    }
}
