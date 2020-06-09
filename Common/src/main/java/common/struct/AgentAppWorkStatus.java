package common.struct;

import common.resource.Resource;

public class AgentAppWorkStatus {
    private AppWorkId appWorkId;
    private RemoteAppWorkState appWorkState;
    private Resource allocatedResource;
    private String tips;
    private int appWorkExitStatus;
    private Priority priority;
    private long creationTime;

    public AgentAppWorkStatus(AppWorkId appWorkId,
                              RemoteAppWorkState appWorkState,
                              Resource allocatedResource,
                              String tips,
                              int appWorkExitStatus,
                              Priority priority,
                              long creationTime) {
        this.appWorkId = appWorkId;
        this.appWorkState = appWorkState;
        this.allocatedResource = allocatedResource;
        this.tips = tips;
        this.appWorkExitStatus = appWorkExitStatus;
        this.priority = priority;
        this.creationTime = creationTime;
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public RemoteAppWorkState getAppWorkState() {
        return appWorkState;
    }

    public Resource getAllocatedResource() {
        return allocatedResource;
    }

    public String getTips() {
        return tips;
    }

    public int getAppWorkExitStatus() {
        return appWorkExitStatus;
    }

    public Priority getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getAppWorkId()).append(", ")
                .append("CreateTime: ").append(getCreationTime()).append(", ")
                .append("State: ").append(getAppWorkState()).append(", ")
                .append("Capability: ").append(getAllocatedResource()).append(", ")
                .append("Tips: ").append(getTips()).append(", ")
                .append("ExitStatus: ").append(getAppWorkExitStatus()).append(", ")
                .append("Priority: ").append(getPriority()).append(", ")
                .append("]");
        return sb.toString();
    }
}
