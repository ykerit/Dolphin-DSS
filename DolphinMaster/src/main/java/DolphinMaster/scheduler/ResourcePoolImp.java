package DolphinMaster.scheduler;

import DolphinMaster.scheduler.policy.SchedulingPolicy;
import common.resource.Resource;
import common.resource.Resources;
import common.struct.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ResourcePoolImp implements Schedulable, ResourcePool {

    private static final Logger log = LogManager.getLogger(ResourcePoolImp.class.getName());

    private Resource fairShare = Resources.createResource(0, 0);
    private Resource steadyFairShare = Resources.createResource(0, 0);
    private Resource reservedResource = Resources.createResource(0, 0);
    private Resource resourceUsage = Resource.newInstance(0, 0);
    private final String name;
    protected final FairScheduler scheduler;
    private final PoolMetrics metrics;

    protected final ParentResourcePool parentPool;

    protected SchedulingPolicy policy = SchedulingPolicy.DEFAULT_POLICY;

    protected float weight;
    private Resource minimumShare;
    private ConfigurableResource maximumShare;
    private int maxRunningApp;
    private ConfigurableResource maxChildQueueResource;

    protected float maxAMShare;

    private long fairSharePreemptionTimeout = Long.MAX_VALUE;
    private long minSharePreemptionTimeout = Long.MAX_VALUE;
    private float fairSharePreemptionThreshold = 0.5f;
    private boolean preemptable = true;

    protected Resource maxSchedulerUnitAllocation;

    public ResourcePoolImp(String name, FairScheduler scheduler, ParentResourcePool parentPool) {
        this.name = name;
        this.scheduler = scheduler;
        this.metrics = new PoolMetrics();
        this.parentPool = parentPool;
    }

    public final void reinit(boolean recursive) {
        AllocationConfiguration configuration = scheduler.getAllocationConfiguration();
        configuration.initPool(this);
        updatePreemptionVariables();
        if (recursive) {
            for (ResourcePoolImp child : getChildPools()) {
                child.reinit(recursive);
            }
        }
    }

    public abstract Resource getMaximumSchedulerUnitAllocation();

    abstract void updateInterval();

    public abstract List<ResourcePoolImp> getChildPools();

    public abstract int getNumRunnableApps();

    public abstract boolean isEmpty();

    public abstract void collectSchedulerApplication(Collection<AppDescribeId> apps);

    protected abstract void dumpStateInterval(StringBuilder sb);

    public Resource getFairShare() {
        return fairShare;
    }

    @Override
    public void setFairShare(Resource fairShare) {
        this.fairShare = fairShare;
    }

    public Resource getSteadyFairShare() {
        return steadyFairShare;
    }

    public void setSteadyFairShare(Resource steadyFairShare) {
        this.steadyFairShare = steadyFairShare;
    }

    public SchedulingPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(SchedulingPolicy policy) {
        this.policy = policy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Resource getMinShare() {
        return minimumShare;
    }

    public void setMinShare(Resource share) {
        this.minimumShare = share;
    }

    @Override
    public Resource getMaxShare() {
        Resource maxResource = maximumShare.getResource(scheduler.getClusterResource());
        Resource result = Resources.componentwiseMax(maxResource, minimumShare);

        if (!Resources.equals(maxResource, result)) {
            log.warn(String.format("Pool %s has max resources %s less than min resources %s",
                    getName(), maxResource, minimumShare));
        }
        return result;
    }

    public void setMaxShare(ConfigurableResource share) {
        this.maximumShare = share;
    }

    public void setMaxSchedulerUnitAllocation(Resource maxSchedulerUnitAllocation) {
        this.maxSchedulerUnitAllocation = maxSchedulerUnitAllocation;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public Priority getPriority() {
        Priority priority = Priority.newInstance(0);
        priority.setPriority(1);
        return priority;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public Resource getResourceUsage() {
        return resourceUsage;
    }

    @Override
    public String getPoolName() {
        return name;
    }

    @Override
    public PoolMetrics getPoolMetrics() {
        return metrics;
    }

    @Override
    public PoolInfo getPoolInfo(boolean includeChildPool, boolean recursive) {
        PoolInfo info = new PoolInfo();
        info.setPoolName(getName());
        if (scheduler.getClusterResource().getMemorySize() == 0) {
            info.setCapacity(0.0f);
        } else {
            info.setCapacity((float) getFairShare().getMemorySize() / scheduler.getClusterResource().getMemorySize());
        }
        if (getFairShare().getMemorySize() == 0) {
            info.setCurrentCapacity(0.0f);
        } else {
            info.setCurrentCapacity((float) getResourceUsage().getMemorySize() / getFairShare().getMemorySize());
        }

        ArrayList<PoolInfo> childPoolInfo = new ArrayList<>();
        if (includeChildPool) {
            Collection<ResourcePoolImp> childPools = getChildPools();
            for (ResourcePoolImp child : childPools) {
                childPoolInfo.add(child.getPoolInfo(recursive, recursive));
            }
        }
        info.setChildQueues(childPoolInfo);
        info.setPoolState(PoolState.RUNNING);
        info.setPoolStatistics(getPoolStatistics());
        return info;
    }

    public PoolStatistics getPoolStatistics() {
        PoolStatistics stats = new PoolStatistics();
        return stats;
    }

    public PoolMetrics getMetrics() {
        return metrics;
    }


    @Override
    public void incPendingResource() {

    }

    @Override
    public void decPendingResource() {

    }

    @Override
    public Priority getApplicationPriority() {
        return null;
    }

    @Override
    public void incReserveResource(String partition, Resource reserveRes) {

    }

    @Override
    public void decReserveResource(String partition, Resource reserveRes) {

    }

    public String dumpState() {
        StringBuilder sb = new StringBuilder();
        dumpStateInterval(sb);
        return sb.toString();
    }

    long getMinSharePreemptionTimeout() {
        return minSharePreemptionTimeout;
    }

    void setMinSharePreemptionTimeout(long minSharePreemptionTimeout) {
        this.minSharePreemptionTimeout = minSharePreemptionTimeout;
    }

    public long getFairSharePreemptionTimeout() {
        return fairSharePreemptionTimeout;
    }

    protected void incUsedResource(Resource res) {
        synchronized (resourceUsage) {
            Resources.addTo(resourceUsage, res);
            if (parentPool != null) {
                parentPool.incUsedResource(res);
            }
        }
    }

    boolean assignSchedulerUnitPreCheck(FSSchedulerNode node) {
        if (node.getReservedSchedulerUnit() != null) {
            log.debug("Assigning container failed on node '{}' because it has"
                    + " reserved containers.", node.getNodeName());
            return false;
        } else if (!Resources.fitsIn(getResourceUsage(), getMaxShare())) {
            if (log.isDebugEnabled()) {
                log.debug("Assigning container failed on node '" + node.getNodeName()
                        + " because queue resource usage is larger than MaxShare: "
                        + dumpState());
            }
            return false;
        } else {
            return true;
        }
    }

    public ParentResourcePool getParentPool() {
        return parentPool;
    }

    public void setMaxChildQueueResource(ConfigurableResource maxChildShare) {
        this.maxChildQueueResource = maxChildShare;
    }

    public ConfigurableResource getMaxChildQueueResource() {
        return maxChildQueueResource;
    }

    public void setMaxRunningApp(int num) {
        maxRunningApp = num;
    }

    public int getMaxRunningApp() {
        return maxRunningApp;
    }

    public float getMaxAMShare() {
        return maxAMShare;
    }

    public void setMaxAMShare(float maxAMShare) {
        this.maxAMShare = maxAMShare;
    }

    public void setFairSharePreemptionTimeout(long fairSharePreemptionTimeout) {
        this.fairSharePreemptionTimeout = fairSharePreemptionTimeout;
    }

    float getFairSharePreemptionThreshold() {
        return fairSharePreemptionThreshold;
    }

    void setFairSharePreemptionThreshold(float fairSharePreemptionThreshold) {
        this.fairSharePreemptionThreshold = fairSharePreemptionThreshold;
    }

    @Override
    public boolean isPreemptable() {
        return preemptable;
    }

    public void update(Resource fairShare) {
        setFairShare(fairShare);
        updateInterval();
    }

    private void updatePreemptionVariables() {
        minSharePreemptionTimeout = scheduler.getAllocationConfiguration().
                getMinSharePreemptionTimeout(getName());
        if (minSharePreemptionTimeout == -1 && parentPool != null) {
            minSharePreemptionTimeout = parentPool.getMinSharePreemptionTimeout();
        }
        fairSharePreemptionTimeout = scheduler.getAllocationConfiguration().
                getFairSharePreemptionTimeout(getName());
        if (fairSharePreemptionTimeout == -1 && parentPool != null) {
            fairSharePreemptionTimeout = parentPool.getFairSharePreemptionTimeout();
        }
        fairSharePreemptionThreshold = scheduler.getAllocationConfiguration().
                getFairSharePreemptionThreshold(getName());
        if (fairSharePreemptionThreshold == -1 && parentPool != null) {
            fairSharePreemptionThreshold = parentPool.getFairSharePreemptionThreshold();
        }
        if (parentPool != null && !parentPool.isPreemptable()) {
            preemptable = true;
        } else {
            preemptable = scheduler.getAllocationConfiguration().isPreemptable(getName());
        }
    }

    public boolean isActive() {
        return getNumRunnableApps() > 0;
    }

    @Override
    public String toString() {
        return String.format("[%s, demand=%s, running=%s, share=%s, w=%s]",
                getName(), getDemand(), getResourceUsage(), fairShare, getWeight());
    }

    protected void decUsedResource(Resource res) {
        synchronized (resourceUsage) {
            Resources.subtractFrom(resourceUsage, res);
            if (parentPool != null) {
                parentPool.decUsedResource(res);
            }
        }
    }


    boolean fitsInMaxShare(Resource additionalResource) {
        Resource usagePlusAddition =
                Resources.add(getResourceUsage(), additionalResource);

        if (!Resources.fitsIn(usagePlusAddition, getMaxShare())) {
            if (log.isDebugEnabled()) {
                log.debug("Resource usage plus resource request: " + usagePlusAddition
                        + " exceeds maximum resource allowed:" + getMaxShare()
                        + " in pool " + getName());
            }
            return false;
        }

        ResourcePoolImp parentQueue = getParentPool();
        if (parentQueue != null) {
            return parentQueue.fitsInMaxShare(additionalResource);
        }
        return true;
    }
}
