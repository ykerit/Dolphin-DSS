package DolphinMaster.scheduler;

import common.struct.ApplicationId;
import common.resource.Resource;
import common.resource.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LeafResourcePool extends ResourcePoolImp {

    private static final Logger log = LogManager.getLogger(LeafResourcePool.class);

    private static final List<ResourcePoolImp> EMPTY_LIST = Collections.emptyList();

    private FSContext context;

    private final List<AppDescribe> runnableApps = new ArrayList<>();
    private final List<AppDescribe> nonRunnableApps = new ArrayList<>();
    private final Set<ApplicationId> assignedApps = new HashSet<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private Resource demand = Resources.createResource(0);

    private long lastTimeAtMinShare;
    private Resource amResourceUsage;

    public LeafResourcePool(String name, FairScheduler scheduler, ParentResourcePool parentPool) {
        super(name, scheduler, parentPool);
        this.context = scheduler.getContext();
        amResourceUsage = Resource.newInstance(0, 0);
    }

    void addApp(AppDescribe app, boolean runnable) {
        writeLock.lock();
        try {
            if (runnable) {
                runnableApps.add(app);
            } else {
                nonRunnableApps.add(app);
            }
            assignedApps.remove(app.getApplicationId());
            incUsedResource(app.getResourceUsage());
        } finally {
            writeLock.unlock();
        }
    }

    boolean removeApp(AppDescribe app) {
        boolean runnable = false;
        writeLock.lock();
        try {
            runnable = runnableApps.remove(app);
            if (!runnable) {
                if (!removeNonRunnableApp(app)) {
                    throw new IllegalStateException("in resource pool does not exist the app: " + app);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return runnable;
    }

    boolean removeNonRunnableApp(AppDescribe app) {
        writeLock.lock();
        try {
            return nonRunnableApps.remove(app);
        } finally {
            writeLock.unlock();
        }
    }

    boolean isRunnableApp(AppDescribe app) {
        readLock.lock();
        try {
            return runnableApps.contains(app);
        } finally {
            readLock.unlock();
        }
    }

    boolean isNonRunnableApp(AppDescribe app) {
        readLock.lock();
        try {
            return nonRunnableApps.contains(app);
        } finally {
            readLock.unlock();
        }
    }

    List<AppDescribe> getCopyOfNonRunnableAppSchedulable() {
        List<AppDescribe> ret = new ArrayList<>();
        readLock.lock();
        try {
            ret.addAll(nonRunnableApps);
        } finally {
            readLock.unlock();
        }
        return ret;
    }

    @Override
    void updateInterval() {
        readLock.lock();
        try {
            policy.computeShares(runnableApps, getFairShare());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<ResourcePoolImp> getChildPools() {
        return EMPTY_LIST;
    }

    @Override
    public int getNumRunnableApps() {
        readLock.lock();
        try {
            return runnableApps.size();
        } finally {
            readLock.unlock();
        }
    }

    int getNumNonRunnableApps() {
        readLock.lock();
        try {
            return nonRunnableApps.size();
        } finally {
            readLock.unlock();
        }
    }

    public int getNumPendingApps() {
        int numPendingApps = 0;
        readLock.lock();
        try {
            for (AppDescribe app : runnableApps) {
                if (app.isPending()) {
                    numPendingApps++;
                }
            }
            numPendingApps += nonRunnableApps.size();
        } finally {
            readLock.unlock();
        }
        return numPendingApps;
    }

    public int getNumAssignedApps() {
        readLock.lock();
        try {
            return assignedApps.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            if (runnableApps.size() > 0 || nonRunnableApps.size() > 0 || assignedApps.size() > 0) {
                return false;
            }
        } finally {
            readLock.unlock();
        }
        return false;
    }

    @Override
    public void collectSchedulerApplication(Collection<AppDescribeId> apps) {
        readLock.lock();
        try {
            for (AppDescribe app : runnableApps) {
                apps.add(app.getApplicationDescribeId());
            }
            for (AppDescribe app : nonRunnableApps) {
                apps.add(app.getApplicationDescribeId());
            }
        } finally {
            readLock.unlock();
        }
    }

    public int getNumActiveApps() {
        int numActiveApps = 0;
        readLock.lock();
        try {
            for (AppDescribe app : runnableApps) {
                if (!app.isPending()) {
                    numActiveApps++;
                }
            }
        } finally {
            readLock.unlock();
        }
        return numActiveApps;
    }

    @Override
    protected void dumpStateInterval(StringBuilder sb) {
        sb.append("{Name: " + getName() +
                ", Weight: " + weight +
                ", Policy: " + "fifo" +
                ", FairShare: " + getFairShare() +
                ", SteadyFairShare: " + getSteadyFairShare() +
                ", MaxShare: " + getMaxShare() +
                ", MinShare: " + getMinShare() +
                ", ResourceUsage: " + getResourceUsage() +
                ", Demand: " + getDemand() +
                ", Runnable: " + getNumRunnableApps() +
                ", NumPendingApps: " + getNumPendingApps() +
                ", NonRunnable: " + getNumNonRunnableApps() +
                ", MaxAMShare: " + maxAMShare +
                ", MaxAMResource: " + computeMaxAMResource() +
                ", AMResourceUsage: " + getAmResourceUsage() +
                ", LastTimeAtMinShare: " + lastTimeAtMinShare +
                "}");
    }

    @Override
    public Resource getDemand() {
        return demand;
    }

    @Override
    public void updateDemand() {
        Resource tmpDemand = Resources.createResource(0);
        readLock.lock();
        try {
            for (AppDescribe app : runnableApps) {
                app.updateDemand();
                Resources.addTo(tmpDemand, app.getDemand());
            }
            for (AppDescribe app : nonRunnableApps) {
                app.updateDemand();
                Resources.addTo(tmpDemand, app.getDemand());
            }
        } finally {
            readLock.unlock();
        }
        demand = Resources.componentwiseMin(tmpDemand, getMaxShare());
        if (log.isDebugEnabled()) {
            log.debug("The update demand for " + getName() + " is " + demand +
                    "; the max is " + getMaxShare());
            log.debug("the updated fairshare for " + getName() + " is " +
                    getFairShare());
        }
    }

    @Override
    public Resource assignSchedulerUnit(FSSchedulerNode node) {
        Resource assigned = Resources.none();
        if (log.isDebugEnabled()) {
            log.debug("Node " + node.getNodeName() + " offered to pool: " + getName() +
                    " fairShare: " + getFairShare());
        }

        if (!assignSchedulerUnitPreCheck(node)) {
            return assigned;
        }

        for (AppDescribe app : fetchAppsWithDemand(true)) {
            assigned = app.assignSchedulerUnit(node);
            if (!assigned.equals(Resources.none())) {
                log.debug("Assigned SchedulerUnit in pool: {} AppWork: {}", getName(), assigned);
                break;
            }
        }
        return assigned;
    }

    private void setLastTimeAtMinShare(long lastTimeAtMinShare) {
        this.lastTimeAtMinShare = lastTimeAtMinShare;
    }

    Resource getAmResourceUsage() {
        return amResourceUsage;
    }


    private Resource updateStarvedAppsFairShare(TreeSet<AppDescribe> appWithDemand) {
        Resource fairShareStarvation = Resources.clone(Resources.none());
        for (AppDescribe app : appWithDemand) {
            Resource appStarvation = app.fairShareStarvation();
            if (!Resources.isNone(appStarvation)) {
                context.getStarvedApps().addStarvedApp(app);
                Resources.addTo(fairShareStarvation, appStarvation);
            } else {
                break;
            }
        }
        return fairShareStarvation;
    }

    private void updateStarvedAppsMinShare(final TreeSet<AppDescribe> appsWithDemand,
                                           final Resource minShareStarvation) {
        Resource pending = Resources.clone(minShareStarvation);

        for (AppDescribe app : appsWithDemand) {
            if (!Resources.isNone(pending)) {
                Resource appMinShare = app.getPendingDemand();
                Resources.subtractFromNonNegative(appMinShare, app.getFairShareStarvation());
                if (Resources.greaterThan(policy.getResourceCalculator(),
                        scheduler.getClusterResource(), appMinShare, pending)) {
                    Resources.subtractFromNonNegative(pending, appMinShare);
                    pending = Resources.none();
                } else {
                    app.resetMinShareStarvation();
                }
            }
        }
    }

    void updateStarvedApps() {
        TreeSet<AppDescribe> appsWithDemand = fetchAppsWithDemand(false);
        Resource fairShareStarvation = updateStarvedAppsFairShare(appsWithDemand);
        Resource minShareStarvation = minShareStarvation();
        Resources.subtractFromNonNegative(minShareStarvation, fairShareStarvation);
        updateStarvedAppsMinShare(appsWithDemand, minShareStarvation);
    }

    private TreeSet<AppDescribe> fetchAppsWithDemand(boolean assignment) {
        TreeSet<AppDescribe> pendingForResourceApps = new TreeSet<>(policy.getComparator());
        readLock.lock();
        try {
            for (AppDescribe app : runnableApps) {
                if (!Resources.isNone(app.getPendingDemand()) && (assignment || app.shouldCheckForStarvation())) {
                    pendingForResourceApps.add(app);
                }
            }
        } finally {
            readLock.unlock();
        }
        return pendingForResourceApps;
    }

    private Resource minShareStarvation() {
        // If demand < minshare, we should use demand to determine starvation
        Resource starvation =
                Resources.componentwiseMin(getMinShare(), getDemand());

        Resources.subtractFromNonNegative(starvation, getResourceUsage());

        boolean starved = !Resources.isNone(starvation);
        long now = scheduler.getClock().getTime();

        if (!starved) {
            // Record that the queue is not starved
            setLastTimeAtMinShare(now);
        }

        if (now - lastTimeAtMinShare < getMinSharePreemptionTimeout()) {
            // the queue is not starved for the preemption timeout
            starvation = Resources.clone(Resources.none());
        }

        return starvation;
    }

    private Resource computeMaxAMResource() {
        Resource maxResource = Resources.clone(getFairShare());
        Resource maxShare = getMaxShare();

        if (maxResource.getMemorySize() == 0) {
            maxResource.setMemorySize(maxShare.getMemorySize());
        }

        if (maxResource.getVCore() == 0) {
            maxResource.setVCore(maxShare.getVCore());
        }

        // Round up to allow AM to run when there is only one vcore on the cluster
        return Resources.multiplyAndRoundUp(maxResource, maxAMShare);
    }

    /**
     * Check whether this queue can run the Application Master under the
     * maxAMShare limit.
     *
     * @param amResource resources required to run the AM
     * @return true if this queue can run
     */
    public boolean canRunAppAM(Resource amResource) {
        if (Math.abs(maxAMShare - -1.0f) < 0.0001) {
            return true;
        }

        Resource maxAMResource = computeMaxAMResource();
        Resource ifRunAMResource = Resources.add(amResourceUsage, amResource);
        return Resources.fitsIn(ifRunAMResource, maxAMResource);
    }

    void addAMResourceUsage(Resource amResource) {
        if (amResource != null) {
            Resources.addTo(amResourceUsage, amResource);
        }
    }


    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public Resource getMaximumSchedulerUnitAllocation() {
        if (maxSchedulerUnitAllocation.equals(Resources.unbounded())
                && getParentPool() != null) {
            return getParentPool().getMaximumSchedulerUnitAllocation();
        } else {
            return maxSchedulerUnitAllocation;
        }
    }


    /**
     * Helper method for tests to check if a queue is starved for minShare.
     *
     * @return whether starved for minshare
     */
    private boolean isStarvedForMinShare() {
        return !Resources.isNone(minShareStarvation());
    }

    /**
     * Helper method for tests to check if a queue is starved for fairshare.
     *
     * @return whether starved for fairshare
     */
    private boolean isStarvedForFairShare() {
        for (AppDescribe app : runnableApps) {
            if (app.isStarvedForFairShare()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method for tests to check if a queue is starved.
     *
     * @return whether starved for either minshare or fairshare
     */
    boolean isStarved() {
        return isStarvedForMinShare() || isStarvedForFairShare();
    }

    /**
     * This method is called when an application is assigned to this queue
     * for book-keeping purposes (to be able to determine if the queue is empty).
     *
     * @param applicationId the application's id
     */
    public void addAssignedApp(ApplicationId applicationId) {
        writeLock.lock();
        try {
            assignedApps.add(applicationId);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This method is called when an application is removed from this queue
     * during the submit process.
     *
     * @param applicationId the application's id
     */
    public void removeAssignedApp(ApplicationId applicationId) {
        writeLock.lock();
        try {
            assignedApps.remove(applicationId);
        } finally {
            writeLock.unlock();
        }
    }

}
