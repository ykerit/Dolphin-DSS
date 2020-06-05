package DolphinMaster.scheduler;

import DolphinMaster.ClusterNodeTracker;
import DolphinMaster.DolphinContext;
import DolphinMaster.node.*;
import DolphinMaster.schedulerunit.SchedulerUnit;
import DolphinMaster.schedulerunit.SchedulerUnitEventType;
import common.struct.AppWorkStatus;
import api.app_master_message.ResourceRequest;
import common.event.EventProcessor;
import common.exception.DolphinException;
import common.resource.Resource;
import common.resource.Resources;
import common.service.AbstractService;
import common.struct.*;
import common.util.SystemClock;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractScheduler extends AbstractService implements ResourceScheduler, EventProcessor<SchedulerEvent> {

    private static final Logger log = LogManager.getLogger(AbstractScheduler.class);

    private static final Resource ZERO_RESOURCE = Resource.newInstance(0, 0);
    private final static List<RemoteAppWork> EMPTY_APP_WORK_LIST = new ArrayList<>();
    protected static final Allocation EMPTY_ALLOCATION =
            new Allocation(EMPTY_APP_WORK_LIST, Resources.createResource(0), null, null);

    protected final ClusterNodeTracker nodeTracker = new ClusterNodeTracker();
    protected final long THREAD_JOIN_TIMEOUT = 1000L;
    protected final Lock readLock;
    private final Lock writeLock;
    private final Object updateThreadMonitor = new Object();

    protected Resource minimumAllocation;
    protected volatile DolphinContext context;
    protected volatile long lastNodeUpdateTime;
    protected long updateInterval = -1L;
    protected ConcurrentMap<ApplicationId, SchedulerApplication> applications;
    protected int expireInterval;
    Thread updateThread;
    private volatile Priority maxClusterLevelAppPriority;
    private volatile SystemClock clock;
    private Timer releaseCache;
    private boolean autoUpdateAppWorks = false;

    public AbstractScheduler(String name) {
        super(name);
        clock = SystemClock.getInstance();
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    protected void serviceInit() throws Exception {
        if (updateInterval > 0) {
            updateThread = new UpdateThread();
            updateThread.setName("SchedulerUpdateThread");
            updateThread.setDaemon(true);
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        if (this.updateThread != null) {
            this.updateThread.start();
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        if (updateThread != null) {
            updateThread.interrupt();
            updateThread.join(THREAD_JOIN_TIMEOUT);
        }
        super.serviceStop();
    }

    public ClusterNodeTracker getNodeTracker() {
        return nodeTracker;
    }

    @Override
    public void reinitialize(Configuration configuration, DolphinContext context) throws IOException {

    }

    @Override
    public Resource getClusterResource() {
        return nodeTracker.getClusterCapacity();
    }

    @Override
    public Resource getMinimumResourceCapability() {
        return minimumAllocation;
    }

    @Override
    public Resource getMaximumResourceCapability() {
        return nodeTracker.getMaxAllowedAllocation();
    }

    @Override
    public Resource getMaximumResourceCapability(String pool) {
        return getMaximumResourceCapability();
    }

    @Override
    public void preValidateMoveApplication(ApplicationId appid, String newPool) throws DolphinException {

    }

    @Override
    public SchedulerAppReport getSchedulerAppInfo(ApplicationId applicationId) {
        SchedulerApplication application = getApplication(applicationId);
        if (application == null) {
            log.debug("Request for appInfo is unknown SchedulerApplicaiton {}", applicationId);
            return null;
        }
        return new SchedulerAppReport(application);
    }

    @Override
    public void moveAllApps(String src, String dst) throws DolphinException {
        writeLock.lock();
        try {
            try {
                getPoolInfo(dst, false, false);
            } catch (IOException e) {
                log.warn(e.toString());
            }
            for (ApplicationId applicationId : getAppsFormPool(src)) {

            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void killAllAppsInPool(String pool) throws DolphinException {
        writeLock.lock();
        try {
            for (ApplicationId applicationId : getAppsFormPool(pool)) {

            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removePool(String pool) throws DolphinException {

    }

    @Override
    public void addPool(ResourcePool pool) throws DolphinException {

    }

    @Override
    public void setClusterMaxPriority() throws DolphinException {

    }

    @Override
    public void getClusterMaxPriority() {

    }

    @Override
    public SchedulerNode getNode(AgentId agentId) {
        return nodeTracker.getNode(agentId);
    }

    @Override
    public SchedulerUnit getSchedulerUnit(AppWorkId id) {
        SchedulerApplication application = getCurrentAppForAppWork(id);
        return application == null ? null : application.getSchedulerUnit(id);
    }

    @Override
    public SchedulerNodeReport getNodeReport(AgentId id) {
        return nodeTracker.getNodeReport(id);
    }

    protected void appWorkLaunchedOnNode(AppWorkId appWorkId, SchedulerNode node) {
        readLock.lock();
        try {
            SchedulerApplication application = getCurrentAppForAppWork(appWorkId);
            if (application == null) {
                log.info("Unknown application " + appWorkId.getApplicationId() +
                        "launched AppWork " + appWorkId);
                this.context.getDolphinDispatcher().
                        getEventProcessor().process(new NodeCleanAppWorkEvent(node.getNodeId(), appWorkId));
                return;
            }
            application.appWorkLaunchOnNode(appWorkId, node.getNodeId());
            node.appWorkStarted(appWorkId);
        } finally {
            readLock.unlock();
        }
    }

    protected void AppWorkIncreaseOnNode(AppWorkId appWorkId, SchedulerNode node, RemoteAppWork increasedAppWorkReport) {
        SchedulerApplication application = getCurrentAppForAppWork(appWorkId);
        if (application == null) {
            log.info("Unknown application " + appWorkId.getApplicationId() +
                    " increased AppWork: " + appWorkId + " on node:" + node);
            this.context.getDolphinDispatcher().getEventProcessor()
                    .process(new NodeCleanAppWorkEvent(node.getNodeId(), appWorkId));
            return;
        }
        SchedulerUnit schedulerUnit = getSchedulerUnit(appWorkId);

    }


    public SchedulerApplication getCurrentAppForAppWork(AppWorkId appWorkId) {
        return getApplication(appWorkId.getApplicationId());
    }

    public SchedulerApplication getApplication(ApplicationId applicationId) {
        return applications.get(applicationId);
    }

    @Override
    public Resource getNormalizedResource(Resource reqRes, Resource maxResourceCapability) {
        return SchedulerUtils.getNormalizeResource(reqRes, getResourceCalculator(),
                getMinimumResourceCapability(), maxResourceCapability, getMinimumResourceCapability());
    }

    protected void normalizedResourceRequests(List<ResourceRequest> asks) {
        normalizedResourceRequests(asks, null);
    }

    protected void normalizedResourceRequests(List<ResourceRequest> asks, String poolName) {
        Resource maxAllocation = getMaximumResourceCapability(poolName);
        for (ResourceRequest resourceRequest : asks) {
            resourceRequest.setCapability(getNormalizedResource(resourceRequest.getCapability(), maxAllocation));
        }
    }

    protected void releaseScheduleUnit(List<AppWorkId> appWorkIds, SchedulerApplication application) {
        for (AppWorkId id : appWorkIds) {
            SchedulerUnit schedulerUnit = getSchedulerUnit(id);
            if (schedulerUnit != null) {

            }
        }
    }

    protected void nodeUpdate(Node node) {
        log.debug("nodeUpdate: {} cluster capacity: {}", node, getClusterResource());
        SchedulerNode schedulerNode = getNode(node.getNodeId());
        List<AppWorkStatus> completedAppWorks = updateNewAppWorkInfo(node, schedulerNode);
        Resource realeasedResource = Resource.newInstance(0, 0);
        int relaseedAppWorks = updateCompletedAppWorks(completedAppWorks, realeasedResource, schedulerNode.getNodeId(), schedulerNode);

        if (node.getState() == NodeState.DECOMMISSIONED && schedulerNode != null) {
            context.getDolphinDispatcher().
                    getEventProcessor().process(new NodeResourceUpdateEvent(node.getNodeId(), schedulerNode.getAllocatedResource()));
        }
    }

    public void completedAppWork(SchedulerUnit unit, AppWorkStatus appWorkStatus, SchedulerUnitEventType type) {
        if (unit == null) {
            log.info("AppWork " + appWorkStatus.getAppWorkId() + " completed with event " + type +
                    " but SchedulerUnit not existed");
            return;
        }

    }

    private List<AppWorkStatus> updateNewAppWorkInfo(Node node, SchedulerNode schedulerNode) {
        List<UpdateAppWorkInfo> appWorkInfoList = node.pullAppWorkUpdates();
        List<AppWorkStatus> newlyLaunchedAppWorks = new ArrayList<>();
        List<AppWorkStatus> completedAppWorks = new ArrayList<>();
        return completedAppWorks;
    }

    private int updateCompletedAppWorks(List<AppWorkStatus> completedAppWorks, Resource releasedResource, AgentId agentId, SchedulerNode node) {
        int releasedAppWorks = 0;
        List<AppWorkId> untrackedAppWorkIds = new ArrayList<>();
        for (AppWorkStatus completedAppWork : completedAppWorks) {
            AppWorkId appWorkId = completedAppWork.getAppWorkId();
            log.debug("AppWork Finished: {]", appWorkId);
            SchedulerUnit schedulerUnit = getSchedulerUnit(appWorkId);
            completedAppWork(schedulerUnit, completedAppWork, SchedulerUnitEventType.FINISHED);
            if (node != null) {
                node.releaseSchedulerUnit(appWorkId, true);
            }
            if (schedulerUnit != null) {
                ++releasedAppWorks;
                Resource ars = schedulerUnit.getAllocatedResource();
                if (ars != null) {
                    Resources.addTo(releasedResource, ars);
                }
                Resource resource = schedulerUnit.getReservedResource();
                if (resource != null) {
                    Resources.addTo(releasedResource, resource);
                }
            } else {
                untrackedAppWorkIds.add(appWorkId);
            }
        }
        return releasedAppWorks;
    }

    public void updateNodeResource(Node node, Resource resource) {
        writeLock.lock();
        try {
            SchedulerNode schedulerNode = getNode(node.getNodeId());
            Resource oldResource = schedulerNode.getTotalResource();
            if (!oldResource.equals(resource)) {
                log.info("Update resource on node: {} from: {}, to: {}", schedulerNode.getNodeName(), oldResource, resource);
                nodeTracker.removeNode(node.getNodeId());
                schedulerNode.updateTotalResource(resource);
                nodeTracker.addNode(schedulerNode);
            } else {
                log.warn("Update resource on node: {} with the same resource: {}", schedulerNode.getNodeName(), resource);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void recoverAppWorksOnNode(List<AppWorkStatus> appWorkReports, Node node) {

    }

    public Resource getMinimumAllocation() {
        Resource ret = Resource.newInstance(10000, 8);
        return ret;
    }

    public Resource getMaximumAllocation() {
        return Resource.newInstance(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    protected void initMaximumResourceCapability(Resource maxAllocation) {
        nodeTracker.setConfiguredMaxAllocation(maxAllocation);
    }

    public abstract void killAppWork(SchedulerUnit schedulerUnit);

    protected abstract void completedAppWorkInterval(SchedulerUnit schedulerUnit, AppWorkStatus appWorkStatus, SchedulerUnitEventType eventType);

    protected void update() {
    }

    private class UpdateThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (updateThreadMonitor) {
                        updateThreadMonitor.wait(updateInterval);
                        update();
                    }
                } catch (InterruptedException e) {
                    log.warn("Scheduler updateThread interrupted, Exiting");
                    return;
                }
            }
        }
    }

    private List<ApplicationId> getAppsFormPool(String pool) {
        List<ApplicationId> apps = getAppsInPool(pool);
        if (apps == null) {
            log.warn("The specified pool: " + pool + "not exist.");
        }
        return apps;
    }
}
