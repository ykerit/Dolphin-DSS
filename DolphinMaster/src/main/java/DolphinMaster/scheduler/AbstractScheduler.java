package DolphinMaster.scheduler;

import DolphinMaster.ClusterNodeTracker;
import DolphinMaster.DolphinContext;
import common.struct.ApplicationId;
import agent.appworkmanage.appwork.AppWork;
import common.exception.DolphinException;
import common.resource.Resource;
import common.resource.Resources;
import common.service.AbstractService;
import common.struct.AgentId;
import common.struct.Priority;
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

    protected final ClusterNodeTracker nodeTracker = new ClusterNodeTracker();
    protected Resource minimumAllocation;
    protected volatile DolphinContext context;
    private volatile Priority maxClusterLevelAppPriority;

    protected volatile long lastNodeUpdateTime;

    protected final long THRAD_JOIN_TIMEOUT = 1000;

    private volatile SystemClock clock;

    protected long updateInterval = -1L;
    Thread updateThread;
    private final Object updateThreadMonitor = new Object();
    private Timer releaseCache;

    protected ConcurrentMap<ApplicationId, SchedulerApplication> applications;
    protected int nmExpireInterval;

    private final static List<AppWork> EMPTY_APP_WORK_LIST = new ArrayList<>();
    protected static final Allocation EMPTY_ALLOCATION =
            new Allocation(EMPTY_APP_WORK_LIST, Resources.createResource(0), null, null);
    protected final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
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
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
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
    public SchedulerAppReport getSchedulerAppInfo(AppDescribeId appDescribeId) {
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

    SchedulerApplication getApplicationDescribe(AppDescribeId appDescribeId) {
        return null;
    }
}
