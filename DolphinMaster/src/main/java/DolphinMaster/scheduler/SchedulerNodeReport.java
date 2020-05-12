package DolphinMaster.scheduler;

import common.resource.Resource;

public class SchedulerNodeReport {
    private final Resource used;
    private final Resource avail;
    private final int num;

    public SchedulerNodeReport(SchedulerNode node) {
        this.used = node.getAllocatedResource();
        this.avail = node.getUnAllocatedResource();
        this.num = node.getNumAppWorks();
    }

    public Resource getUsedResource() {
        return used;
    }

    public Resource getAvailableResource() {
        return avail;
    }

    public int getNumAppWorks() {
        return num;
    }
}
