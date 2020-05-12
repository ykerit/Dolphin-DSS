package DolphinMaster.scheduler;

import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PoolManager {
    private static final Logger log = LogManager.getLogger(PoolManager.class);

    public static final String ROOT_POOL = "root";

    private final FairScheduler scheduler;

    private ParentResourcePool rootPool;
    private final Collection<LeafResourcePool> leafPools = new CopyOnWriteArrayList<>();
    private final Map<String, ResourcePoolImp> pools = new HashMap<>();

    public PoolManager(FairScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ParentResourcePool getRootPool() {
        return rootPool;
    }

    public void initialize() {
        rootPool = new ParentResourcePool("root", scheduler, null);
        pools.put(rootPool.getName(), rootPool);

        LeafResourcePool defaultQueue = getLeafPool("default", true);
        rootPool.reinit(true);
    }

    public LeafResourcePool getLeafPool(String name, boolean create) {
        return getLeafPool(name, create, null, true);
    }

    public LeafResourcePool getLeafPool(String name, boolean create, ApplicationId applicationId) {
        return getLeafPool(name, create, applicationId, true);
    }

    private LeafResourcePool getLeafPool(String name, boolean create,
                                         ApplicationId applicationId, boolean recomputeSteadyShare) {
        ResourcePoolImp pool = getPool(name, create,
                PoolType.LEAF, recomputeSteadyShare, applicationId);
        if (pool instanceof ParentResourcePool) {
            return null;
        }
        return (LeafResourcePool) pool;
    }

    public boolean removeLeafPool(String name) {
        name = ensureRootPrefix(name);
        return true;
    }

    public ParentResourcePool getParentPool(String name, boolean create) {
        return getParentPool(name, create, true);
    }

    public ParentResourcePool getParentPool(String name, boolean create, boolean recomputeSteadyShares) {
        ResourcePoolImp pool = getPool(name, create, PoolType.PARENT,
                recomputeSteadyShares, null);
        if (pool instanceof LeafResourcePool) {
            return null;
        }
        return (ParentResourcePool) pool;
    }

    public ResourcePoolImp getPool(String name) {
        name = ensureRootPrefix(name);
        synchronized (pools) {
            return pools.get(name);
        }
    }

    private ResourcePoolImp getPool(String name, boolean create,
                                    PoolType poolType, boolean recomputeSteadyShares,
                                    ApplicationId applicationId) {
        boolean recompute = recomputeSteadyShares;
        name = ensureRootPrefix(name);
        ResourcePoolImp pool;
        synchronized (pools) {
            pool = pools.get(name);
            if (pool == null && create) {
                pool = createPool(name, poolType);
            } else {
                recompute = false;
            }
            if (applicationId != null && pool instanceof LeafResourcePool) {
                ((LeafResourcePool) pool).addAssignedApp(applicationId);
            }
        }
        if (recompute && pool != null) {
            rootPool.recomputeSteadyShares();
        }
        return pool;
    }

    ResourcePoolImp createPool(String name, PoolType poolType) {
        List<String> newPoolNames = new ArrayList<>();
        ParentResourcePool parent = buildNewPoolList(name, newPoolNames);
        ResourcePoolImp pool = null;
        if (parent != null) {
            pool = createNewPools(poolType, parent, newPoolNames);
        }
        return pool;
    }


    // Generate resource pool from configuration file.
    private ResourcePoolImp createNewPools(PoolType poolType,
                                           ParentResourcePool rootParent,
                                           List<String> newPoolNames) {
        return null;
    }

    private static String ensureRootPrefix(String name) {
        if (!name.startsWith(ROOT_POOL + ".") && !name.equals(ROOT_POOL)) {
            name = ROOT_POOL + "." + name;
        }
        return name;
    }

    private ParentResourcePool buildNewPoolList(String name,
                                                List<String> newPoolNames) {
        newPoolNames.add(name);
        int sepIndex = name.length();
        ParentResourcePool parent = null;

        // Move up the queue tree until we reach one that exists.
        while (sepIndex != -1) {
            int prevSepIndex = sepIndex;
            sepIndex = name.lastIndexOf('.', sepIndex - 1);
            String node = name.substring(sepIndex + 1, prevSepIndex);

            String curName = name.substring(0, sepIndex);
            ResourcePoolImp queue = pools.get(curName);

            if (queue == null) {
                newPoolNames.add(0, curName);
            } else {
                if (queue instanceof ParentResourcePool) {
                    parent = (ParentResourcePool) queue;
                }

                // If the queue isn't a parent queue, parent will still be null when
                // we break

                break;
            }
        }

        return parent;
    }

}
