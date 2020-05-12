package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.node.Node;
import DolphinMaster.schedulerunit.SchedulerUnit;
import agent.appworkmanage.appwork.AppWork;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.resource.Resources;
import common.struct.AgentId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

// In a scheduler, a node is abstracted as a scheduling node
public abstract class SchedulerNode {
    private static final Logger log = LogManager.getLogger(SchedulerNode.class);

    private Resource unAllocatedResource = Resource.newInstance(0, 0);
    private Resource allocatedResource = Resource.newInstance(0, 0);
    private Resource totalResource;
    private SchedulerUnit reservedUnit;
    private volatile int numAppWorks;
    private volatile ResourceUtilization appWorksUtilization = ResourceUtilization.newInstance(0, 0);
    private volatile ResourceUtilization nodeUtilization = ResourceUtilization.newInstance(0, 0);
    private long overcommitTimeout = -1;

    private final Map<String, SchedulerUnitInfo> launchedAppWorks = new HashMap<>();

    private final Node node;
    private final String nodeName;
    private final DolphinContext context;

    public SchedulerNode(Node node) {
        this.node = node;
        this.context = node.getContext();
        this.unAllocatedResource = Resources.clone(node.getTotalCapability());
        this.totalResource = Resources.clone(node.getTotalCapability());
        nodeName = node.getNodeName();
    }

    public Node getNode() {
        return node;
    }

    public synchronized void updateTotalResource(Resource resource) {
        this.totalResource = resource;
        this.unAllocatedResource = Resources.subtract(totalResource, this.allocatedResource);
    }

    public synchronized void setOvercommitTimeout(long timeout) {
        overcommitTimeout = timeout;
    }

    public synchronized boolean isOverCommitTimeout() {
        return overcommitTimeout >= 0 && System.currentTimeMillis() >= overcommitTimeout;
    }

    public synchronized boolean isOverCommitTimeoutSet() {
        return overcommitTimeout >= 0;
    }

    public AgentId getNodeId() {
        return node.getNodeId();
    }

    public String getNodeName() {
        return node.getNodeName();
    }

    public String getRackName() {
        return node.getRackName();
    }

    public void allocateSchedulerUnit(SchedulerUnit unit) {
        allocateSchedulerUnit(unit, false);
    }

    protected synchronized void allocateSchedulerUnit(SchedulerUnit unit, boolean launchedOnNode) {
        numAppWorks++;
        AppWork appWork = unit.getAppWork();
        launchedAppWorks.put(appWork.getAppWorkId(), new SchedulerUnitInfo(unit, launchedOnNode));
    }

    public synchronized Resource getAllocatedResource() {
        return this.allocatedResource;
    }

    public synchronized Resource getUnAllocatedResource() {
        return this.unAllocatedResource;
    }

    public synchronized Resource getTotalResource() {
        return this.totalResource;
    }

    public synchronized boolean isValidAppWork(String appWorkId) {
        if (launchedAppWorks.containsKey(appWorkId)) {
            return true;
        }
        return false;
    }

    protected synchronized void updateResourceForReleasedContainer(
            AppWork appWork) {
        addUnallocatedResource(appWork.getResource());
        --numAppWorks;
    }

    public synchronized void releaseSchedulerUnit(String appWorkId,
                                              boolean releasedByNode) {
        SchedulerUnitInfo info = launchedAppWorks.get(appWorkId);
        if (info == null) {
            return;
        }
        if (!releasedByNode && info.launchedOnNode) {
            // wait until node reports container has completed
            return;
        }

        launchedAppWorks.remove(appWorkId);
        AppWork appWork = info.schedulerUnit.getAppWork();

        // We remove allocation tags when a container is actually
        // released on NM. This is to avoid running into situation
        // when AM releases a container and NM has some delay to
        // actually release it, then the tag can still be visible
        // at RM so that RM can respect it during scheduling new containers.

        updateResourceForReleasedContainer(appWork);

        if (log.isDebugEnabled()) {
            log.debug("Released AppSchedulerUnit " + appWork + " of capacity "
                    + appWork.getResource() + " on host " + node.getNodeAddress()
                    + ", which currently has " + numAppWorks + " containers, "
                    + getAllocatedResource() + " used and " + getUnAllocatedResource()
                    + " available" + ", release resources=" + true);
        }
    }

    public synchronized void appWorkStarted(String appWorkId) {
        SchedulerUnitInfo info = launchedAppWorks.get(appWorkId);
        if (info != null) {
            info.launchedOnNode = true;
        }
    }


    private synchronized void addUnallocatedResource(Resource resource) {
        if (resource == null) {
            log.error("Invalid resource addition of null resource for "
                    + node.getNodeAddress());
            return;
        }
        Resources.addTo(unAllocatedResource, resource);
        Resources.subtractFrom(allocatedResource, resource);
    }

    public synchronized void deductUnallocatedResource(Resource resource) {
        if (resource == null) {
            log.error("Invalid deduction of null resource for "
                    + node.getNodeAddress());
            return;
        }
        Resources.subtractFrom(unAllocatedResource, resource);
        Resources.addTo(allocatedResource, resource);
    }

    public abstract void reserveResource(AppDescribe attempt, SchedulerUnit unit);


    public abstract void unreserveResource(AppDescribe attempt);

    @Override
    public String toString() {
        return "host: " + node.getNodeAddress() + " #AppWorks="
                + getNumAppWorks() + " available=" + getUnAllocatedResource()
                + " used=" + getAllocatedResource();
    }

    public int getNumAppWorks() {
        return numAppWorks;
    }

    public synchronized List<SchedulerUnit> getCopiedListOfRunningSchedulerUnit() {
        List<SchedulerUnit> result = new ArrayList<>(launchedAppWorks.size());
        for (SchedulerUnitInfo info : launchedAppWorks.values()) {
            result.add(info.schedulerUnit);
        }
        return result;
    }

    public synchronized List<SchedulerUnit> getRunningAppWorksWithAMsAtTheEnd() {
        LinkedList<SchedulerUnit> result = new LinkedList<>();
        for (SchedulerUnitInfo info : launchedAppWorks.values()) {
            if (info.schedulerUnit.isAMAppWork()) {
                result.addLast(info.schedulerUnit);
            } else {
                result.addFirst(info.schedulerUnit);
            }
        }
        return result;
    }

    public List<SchedulerUnit> getAppWorksToKill() {
        List<SchedulerUnit> result = getLaunchedSchedulerUnits();
        return result;
    }

    protected synchronized List<SchedulerUnit> getLaunchedSchedulerUnits() {
        List<SchedulerUnit> result = new ArrayList<>();
        for (SchedulerUnitInfo info : launchedAppWorks.values()) {
            result.add(info.schedulerUnit);
        }
        return result;
    }

    protected synchronized SchedulerUnit getSchedulerUnit(String appWorkId) {
        SchedulerUnit schedulerUnit = null;
        SchedulerUnitInfo info = launchedAppWorks.get(appWorkId);
        if (info != null) {
            schedulerUnit = info.schedulerUnit;
        }
        return schedulerUnit;
    }

    public synchronized SchedulerUnit getReservedSchedulerUnit() {
        return reservedUnit;
    }

    public synchronized void setReservedSchedulerUnit(SchedulerUnit reservedSchedulerUnit) {
        this.reservedUnit = reservedSchedulerUnit;
    }

    public void setAggregatedAppWorksUtilization(
            ResourceUtilization containersUtilization) {
        this.appWorksUtilization = containersUtilization;
    }

    public ResourceUtilization getAggregatedAppWorksUtilization() {
        return this.appWorksUtilization;
    }

    public void setNodeUtilization(ResourceUtilization nodeUtilization) {
        this.nodeUtilization = nodeUtilization;
    }

    public ResourceUtilization getNodeUtilization() {
        return this.nodeUtilization;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchedulerNode)) {
            return false;
        }

        SchedulerNode that = (SchedulerNode) o;

        return getNodeId().equals(that.getNodeId());
    }

    @Override
    public int hashCode() {
        return getNodeId().hashCode();
    }

    private static class SchedulerUnitInfo {
        private final SchedulerUnit schedulerUnit;
        private boolean launchedOnNode;

        public SchedulerUnitInfo(SchedulerUnit unit, boolean launchedOnNode) {
            this.schedulerUnit = unit;
            this.launchedOnNode = launchedOnNode;
        }
    }
}
