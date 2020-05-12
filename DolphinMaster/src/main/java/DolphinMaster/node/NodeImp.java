package DolphinMaster.node;


import DolphinMaster.DolphinContext;
import agent.appworkmanage.appwork.AppWork;
import agent.message.AgentHeartBeatResponse;
import agent.status.AppWorkStatus;
import common.event.EventProcessor;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.struct.AgentId;
import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodeImp implements Node, EventProcessor<NodeEvent> {

    private static final Logger log = LogManager.getLogger(NodeImp.class.getName());

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final ConcurrentLinkedQueue<UpdateAppWorkInfo> nodeUpdateQueue;

    private final AgentId agentId;
    private final DolphinContext context;
    private final String hostName;
    private final String nodeAddress;
    // use for AM
    private int commandPort;

    // snapshot for total resource
    private volatile Resource originalTotalCapability;
    private volatile Resource totalCapability;

    private long timeStamp;
    private NodeState nodeState;

    private ResourceUtilization appWorksUtilization;
    private ResourceUtilization nodeUtilization;
    private volatile Resource physicalResource;

    private final Set<String> launchedAppWorks = new HashSet<>();
    private final Set<String> completedAppWorks = new HashSet<>();
    private final Set<String> appWorksToClean = new TreeSet<>();
    private final Set<String> appWorksToBeRemovedFromAgent = new HashSet<>();
    private final List<ApplicationId> finishedApplications = new ArrayList<>();
    private final List<ApplicationId> runningApplication = new ArrayList<>();
    private final Map<String, AppWork> toBeUpdateAppWorks = new HashMap<>();

    private final Map<String, AppWorkStatus> updateExistAppWorks = new HashMap<>();
    private final Map<String, AppWork> toBeDecreasedAppWorks = new HashMap<>();
    private final Map<String, AppWork> reportedIncreasedAppWorks = new HashMap<>();


    public NodeImp(AgentId id, DolphinContext context,
                   String hostName, int commandPort,
                   Resource capability, Resource physicalResource) {
        this.agentId = id;
        this.context = context;
        this.hostName = hostName;
        this.nodeAddress = hostName + ":" + commandPort;
        this.commandPort = commandPort;
        this.totalCapability = capability;
        this.timeStamp = 0;
        this.physicalResource = physicalResource;
        this.nodeUpdateQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public String toString() {
        return this.agentId.toString();
    }

    @Override
    public AgentId getNodeId() {
        return agentId;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public int getCommandPort() {
        return commandPort;
    }

    @Override
    public String getNodeAddress() {
        return nodeAddress;
    }

    @Override
    public Resource getTotalCapability() {
        return totalCapability;
    }

    @Override
    public Resource getPhysicalResource() {
        return physicalResource;
    }

    @Override
    public ResourceUtilization getAppWorksUtilization() {
        readLock.lock();
        try {
            return appWorksUtilization;
        } finally {
            readLock.unlock();
        }
    }

    public void setAppWorksUtilization(ResourceUtilization appWorksUtilization) {
        writeLock.lock();
        try {
            this.appWorksUtilization = appWorksUtilization;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public ResourceUtilization getNodeUtilization() {
        readLock.lock();
        try {
            return nodeUtilization;
        } finally {
            readLock.unlock();
        }
    }

    public void setNodeUtilization(ResourceUtilization nodeUtilization) {
        writeLock.lock();
        try {
            this.nodeUtilization = nodeUtilization;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<ApplicationId> getAppsToCleanup() {
        readLock.lock();
        try {
            return new ArrayList<>(finishedApplications);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<ApplicationId> getRunningApps() {
        readLock.lock();
        try {
            return new ArrayList<>(runningApplication);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<String> getAppWorkToCleanup() {
        readLock.lock();
        try {
            return new ArrayList<>(appWorksToClean);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setAndUpdateAgentHeartbeatResponse(AgentHeartBeatResponse response) {
        writeLock.lock();
        try {

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Collection<AppWork> getToBeUpdateAppWorks() {
        readLock.lock();
        try {
            return toBeUpdateAppWorks.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void rsyncCapability() {

    }

    @Override
    public void process(NodeEvent event) {
        log.debug("Processing {} of type {}", event.getAgentId(), event.getType());
        writeLock.lock();
        try {

        } finally {
            writeLock.unlock();
        }
    }

    public NodeState getState() {
        readLock.lock();
        try {
            return nodeState;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DolphinContext getContext() {
        return context;
    }

    @Override
    public String getRackName() {
        return null;
    }
}
