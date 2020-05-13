package DolphinMaster.scheduler;

import DolphinMaster.ClusterNodeTracker;
import DolphinMaster.DolphinContext;
import DolphinMaster.node.Node;
import DolphinMaster.node.NodeCleanAppWorkEvent;
import DolphinMaster.schedulerunit.SchedulerUnit;
import DolphinMaster.schedulerunit.SchedulerUnitState;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractScheduler extends AbstractService implements ResourceScheduler {

    private static final Logger log = LogManager.getLogger(AbstractScheduler.class);

    private static final Resource ZERO_RESOURCE = Resource.newInstance(0, 0);
    private final static List<RemoteAppWork> EMPTY_APP_WORK_LIST = new ArrayList<>();
    protected static final Allocation EMPTY_ALLOCATION =
            new Allocation(EMPTY_APP_WORK_LIST, Resources.createResource(0), null, null);
    protected final ClusterNodeTracker nodeTracker = new ClusterNodeTracker();
    protected final long THRAD_JOIN_TIMEOUT = 1000;
    protected final ReentrantReadWriteLock.ReadLock readLock;
    private final Object updateThreadMonitor = new Object();
    private final ReentrantReadWriteLock.WriteLock writeLock;
    protected Resource minimumAllocation;
    protected volatile DolphinContext context;
    protected volatile long lastNodeUpdateTime;
    protected long updateInterval = -1L;
    protected ConcurrentMap<ApplicationId, SchedulerApplication> applications;
    protected int nmExpireInterval;
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
            updateThread.join(1000L);
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
        return null;
    }

    @Override
    public void moveAllApps(String src, String dst) throws DolphinException {

    }

    @Override
    public void killAllAppsInPool(String pool) throws DolphinException {

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
    public SchedulerNode getSchedulerNode(AgentId agentId) {
        return null;
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

    private void killOrphanAppWorkOnNode(Node node, SchedulerUnitState schedulerUnit) {
        if (!schedulerUnit.equals(RemoteAppWorkState.COMPLETE)) {
            this.context.getDolphinDispatcher().getEventProcessor()
                    .process(new NodeCleanAppWorkEvent(node.getNodeId(), schedulerUnit));
        }
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
}
