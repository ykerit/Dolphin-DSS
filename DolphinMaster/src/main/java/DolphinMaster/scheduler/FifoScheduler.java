package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import DolphinMaster.app.AppState;
import DolphinMaster.node.Node;
import DolphinMaster.scheduler.event.*;
import DolphinMaster.scheduler.fica.FicaSchedulerNode;
import DolphinMaster.schedulerunit.SchedulerUnit;
import DolphinMaster.schedulerunit.SchedulerUnitEventType;
import DolphinMaster.schedulerunit.SchedulerUnitState;
import api.app_master_message.ResourceRequest;
import common.exception.DolphinException;
import common.exception.DolphinRuntimeException;
import common.resource.DefaultResourceCalculator;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.resource.Resources;
import common.struct.*;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class FifoScheduler extends AbstractScheduler {

    private static final Logger log = LogManager.getLogger(FifoScheduler.class);
    private static final String DEFAULT_QUEUE_NAME = "default";
    private final ResourceCalculator resourceCalculator = new DefaultResourceCalculator();
    private boolean usePortForNodeName;
    private PoolMetrics metrics;
    private Resource usedResource = Resource.newInstance(0, 0);
    private final ResourcePool DEFAULT_QUEUE = new ResourcePool() {
        @Override
        public String getPoolName() {
            return DEFAULT_QUEUE_NAME;
        }

        @Override
        public PoolMetrics getPoolMetrics() {
            return metrics;
        }

        @Override
        public PoolInfo getPoolInfo(
                boolean includeChildQueues, boolean recursive) {
            PoolInfo poolInfo = new PoolInfo();
            poolInfo.setPoolName(DEFAULT_QUEUE.getPoolName());
            poolInfo.setCapacity(1.0f);
            Resource clusterResource = getClusterResource();
            if (clusterResource.getMemorySize() == 0) {
                poolInfo.setCurrentCapacity(0.0f);
            } else {
                poolInfo.setCurrentCapacity((float) usedResource.getMemorySize()
                        / clusterResource.getMemorySize());
            }
            poolInfo.setMaximumCapacity(1.0f);
            poolInfo.setChildQueues(new ArrayList<PoolInfo>());
            poolInfo.setPoolState(PoolState.RUNNING);
            return poolInfo;
        }

        @Override
        public void recoverAppWork(Resource clusterResource, SchedulerApplication schedulerApplication, SchedulerUnit schedulerUnit) {
            if (schedulerUnit.getState().equals(SchedulerUnitState.COMPLETED)) {
                return;
            }
            increaseUsedResources(schedulerUnit);
            updateAppHeadRoom(schedulerApplication);
            updateAvailableResourcesMetrics();
        }


        @Override
        public void incPendingResource(String nodeLabel, Resource resourceToInc) {
        }

        @Override
        public void decPendingResource(String nodeLabel, Resource resourceToDec) {
        }

        @Override
        public Priority getApplicationPriority() {
            // TODO add implementation for FIFO scheduler
            return null;
        }

        @Override
        public void incReserveResource(String partition, Resource reservedRes) {
            // TODO add implementation for FIFO scheduler

        }

        @Override
        public void decReserveResource(String partition, Resource reservedRes) {
            // TODO add implementation for FIFO scheduler

        }
    };

    public FifoScheduler() {
        super(FifoScheduler.class.getName());
    }

    private synchronized void initScheduler(Configuration conf) throws DolphinException {
        validateConf(conf);
        //Use ConcurrentSkipListMap because applications need to be ordered
        this.applications =
                new ConcurrentSkipListMap<>();
        this.minimumAllocation = super.getMinimumAllocation();
        initMaximumResourceCapability(super.getMaximumAllocation());
    }

    @Override
    public void setDolphinContext(DolphinContext context) {
        this.context = context;
    }

    @Override
    public void serviceInit() throws Exception {
        initScheduler(context.getConfiguration());
        super.serviceInit();

        // Initialize SchedulingMonitorManager
    }

    @Override
    public void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    public void serviceStop() throws Exception {
        super.serviceStop();
    }

    private void validateConf(Configuration conf) throws DolphinException {
        // validate scheduler memory allocation setting
        int minMem = conf.SCHEDULER_MINIMUM_ALLOCATION_MB;
        int maxMem = conf.SCHEDULER_MAXIMUM_ALLOCATION_MB;

        if (minMem <= 0 || minMem > maxMem) {
            throw new DolphinRuntimeException("Invalid resource scheduler memory"
                    + " allocation configuration"
                    + ", " + conf.SCHEDULER_MINIMUM_ALLOCATION_MB
                    + "=" + minMem
                    + ", " + conf.SCHEDULER_MAXIMUM_ALLOCATION_MB
                    + "=" + maxMem + ", min and max should be greater than 0"
                    + ", max should be no smaller than min.");
        }
    }

    @Override
    public int getNumClusterNodes() {
        return nodeTracker.nodeCount();
    }

    public synchronized void setContext(DolphinContext context) {
        this.context = context;
    }

    @Override
    public synchronized void
    reinitialize(Configuration configuration, DolphinContext context) throws IOException {
        super.reinitialize(configuration, context);
    }

    @Override
    public Allocation allocate(ApplicationId applicationId, List<ResourceRequest> ask, List<AppWorkId> release) {
        SchedulerApplication application = getApplication(applicationId);
        if (application == null) {
            log.error("Calling allocate on removed or non existent application " +
                    applicationId);
            return EMPTY_ALLOCATION;
        }

        // The allocate may be the leftover from previous attempt, and it will
        // impact current attempt, such as confuse the request and allocation for
        // current attempt's AM container.
        // Note outside precondition check for the attempt id may be
        // outdated here, so double check it here is necessary.
        if (!application.getApplicationId().equals(applicationId)) {
            log.error("Calling allocate on previous or removed " +
                    "or non existent application attempt " + applicationId);
            return EMPTY_ALLOCATION;
        }

        // Sanity check
        normalizedResourceRequests(ask);

        // Release SchedulerUnit
        releaseScheduleUnit(release, application);

        synchronized (application) {

            // make sure we aren't stopping/removing the application
            // when the allocate comes in
            if (application.isStopped()) {
                log.info("Calling allocate on a stopped " +
                        "application " + applicationId);
                return EMPTY_ALLOCATION;
            }

            if (!ask.isEmpty()) {
                log.debug("allocate: pre-update" +
                        " applicationId=" + applicationId +
                        " application=" + application);

                // Update application requests
                application.updateResourceRequests(ask);

                log.debug("allocate: post-update" +
                        " applicationId=" + applicationId +
                        " application=" + application);

                log.debug("allocate:" +
                        " applicationId=" + applicationId +
                        " #ask=" + ask.size());
            }

            Resource headroom = application.getHeadRoom();
            return new Allocation(application.pullNewlyAllocatedAppWorks(),
                    headroom, null, null);
        }
    }

    public synchronized void addApplication(ApplicationId applicationId,
                                            String queue, String user) {
        SchedulerApplication application =
                new SchedulerApplication(applicationId, user, DEFAULT_QUEUE, context);
        applications.put(applicationId, application);
        log.info("Accepted application " + applicationId + " from user: " + user
                + ", currently num of applications: " + applications.size());
        context.getDolphinDispatcher().getEventProcessor()
                .process(new AppEvent(applicationId, AppEventType.APP_ACCEPTED));
    }

    private synchronized void doneApplication(ApplicationId applicationId,
                                              AppState finalState) {
        SchedulerApplication application =
                applications.get(applicationId);
        if (application == null) {
            log.warn("Couldn't find application " + applicationId);
            return;
        }

        application.stop(finalState);
        applications.remove(applicationId);
    }

    /**
     * Heart of the scheduler...
     *
     * @param node node on which resources are available to be allocated
     */
    private void assignAppWorks(FicaSchedulerNode node) {
        log.debug("assignContainers:" +
                " node=" + node.getNode().getNodeAddress() +
                " #applications=" + applications.size());

        // Try to assign containers to applications in fifo order
        for (Map.Entry<ApplicationId, SchedulerApplication> e : applications
                .entrySet()) {
            SchedulerApplication application = e.getValue();
            if (application == null) {
                continue;
            }

            log.debug("pre-assignContainers");
            synchronized (application) {
                int maxContainers =
                        getMaxAllocatableAppWorks(application, node);
                // Ensure the application needs containers of this priority
                if (maxContainers > 0) {
                    int assignedContainers =
                            assignAppWorksOnNode(node, application);
                    // Do not assign out of order w.r.t priorities
                    if (assignedContainers == 0) {
                        return;
                    }
                }
            }

            log.debug("post-assignContainers");

            // Done
            if (Resources.lessThan(resourceCalculator, getClusterResource(),
                    node.getUnAllocatedResource(), minimumAllocation)) {
                break;
            }
        }

        // Update the applications' headroom to correctly take into
        // account the containers assigned in this update.
        for (SchedulerApplication application : applications.values()) {
            if (application == null) {
                continue;
            }
            updateAppHeadRoom(application);
        }
    }

    // rack local off_switch
    private int getMaxAllocatableAppWorks(SchedulerApplication application, FicaSchedulerNode node) {
        ResourceRequest resourceRequest = application.getPendingAsk();
        int maxApppWorks = resourceRequest.getNumAppWorks();
        return maxApppWorks;
    }

    private int assignAppWorksOnNode(FicaSchedulerNode node, SchedulerApplication application) {
        // Off-switch
        int offSwitchContainers = assignOffSwitchContainers(node, application);


        log.debug("assignContainersOnNode:" +
                " node=" + node.getNode().getNodeAddress() +
                " application=" + application.getApplicationId().getId() +
                " #assigned=" + offSwitchContainers);


        return offSwitchContainers;
    }

    private int assignOffSwitchContainers(FicaSchedulerNode node,
                                          SchedulerApplication application) {
        int assignedContainers = 0;
        ResourceRequest resourceRequest = application.getPendingAsk();
        int maxApppWorks = resourceRequest.getNumAppWorks();
        if (maxApppWorks > 0) {
            assignedContainers = assignAppWork(node, application,
                    maxApppWorks, resourceRequest.getCapability());
        }
        return assignedContainers;
    }

    private int assignAppWork(FicaSchedulerNode node, SchedulerApplication application, int assignableAppWorks,
                              Resource capability) {
        log.debug("assignContainers:" +
                " node=" + node.getNode().getNodeAddress() +
                " application=" + application.getApplicationId().getId() +
                " assignableContainers=" + assignableAppWorks +
                " capability=" + capability);

        // TODO: A buggy application with this zero would crash the scheduler.
        int availableAppWorks =
                (int) (node.getUnAllocatedResource().getMemorySize() /
                        capability.getMemorySize());
        int assignedAppWorks = Math.min(assignableAppWorks, availableAppWorks);

        if (assignedAppWorks > 0) {
            for (int i = 0; i < assignedAppWorks; ++i) {

                AgentId nodeId = node.getNodeId();
                AppWorkId appWorkId = new AppWorkId(application.getApplicationId(), application.getNewAppWorkId());
                RemoteAppWork appWork = new RemoteAppWork(appWorkId,
                        new AgentId(node.getNode().getNodeAddress(), node.getNode().getCommandPort()),
                        capability, null, null);

                // Allocate!

                // Inform the application
                SchedulerUnit schedulerUnit = application.allocate(node, appWork);

                // Inform the node
                node.allocateSchedulerUnit(schedulerUnit);

                // Update usage for this container
                increaseUsedResources(schedulerUnit);
            }

        }

        return assignedAppWorks;
    }

    private void increaseUsedResources(SchedulerUnit schedulerUnit) {
        Resources.addTo(usedResource, schedulerUnit.getAllocatedResource());
    }

    private void updateAppHeadRoom(SchedulerApplication application) {
        application.setHeadRoom(Resources.subtract(getClusterResource(),
                usedResource));
    }

    private void updateAvailableResourcesMetrics() {

    }

    @Override
    public void process(SchedulerEvent event) {
        switch (event.getType()) {
            case NODE_ADDED: {
                NodeAddedSchedulerEvent nodeAddedEvent = (NodeAddedSchedulerEvent) event;
                addNode(nodeAddedEvent.getNode());
                recoverAppWorksOnNode(nodeAddedEvent.getAppWorkReport(),
                        nodeAddedEvent.getNode());

            }
            break;
            case NODE_REMOVED: {
                NodeRemovedSchedulerEvent nodeRemovedEvent = (NodeRemovedSchedulerEvent) event;
                removeNode(nodeRemovedEvent.getRemovedNode());
            }
            break;
            case NODE_RESOURCE_UPDATE: {
                NodeResourceUpdateSchedulerEvent nodeResourceUpdatedEvent =
                        (NodeResourceUpdateSchedulerEvent) event;
                updateNodeResource(nodeResourceUpdatedEvent.getNode(),
                        nodeResourceUpdatedEvent.getUpdateResource());
            }
            break;
            case NODE_UPDATE: {
                NodeUpdateSchedulerEvent nodeUpdatedEvent =
                        (NodeUpdateSchedulerEvent) event;
                nodeUpdate(nodeUpdatedEvent.getNode());
            }
            break;
            case APP_ADDED: {
                AppAddedSchedulerEvent appAddedEvent = (AppAddedSchedulerEvent) event;
                addApplication(appAddedEvent.getApplicationId(),
                        appAddedEvent.getPool(), appAddedEvent.getUser());
            }
            break;
            case APP_REMOVED: {
                AppRemovedSchedulerEvent appRemovedEvent = (AppRemovedSchedulerEvent) event;
                doneApplication(appRemovedEvent.getApplicationId(),
                        appRemovedEvent.getAppState());
            }
            break;
            case RELEASE_SCHEDULER_UNIT: {
                if (!(event instanceof ReleaseSchedulerUnitEvent)) {
                    throw new RuntimeException("Unexpected event type: " + event);
                }
                SchedulerUnit schedulerUnit = ((ReleaseSchedulerUnitEvent) event).getSchedulerUnit();
                completedAppWork(schedulerUnit,
                        SchedulerUtils.createAbnormalSchedulerUnitStatus(
                                schedulerUnit.getAppWorkId(),
                                SchedulerUtils.RELEASED_SCHEDULER_UNIT),
                        SchedulerUnitEventType.RELEASED);
            }
            break;
            default:
                log.error("Invalid eventtype " + event.getType() + ". Ignoring!");
        }
    }

    @Override
    protected synchronized void completedAppWorkInterval(SchedulerUnit schedulerUnit, AppWorkStatus appWorkStatus, SchedulerUnitEventType eventType) {

        // Get the application for the finished container
        RemoteAppWork appWork = schedulerUnit.getAppWork();
        SchedulerApplication application = getCurrentAppForAppWork(appWork.getAppWorkId());
        ApplicationId appId = appWork.getAppWorkId().getApplicationId();

        // Get the node on which the container was allocated
        FicaSchedulerNode node = (FicaSchedulerNode) getNode(appWork.getAgentId());

        if (application == null) {
            log.info("Unknown application: " + appId +
                    " released AppWork " + appWork.getAppWorkId() +
                    " on node: " + node +
                    " with event: " + eventType);
            return;
        }

        // Inform the application
        application.appWorkCompleted(schedulerUnit, appWorkStatus, eventType);

        // Inform the node
        node.releaseSchedulerUnit(schedulerUnit.getAppWorkId(), false);

        // Update total usage
        Resources.subtractFrom(usedResource, appWork.getResource());

        log.info("Application " + application.getApplicationId() +
                " released AppWork " + appWork.getAppWorkId() +
                " on node: " + node +
                " with event: " + eventType);

    }

    private synchronized void removeNode(Node nodeInfo) {
        FicaSchedulerNode node = (FicaSchedulerNode) nodeTracker.getNode(nodeInfo.getNodeId());
        if (node == null) {
            return;
        }
        // Kill running containers
        for (SchedulerUnit schedulerUnit : node.getCopiedListOfRunningSchedulerUnit()) {
            super.completedAppWork(schedulerUnit,
                    SchedulerUtils.createAbnormalSchedulerUnitStatus(
                            schedulerUnit.getAppWorkId(),
                            SchedulerUtils.LOST_SCHEDULER_UNIT),
                    SchedulerUnitEventType.KILL);
        }
        nodeTracker.removeNode(nodeInfo.getNodeId());
    }

    @Override
    public PoolInfo getPoolInfo(String poolName,
                                boolean includeChildPools, boolean recursive) {
        return DEFAULT_QUEUE.getPoolInfo(false, false);
    }

    @Override
    public ResourceCalculator getResourceCalculator() {
        return resourceCalculator;
    }

    private synchronized void addNode(Node node) {
        FicaSchedulerNode schedulerNode = new FicaSchedulerNode(node);
        nodeTracker.addNode(schedulerNode);
    }

    @Override
    public SchedulerUnit getSchedulerUnit(AppWorkId appWorkId) {
        SchedulerApplication application = getCurrentAppForAppWork(appWorkId);
        return (application == null) ? null : application.getSchedulerUnit(appWorkId);
    }

    @Override
    public String moveApplication(ApplicationId appId, String newPool) throws DolphinException {
        return null;
    }

    @Override
    public PoolMetrics getRootPoolMetrics() {
        return DEFAULT_QUEUE.getPoolMetrics();
    }


    @Override
    public synchronized List<ApplicationId> getAppsInPool(String poolName) {
        return new ArrayList<>(applications.keySet());
    }

    public Resource getUsedResource() {
        return usedResource;
    }

    @Override
    protected synchronized void nodeUpdate(Node node) {
        super.nodeUpdate(node);

        FicaSchedulerNode ficaNode = (FicaSchedulerNode) getNode(node.getNodeId());

        // A decommissioned node might be removed before we get here
        if (ficaNode != null &&
                Resources.greaterThanOrEqual(resourceCalculator, getClusterResource(),
                        ficaNode.getUnAllocatedResource(), minimumAllocation)) {
            log.debug("Node heartbeat " + node.getNodeId() +
                    " available resource = " + ficaNode.getUnAllocatedResource());

            assignAppWorks(ficaNode);

            log.debug("Node after allocation " + node.getNodeId() + " resource = "
                    + ficaNode.getUnAllocatedResource());
        }

        updateAvailableResourcesMetrics();
    }

    @Override
    public void killAppWork(SchedulerUnit schedulerUnit) {
        AppWorkStatus status = SchedulerUtils.createKilledSchedulerUnitStatus(
                schedulerUnit.getAppWorkId(),
                "Killed by RM to simulate an AM container failure");
        log.info("Killing SchedulerUnit " + schedulerUnit);
        completedAppWork(schedulerUnit, status, SchedulerUnitEventType.KILL);
    }

    @Override
    public synchronized void recoverAppWorksOnNode(
            List<AppWorkStatus> containerReports, Node node) {
        super.recoverAppWorksOnNode(containerReports, node);
    }
}
