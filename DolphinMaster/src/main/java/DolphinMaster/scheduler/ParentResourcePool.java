package DolphinMaster.scheduler;

import com.google.common.collect.ImmutableList;
import common.resource.Resource;
import common.resource.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParentResourcePool extends ResourcePoolImp {
    private static final Logger log = LogManager.getLogger(ParentResourcePool.class);

    private final List<ResourcePoolImp> childPools = new ArrayList<>();
    private Resource demand = Resources.createResource(0);
    private int runnableApps;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    public ParentResourcePool(String name, FairScheduler scheduler, ParentResourcePool parentPool) {
        super(name, scheduler, parentPool);
    }

    @Override
    public Resource getMaximumSchedulerUnitAllocation() {
        if (getName().equals("root")) {
            return maxSchedulerUnitAllocation;
        }
        if (maxSchedulerUnitAllocation.equals(Resources.unbounded()) && getParentPool() != null) {
            return getParentPool().getMaximumSchedulerUnitAllocation();
        } else {
            return maxSchedulerUnitAllocation;
        }
    }

    void addChildPool(ResourcePoolImp child) {
        writeLock.lock();
        try {
            childPools.add(child);
        } finally {
            writeLock.unlock();
        }
    }

    void removeChildPool(ResourcePoolImp child) {
        writeLock.lock();
        try {
            childPools.remove(child);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    void updateInterval() {
        readLock.lock();
        try {
            policy.computeShares(childPools, getFairShare());
            for (ResourcePoolImp childPool : childPools) {
                // metrics
                childPool.updateInterval();
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<ResourcePoolImp> getChildPools() {
        readLock.lock();
        try {
            return ImmutableList.copyOf(childPools);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int getNumRunnableApps() {
        readLock.lock();
        try {
            return runnableApps;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            for (ResourcePoolImp child : childPools) {
                if (!child.isEmpty()) {
                    return false;
                }
            }
        } finally {
            readLock.unlock();
        }
        return true;
    }

    void recomputeSteadyShares() {
        readLock.lock();
        try {
            policy.computeShares(childPools, getSteadyFairShare());
            for (ResourcePoolImp childPool : childPools) {
                // metrics
                if (childPool instanceof ParentResourcePool) {
                    ((ParentResourcePool) childPool).recomputeSteadyShares();
                }
            }
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public Resource getDemand() {
        readLock.lock();
        try {
            return Resource.newInstance(demand.getMemorySize(), demand.getVCore());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void updateDemand() {
        writeLock.lock();
        try {
            demand = Resources.createResource(0);
            for (ResourcePoolImp childPool : childPools) {
                childPool.updateDemand();
                Resource toAdd = childPool.getDemand();
                demand = Resources.add(demand, toAdd);
                if (log.isDebugEnabled()) {
                    log.debug("counting resource from " + childPool.getName() +
                            " " + toAdd + "; total resource demand for " + getName() + " now " + demand);
                }
            }
            demand = Resources.componentwiseMin(demand, getMaxShare());
        } finally {
            writeLock.unlock();
        }
        if (log.isDebugEnabled()) {
            log.debug("the updated demand for " + getName() + " is " + demand + " the max is " + getMaxShare());
        }
    }

    @Override
    public Resource assignSchedulerUnit(FSSchedulerNode node) {
        Resource assigned = Resources.none();

        TreeSet<ResourcePoolImp> sortedChildPool = new TreeSet<>(policy.getComparator());
        readLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Node: " + node.getNodeName() + " offered to parent pool: " +
                        getName() + " visiting " + childPools.size() + " children");
            }
            sortedChildPool.addAll(childPools);
            for (ResourcePoolImp child : sortedChildPool) {
                assigned = child.assignSchedulerUnit(node);
                if (!Resources.equals(assigned, Resources.none())) {
                    break;
                }
            }
        } finally {
            readLock.unlock();
        }
        return assigned;
    }

    void incrementRunnableApps() {
        writeLock.lock();
        try {
            runnableApps++;
        } finally {
            writeLock.unlock();
        }
    }

    void decrementRunnableApps() {
        writeLock.lock();
        try {
            runnableApps--;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void collectSchedulerApplication(Collection<AppDescribeId> apps) {
        readLock.lock();
        try {
            for (ResourcePoolImp child : childPools) {
                child.collectSchedulerApplication(apps);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected void dumpStateInterval(StringBuilder sb) {
        sb.append("{Name: " + getName() +
                ", Weight: " + weight +
                ", Policy: " + "FIFO" +
                ", FairShare: " + getFairShare() +
                ", SteadyFairShare: " + getSteadyFairShare() +
                ", MaxShare: " + getMaxShare() +
                ", MinShare: " + getMinShare() +
                ", ResourceUsage: " + getResourceUsage() +
                ", Demand: " + getDemand() +
                ", MaxAMShare: " + maxAMShare +
                ", RunnableApps: " + getNumRunnableApps() + "}");
        for (ResourcePoolImp child : getChildPools()) {
            sb.append(", ");
            child.dumpStateInterval(sb);
        }
    }
}
