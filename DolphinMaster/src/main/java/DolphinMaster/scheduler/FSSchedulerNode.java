package DolphinMaster.scheduler;


import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import DolphinMaster.schedulerunit.SchedulerUnit;
import com.google.common.collect.Lists;
import common.resource.Resource;
import common.resource.Resources;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class FSSchedulerNode extends SchedulerNode{
    private static final Logger log =
            LogManager.getLogger(FSSchedulerNode.class);

    final Set<SchedulerUnit> schedulerUnitsForPreemption = new ConcurrentSkipListSet<>();
    final Map<App, Resource> resourcePreemptedForApp = new LinkedHashMap<>();
    private final Map<ApplicationId, App> appIdToAppMap = new HashMap<>();

    private Resource totalResourcesPreempted = Resource.newInstance(0, 0);

    private SchedulerApplication reservedAppSchedulable;

    public FSSchedulerNode(Node node) {
        super(node);
    }

    Resource getTotalReserved() {
        Resource totalReserved = Resources.clone(getReservedSchedulerUnit() != null
                ? getReservedSchedulerUnit().getAllocatedResource()
                : Resource.newInstance(0, 0));
        Resources.addTo(totalReserved, totalResourcesPreempted);
        return totalReserved;
    }

    @Override
    public synchronized void reserveResource(SchedulerApplication application, SchedulerUnit unit) {
        SchedulerUnit reserveSchedulerUnit = getReservedSchedulerUnit();
        if (reserveSchedulerUnit != null) {
            if (!unit.getAppWork().getAgentId().equals(getNodeId())) {
                throw new IllegalStateException("Trying to reserve" +
                        " container " + unit +
                        " on node " + unit.getReservedNode() +
                        " when currently" + " reserved resource " + reserveSchedulerUnit +
                        " on node " + reserveSchedulerUnit.getReservedNode());
            }

            if (!reserveSchedulerUnit.getAppWork().getAppWorkId().equals(unit.getAppWork().getAgentId())) {
                throw new IllegalStateException("Trying to reserve" +
                        " container " + unit +
                        " for application " + application.getApplicationId() +
                        " when currently" +
                        " reserved container " + reserveSchedulerUnit +
                        " on node " + this);
            }
            log.info("Update reserved scheduler unit " + unit.getAppWork().getAppWorkId() +
                    " on node " + this + " for application " + application.getApplicationId());
        }
        setReservedSchedulerUnit(reserveSchedulerUnit);
        this.reservedAppSchedulable = application;
    }

    public SchedulerApplication getReservedAppSchedulable() {
        return reservedAppSchedulable;
    }

    synchronized LinkedHashMap<App, Resource> getPreemptionList() {
        cleanupPreemptionList();
        return new LinkedHashMap<>(resourcePreemptedForApp);
    }

    void cleanupPreemptionList() {
        LinkedList<App> waitForClean;
        synchronized (this) {
            waitForClean = Lists.newLinkedList(this.resourcePreemptedForApp.keySet());
        }

        for (App app : waitForClean) {
            // ...
            if (true) {
                synchronized (this) {
                    Resource removed = resourcePreemptedForApp.remove(app);
                    if (removed != null) {
                        Resources.subtractFrom(totalResourcesPreempted, removed);
                        appIdToAppMap.remove(app);
                    }
                }
            }
        }
    }

    void addSchedulerUnitForPreemption(Collection<SchedulerUnit> schedulerUnits, App app) {
        Resource appReserved = Resources.createResource(0);
        for (SchedulerUnit unit : schedulerUnits) {
            if (schedulerUnitsForPreemption.add(unit)) {
                Resources.addTo(appReserved, unit.getAllocatedResource());
            }
        }

        synchronized (this) {
            if (!Resources.isNone(appReserved)) {
                Resources.addTo(totalResourcesPreempted, appReserved);
                appIdToAppMap.putIfAbsent(app.getApplicationId(), app);
                resourcePreemptedForApp.putIfAbsent(app, Resource.newInstance(0, 0));
                Resources.addTo(resourcePreemptedForApp.get(app), appReserved);
            }
        }
    }

    Set<SchedulerUnit> getSchedulerUnitsForPreemption() {
        return schedulerUnitsForPreemption;
    }

    @Override
    protected synchronized void allocateSchedulerUnit(SchedulerUnit unit, boolean launchedOnNode) {
        super.allocateSchedulerUnit(unit, launchedOnNode);
        if (log.isDebugEnabled()) {
            final RemoteAppWork appWork = unit.getAppWork();
            log.debug("Assigned AppWork " + appWork.getAppWorkId() + " of capacity "
                    + appWork.getResource() + " on host " + getNode().getNodeAddress()
                    + ", which has " + getNumAppWorks() + " SchedulerUnit, "
                    + getAllocatedResource() + " used and " + getUnAllocatedResource()
                    + " available after allocation");
        }

        Resource allocated = unit.getAllocatedResource();
        if (!Resources.isNone(allocated)) {
            // check for satisfied preemption request and update bookkeeping
            App app =
                    appIdToAppMap.get(unit.getApplicationId());
            if (app != null) {
                Resource reserved = resourcePreemptedForApp.get(app);
                Resource fulfilled = Resources.componentwiseMin(reserved, allocated);
                Resources.subtractFrom(reserved, fulfilled);
                Resources.subtractFrom(totalResourcesPreempted, fulfilled);
                if (Resources.isNone(reserved)) {
                    // No more preempted containers
                    resourcePreemptedForApp.remove(app);
                    appIdToAppMap.remove(unit.getApplicationId());
                }
            }
        } else {
            log.error("Allocated empty AppWork" + unit.getAppWorkId());
        }
    }

    @Override
    public synchronized void releaseSchedulerUnit(AppWorkId appWorkId,
                                                  boolean releasedByNode) {
        SchedulerUnit schedulerUnit = getSchedulerUnit(appWorkId);
        super.releaseSchedulerUnit(appWorkId, releasedByNode);
        if (schedulerUnit != null) {
            schedulerUnitsForPreemption.remove(schedulerUnit);
        }
    }

    @Override
    public void unreserveResource(SchedulerApplication application) {

    }


}
