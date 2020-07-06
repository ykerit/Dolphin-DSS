package DolphinMaster.node;


import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import DolphinMaster.app.event.AppRunningOnNodeEvent;
import DolphinMaster.scheduler.SchedulerUtils;
import DolphinMaster.scheduler.event.NodeAddedSchedulerEvent;
import DolphinMaster.scheduler.event.NodeUpdateSchedulerEvent;
import agent.message.AgentHeartBeatResponse;
import common.event.EventProcessor;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.struct.*;
import common.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

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
    private final Set<AppWorkId> launchedAppWorks = new HashSet<>();
    private final Set<AppWorkId> completedAppWorks = new HashSet<>();
    private final Set<AppWorkId> appWorksToClean = new TreeSet<>();
    private final Set<AppWorkId> appWorksToBeRemovedFromAgent = new HashSet<>();
    private final List<ApplicationId> finishedApplications = new ArrayList<>();
    private final List<ApplicationId> runningApplications = new ArrayList<>();
    private final Map<AppWorkId, RemoteAppWork> toBeUpdateAppWorks = new HashMap<>();
    private final Map<AppWorkId, AppWorkStatus> updateExistAppWorks = new HashMap<>();
    private final Map<AppWorkId, RemoteAppWork> toBeDecreasedAppWorks = new HashMap<>();
    private final Map<AppWorkId, RemoteAppWork> reportedIncreasedAppWorks = new HashMap<>();
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

    private final UntypedStateMachineBuilder nodeStateMachineBuilder;
    private final UntypedStateMachine nodeStateMachine;

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

        nodeStateMachineBuilder = StateMachineBuilderFactory.create(NodeStateMachine.class);

        nodeStateMachineBuilder.externalTransitions()
                .from(NodeState.NEW)
                .toAmong(NodeState.RUNNING, NodeState.NEW, NodeState.NEW)
                .onEach(NodeEventType.STARTED, NodeEventType.RESOURCE_UPDATE, NodeEventType.FINISHED_APP_WORKS_PULLED_BY_AM)
                .callMethod("addNode|updateResource|addAppWorksToBeRemoved");

        nodeStateMachineBuilder.externalTransitions()
                .from(NodeState.RUNNING)
                .toAmong(NodeState.RUNNING, NodeState.LOST, NodeState.REBOOTED)
                .onEach(NodeEventType.STATUS_UPDATE, NodeEventType.EXPIRE, NodeEventType.REBOOTING)
                .callMethod("statusUpdate|_|_");

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.CLEANUP_APP);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.CLEANUP_APP_WORK);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.FINISHED_APP_WORKS_PULLED_BY_AM);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.RECONNECTED);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.RESOURCE_UPDATE);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.RUNNING)
                .on(NodeEventType.UPDATE_APP_WORK);

        nodeStateMachineBuilder.externalTransition()
                .from(NodeState.RUNNING)
                .to(NodeState.SHUTDOWN)
                .on(NodeEventType.SHUTDOWN);

        nodeStateMachine = nodeStateMachineBuilder.newStateMachine(NodeState.NEW);
        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        nodeStateMachine.accept(visitor);
        visitor.convertDotFile(Tools.storeStateMachine("NodeStateMachine"));
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
            return new ArrayList<>(runningApplications);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<AppWorkId> getAppWorkToCleanup() {
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
            response.setAppWorksToCleanup(new ArrayList<>(this.appWorksToClean));
            response.setApplicationsToCleanup(this.finishedApplications);
            response.setAppWorksToBeRemoved(new ArrayList<>(this.appWorksToBeRemovedFromAgent));
            this.completedAppWorks.removeAll(this.appWorksToBeRemovedFromAgent);
            this.appWorksToClean.clear();
            this.finishedApplications.clear();
            this.appWorksToBeRemovedFromAgent.clear();

            response.setAppWorksToUpdate(this.toBeUpdateAppWorks.values());
            this.toBeDecreasedAppWorks.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Collection<RemoteAppWork> getToBeUpdateAppWorks() {
        readLock.lock();
        try {
            return toBeUpdateAppWorks.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<UpdateAppWorkInfo> pullAppWorkUpdates() {
        List<UpdateAppWorkInfo> latestAppWorkInfoList = new ArrayList<>();
        UpdateAppWorkInfo appWorkInfo;
        while ((appWorkInfo = nodeUpdateQueue.poll()) != null) {
            latestAppWorkInfoList.add(appWorkInfo);
        }
        return latestAppWorkInfoList;
    }

    @Override
    public void rsyncCapability() {

    }

    @Override
    public void process(NodeEvent event) {
        writeLock.lock();
        try {
            log.debug("Processing {} of type {}", event.getAgentId(), event.getType());
            nodeStateMachine.fire(event.getType(), new Channel(this, event));
        } finally {
            writeLock.unlock();
        }
    }

    public NodeState getState() {
        readLock.lock();
        try {
            return (NodeState) nodeStateMachine.getCurrentState();
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

    private void handleAppWorkStatus(List<AppWorkStatus> appWorkStatuses) {
        List<AppWorkStatus> newlyLaunchedAppWorks = new ArrayList<>();
        List<AppWorkStatus> newlyCompletedAppWorks = new ArrayList<>();
        List<Pair<ApplicationId, AppWorkStatus>> needUpdateAppWorks =
                new ArrayList<>();
        int numRemoteRunningAppWorks = 0;
        for (AppWorkStatus remoteAppWork : appWorkStatuses) {
            AppWorkId appWorkId = remoteAppWork.getAppWorkId();
            if (appWorksToClean.contains(appWorkId)) {
                log.info("AppWork " + appWorkId + " already schedled for " +
                        "cleanup, no further processing");
                continue;
            }
            ApplicationId appWorkAppId = appWorkId.getApplicationId();
            if (finishedApplications.contains(appWorkAppId)) {
                log.info("AppWork " + appWorkId +
                        " belongs to an application that is already killed, " +
                        "no further processing");
                continue;
            } else if (!runningApplications.contains(appWorkAppId)) {
                log.debug("AppWork {} is the first AppWork get launched for" +
                        " application {}", appWorkId, appWorkAppId);
                handleRunningAppOnNode(this, context, appWorkAppId, agentId);
            }

            if (remoteAppWork.getState() == RemoteAppWorkState.RUNNING) {
                ++numRemoteRunningAppWorks;
                if (!launchedAppWorks.contains(appWorkId)) {
                    launchedAppWorks.add(appWorkId);
                    newlyLaunchedAppWorks.add(remoteAppWork);
                }
                boolean needUpdate = false;
                if (!updateExistAppWorks.containsKey(appWorkId)) {
                    needUpdate = true;
                }
                if (needUpdate) {
                    updateExistAppWorks.put(appWorkId, remoteAppWork);
                    needUpdateAppWorks.add(new Pair<>(appWorkAppId, remoteAppWork));
                }
            } else {
                launchedAppWorks.remove(appWorkId);
                if (completedAppWorks.add(appWorkId)) {
                    newlyCompletedAppWorks.add(remoteAppWork);
                }
            }
        }
        List<AppWorkStatus> lostAppWorks = findLostAppWorks(numRemoteRunningAppWorks, appWorkStatuses);
        for (AppWorkStatus remoteAppWork : lostAppWorks) {
            AppWorkId appWorkId = remoteAppWork.getAppWorkId();
            if (completedAppWorks.add(appWorkId)) {
                newlyCompletedAppWorks.add(remoteAppWork);
            }
        }
        if (newlyLaunchedAppWorks.size() != 0 ||
                newlyCompletedAppWorks.size() != 0 ||
                needUpdateAppWorks.size() != 0) {
            nodeUpdateQueue.add(new UpdateAppWorkInfo(newlyLaunchedAppWorks, newlyCompletedAppWorks, needUpdateAppWorks));
        }
    }

    private List<AppWorkStatus> findLostAppWorks(int numRemoteRunning, List<AppWorkStatus> appWorkStatuses) {
        if (numRemoteRunning >= launchedAppWorks.size()) {
            return Collections.emptyList();
        }
        Set<AppWorkId> nodeAppWorks = new HashSet<>(numRemoteRunning);
        List<AppWorkStatus> lostAppWorks = new ArrayList<>(launchedAppWorks.size() - numRemoteRunning);
        for (AppWorkStatus remoteAppWork : appWorkStatuses) {
            if (remoteAppWork.getState() == RemoteAppWorkState.RUNNING) {
                nodeAppWorks.add(remoteAppWork.getAppWorkId());
            }
        }
        Iterator<AppWorkId> iterator = launchedAppWorks.iterator();
        while (iterator.hasNext()) {
            AppWorkId appWorkId = iterator.next();
            if (!nodeAppWorks.contains(appWorkId)) {
                String msg = " AppWork " + appWorkId + " was running but not reported form " + agentId;
                log.warn(msg);
                lostAppWorks.add(SchedulerUtils.createAbnormalSchedulerUnitStatus(appWorkId, msg));
                iterator.remove();
            }
        }
        return lostAppWorks;
    }


    private static void handleRunningAppOnNode(NodeImp node, DolphinContext context,
                                               ApplicationId applicationId, AgentId agentId) {
        App app = context.getApps().get(applicationId);
        if (app == null) {
            log.warn("Can't get App by appId=" + applicationId
                    + ", added it to finished application list for cleanup");
            node.finishedApplications.add(applicationId);
            node.runningApplications.remove(applicationId);
            return;
        }
        node.runningApplications.add(applicationId);
        context.getDolphinDispatcher().getEventProcessor().
                process(new AppRunningOnNodeEvent(applicationId, agentId));
    }

    @StateMachineParameters(stateType = NodeState.class, eventType = NodeEventType.class, contextType = Channel.class)
    static class NodeStateMachine extends AbstractUntypedStateMachine {

        protected void addNode(NodeState from, NodeState to, NodeEventType type, Channel ch) {
            AgentStartedEvent startEvent = (AgentStartedEvent) ch.event;

            AgentId agentId = ch.node.agentId;
            List<AgentAppWorkStatus> appWorkStatuses = startEvent.getAppWorkStatuses();
            if (appWorkStatuses != null && !appWorkStatuses.isEmpty()) {
                for (AgentAppWorkStatus appWork : appWorkStatuses) {
                    if (appWork.getAppWorkState() == RemoteAppWorkState.RUNNING) {
                        ch.node.launchedAppWorks.add(appWork.getAppWorkId());
                    }
                }
            }

            if (startEvent.getRunningApplications() != null) {
                for (ApplicationId applicationId : startEvent.getRunningApplications()) {
                    handleRunningAppOnNode(ch.node, ch.node.context, applicationId, agentId);
                }
            }

            ch.node.context.getDolphinDispatcher()
                    .getEventProcessor()
                    .process(new NodeAddedSchedulerEvent(ch.node, appWorkStatuses));
        }

        protected void updateResource(NodeState from, NodeState to, NodeEventType type, Channel ch) {
            log.warn("Try to update resource on a " + getCurrentState() + " node: " + ch.node.toString());
            NodeResourceUpdateEvent resourceUpdateEvent = (NodeResourceUpdateEvent) ch.event;
            ch.node.totalCapability = resourceUpdateEvent.getUpdateResource();
        }

        protected void addAppWorksToBeRemoved(NodeState from, NodeState to, NodeEventType type, Channel ch) {
            ch.node.appWorksToBeRemovedFromAgent.
                    addAll(((NodeFinishedAppWorksPulledByAMEvent) ch.event).getAppWorks());
        }

        protected void statusUpdate(NodeState from, NodeState to, NodeEventType type, Channel ch) {
//            NodeStatusEvent statusEvent = (NodeStatusEvent) ch.event;
//            ch.node.handleAppWorkStatus(statusEvent.getAppWorks());
//            ch.node.context.getDolphinDispatcher()
//                    .getEventProcessor().process(new NodeUpdateSchedulerEvent(ch.node));
        }
    }

    class Channel {
        final NodeImp node;
        final NodeEvent event;

        public Channel(NodeImp node, NodeEvent event) {
            this.node = node;
            this.event = event;
        }
    }
}
