package DolphinMaster;


import DolphinMaster.scheduler.SchedulerNode;
import DolphinMaster.scheduler.SchedulerNodeReport;
import common.resource.Resource;
import common.resource.ResourceInformation;
import common.resource.Resources;
import common.struct.AgentId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterNodeTracker {
    private static final Logger log = LogManager.getLogger(ClusterNodeTracker.class);

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    private HashMap<AgentId, SchedulerNode> nodes = new HashMap<>();
    private Map<String, SchedulerNode> nodeNameToNodeMap = new HashMap<>();
    private Map<String, List<SchedulerNode>> nodesPerRack = new HashMap<>();
    private Map<String, List<SchedulerNode>> nodesPerLabel = new HashMap<>();

    private Resource clusterCapacity = Resources.createResource(0, 0);
    private volatile Resource staleClusterCapacity =
            Resources.clone(Resources.none());

    // Max allocation
    private final long[] maxAllocation;
    private Resource configuredMaxAllocation;
    private boolean forceConfiguredMaxAllocation = true;
    private long configuredMaxAllocationWaitTime;
    private boolean reportedMaxAllocation = false;

    public ClusterNodeTracker() {
        maxAllocation = new long[2];
        Arrays.fill(maxAllocation, -1);
    }

    public void addNode(SchedulerNode node) {
        writeLock.lock();
        try {
            nodes.put(node.getNodeId(), node);
            nodeNameToNodeMap.put(node.getNodeName(), node);

            // Update nodes per rack as well
            String rackName = node.getRackName();
            List<SchedulerNode> nodesList = nodesPerRack.get(rackName);
            if (nodesList == null) {
                nodesList = new ArrayList<>();
                nodesPerRack.put(rackName, nodesList);
            }
            nodesList.add(node);

            // Update cluster capacity
            Resources.addTo(clusterCapacity, node.getTotalResource());
            staleClusterCapacity = Resources.clone(clusterCapacity);

            // Update maximumAllocation
            updateMaxResources(node, true);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean exists(AgentId nodeId) {
        readLock.lock();
        try {
            return nodes.containsKey(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    public SchedulerNode getNode(AgentId nodeId) {
        readLock.lock();
        try {
            return nodes.get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    public SchedulerNodeReport getNodeReport(AgentId nodeId) {
        readLock.lock();
        try {
            SchedulerNode n = nodes.get(nodeId);
            return n == null ? null : new SchedulerNodeReport(n);
        } finally {
            readLock.unlock();
        }
    }

    public int nodeCount() {
        readLock.lock();
        try {
            return nodes.size();
        } finally {
            readLock.unlock();
        }
    }

    public int nodeCount(String rackName) {
        readLock.lock();
        String rName = rackName == null ? "NULL" : rackName;
        try {
            List<SchedulerNode> nodesList = nodesPerRack.get(rName);
            return nodesList == null ? 0 : nodesList.size();
        } finally {
            readLock.unlock();
        }
    }

    public Resource getClusterCapacity() {
        return staleClusterCapacity;
    }

    public SchedulerNode removeNode(AgentId nodeId) {
        writeLock.lock();
        try {
            SchedulerNode node = nodes.remove(nodeId);
            if (node == null) {
                log.warn("Attempting to remove a non-existent node " + nodeId);
                return null;
            }
            nodeNameToNodeMap.remove(node.getNodeName());

            // Update nodes per rack as well
            String rackName = node.getRackName();
            List<SchedulerNode> nodesList = nodesPerRack.get(rackName);
            if (nodesList == null) {
                log.error("Attempting to remove node from an empty rack " + rackName);
            } else {
                nodesList.remove(node);
                if (nodesList.isEmpty()) {
                    nodesPerRack.remove(rackName);
                }
            }

            // Update cluster capacity
            Resources.subtractFrom(clusterCapacity, node.getTotalResource());
            staleClusterCapacity = Resources.clone(clusterCapacity);

            // Update maximumAllocation
            updateMaxResources(node, false);

            return node;
        } finally {
            writeLock.unlock();
        }
    }

    public void setConfiguredMaxAllocation(Resource resource) {
        writeLock.lock();
        try {
            configuredMaxAllocation = Resources.clone(resource);
        } finally {
            writeLock.unlock();
        }
    }

    public void setConfiguredMaxAllocationWaitTime(
            long configuredMaxAllocationWaitTime) {
        writeLock.lock();
        try {
            this.configuredMaxAllocationWaitTime =
                    configuredMaxAllocationWaitTime;
        } finally {
            writeLock.unlock();
        }
    }

    public Resource getMaxAllowedAllocation() {
        readLock.lock();
        try {
            if (forceConfiguredMaxAllocation &&
                    System.currentTimeMillis() - DolphinMaster.getClusterTimeStamp()
                            > configuredMaxAllocationWaitTime) {
                forceConfiguredMaxAllocation = false;
            }

            if (forceConfiguredMaxAllocation || !reportedMaxAllocation) {
                return configuredMaxAllocation;
            }

            Resource ret = Resources.clone(configuredMaxAllocation);

            for (int i = 0; i < maxAllocation.length; i++) {
                ResourceInformation info = ret.getResourceInformation(i);

                if (info.getValue() > maxAllocation[i]) {
                    info.setValue(maxAllocation[i]);
                }
            }

            return ret;
        } finally {
            readLock.unlock();
        }
    }

    public void setForceConfiguredMaxAllocation(boolean flag) {
        writeLock.lock();
        try {
            forceConfiguredMaxAllocation = flag;
        } finally {
            writeLock.unlock();
        }
    }

    private void updateMaxResources(SchedulerNode node, boolean add) {
        Resource totalResource = node.getTotalResource();
        ResourceInformation[] totalResources;

        if (totalResource != null) {
            totalResources = totalResource.getResources();
        } else {
            log.warn(node.getNodeName() + " reported in with null resources, which "
                    + "indicates a problem in the source code. Please file an issue at "
                    + "https://issues.apache.org/jira/secure/CreateIssue!default.jspa");

            return;
        }

        writeLock.lock();

        try {
            if (add) { // added node
                // If we add a node, we must have a max allocation for all resource
                // types
                reportedMaxAllocation = true;

                for (int i = 0; i < maxAllocation.length; i++) {
                    long value = totalResources[i].getValue();

                    if (value > maxAllocation[i]) {
                        maxAllocation[i] = value;
                    }
                }
            } else {  // removed node
                boolean recalculate = false;

                for (int i = 0; i < maxAllocation.length; i++) {
                    if (totalResources[i].getValue() == maxAllocation[i]) {
                        // No need to set reportedMaxAllocation to false here because we
                        // will recalculate before we release the lock.
                        maxAllocation[i] = -1;
                        recalculate = true;
                    }
                }

                // We only have to iterate through the nodes if the current max memory
                // or vcores was equal to the removed node's
                if (recalculate) {
                    // Treat it like an empty cluster and add nodes
                    reportedMaxAllocation = false;
                    nodes.values().forEach(n -> updateMaxResources(n, true));
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * update cached nodes per partition on a node label change event.
     *
     * @param partition nodeLabel
     * @param nodeIds   List of Node IDs
     */
    public void updateNodesPerPartition(String partition, Set<AgentId> nodeIds) {
        writeLock.lock();
        try {
            // Clear all entries.
            nodesPerLabel.remove(partition);

            List<SchedulerNode> nodesPerPartition = new ArrayList<>();
            for (AgentId nodeId : nodeIds) {
                SchedulerNode n = getNode(nodeId);
                if (n != null) {
                    nodesPerPartition.add(n);
                }
            }

            // Update new set of nodes for given partition.
            nodesPerLabel.put(partition, nodesPerPartition);
        } finally {
            writeLock.unlock();
        }
    }

    public List<SchedulerNode> getNodesPerPartition(String partition) {
        List<SchedulerNode> nodesPerPartition = null;
        readLock.lock();
        try {
            if (nodesPerLabel.containsKey(partition)) {
                nodesPerPartition = new ArrayList<SchedulerNode>(nodesPerLabel.get(partition));
            }
        } finally {
            readLock.unlock();
        }
        return nodesPerPartition;
    }

}
