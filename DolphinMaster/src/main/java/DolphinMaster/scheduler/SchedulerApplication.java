package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import DolphinMaster.app.AppState;
import DolphinMaster.node.NodeCleanAppWorkEvent;
import DolphinMaster.scheduler.fica.FicaSchedulerNode;
import DolphinMaster.schedulerunit.*;
import common.struct.AppWorkStatus;
import api.app_master_message.ResourceRequest;
import common.context.ApplicationSubmission;
import common.resource.Resource;
import common.resource.Resources;
import common.struct.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SchedulerApplication {
    private static final Logger log = LogManager.getLogger(SchedulerApplication.class);
    protected final Map<AppWorkId, SchedulerUnit> liveAppWorks = new ConcurrentHashMap<>();
    protected final Lock readLock;
    protected final Lock writeLock;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DolphinContext context;
    private volatile Resource resourceLimit = new Resource(0, 0);
    private final boolean amRunning = false;
    private final ApplicationId applicationId;
    private App app;
    private volatile Priority appPriority = null;
    private final ResourceUsage appResourceUsage = new ResourceUsage();
    private final ResourceUsage opportunisticResourceUsage = new ResourceUsage();
    private final ResourceUsage resourceUsageAllocated = new ResourceUsage();
    private final AtomicLong firstAllocationRequestTime = new AtomicLong(0);
    private final AtomicLong firstAppWorkAllocatedTime = new AtomicLong(0);
    private final List<SchedulerUnit> newlyAllocatedSchedulerUnits = new ArrayList<>();
    private final List<SchedulerUnit> tempSchedulerUnitsToKill = new ArrayList<>();
    private final Map<AppWorkId, SchedulerUnit> newlyPromotedAppWorks = new HashMap<>();
    private final Map<AppWorkId, SchedulerUnit> newlyDemotedAppWorks = new HashMap<>();
    private final Map<AppWorkId, SchedulerUnit> newlyDecreaseAppWorks = new HashMap<>();
    private final Map<AppWorkId, SchedulerUnit> newlyIncreaseAppWorks = new HashMap<>();
    private Set<AppWorkId> pendingRelease = null;
    private String user;
    private final ResourcePool pool;
    private final boolean isStopped = false;
    private Map<String, String> applicationSchedulingEnvs = new HashMap<>();
    private final AtomicLong unconfirmedAllocatedMem = new AtomicLong();
    private final AtomicInteger unconfirmedAllocateVCore = new AtomicInteger();
    private final AtomicInteger appWorkIdCounter = new AtomicInteger(0);
    private List<ResourceRequest> resourceRequests;

    public SchedulerApplication(ApplicationId applicationId, String user,
                                ResourcePool pool, DolphinContext context) {
        this.context = context;
        this.pool = pool;
        this.pendingRelease = Collections.
                newSetFromMap(new ConcurrentHashMap<AppWorkId, Boolean>());
        this.applicationId = applicationId;
        if (context.getApps() != null &&
                context.getApps().containsKey(applicationId)) {
            this.app = context.getApps().get(applicationId);
            ApplicationSubmission submission = app.getApplicationSubmission();
            applicationSchedulingEnvs = app.getApplicationEnvs();
        }
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    public Collection<SchedulerUnit> getLiveAppWorks() {
        readLock.lock();
        try {
            return new ArrayList<>(liveAppWorks.values());
        } finally {
            readLock.unlock();
        }
    }

    public long getNewAppWorkId() {
        return this.appWorkIdCounter.incrementAndGet();
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public SchedulerUnit getSchedulerUnit(AppWorkId id) {
        return liveAppWorks.get(id);
    }

    public void addSchedulerUnit(AppWorkId id, SchedulerUnit schedulerUnit) {
        writeLock.lock();
        try {
            liveAppWorks.put(id, schedulerUnit);

        } finally {
            writeLock.unlock();
        }
    }

    public void removeSchedulerUnit(AppWorkId appWorkId) {
        writeLock.lock();
        try {
            SchedulerUnit schedulerUnit = liveAppWorks.remove(appWorkId);
            if (schedulerUnit != null) {
                // record something
            }
        } finally {
            writeLock.unlock();
        }
    }

    public ResourcePool getPool() {
        return pool;
    }

    public void appWorkLaunchOnNode(AppWorkId appWorkId, AgentId agentId) {
        writeLock.lock();
        try {
            SchedulerUnit schedulerUnit = getSchedulerUnit(appWorkId);
            if (schedulerUnit == null) {
                context.getDolphinDispatcher().getEventProcessor().
                        process(new NodeCleanAppWorkEvent(agentId, appWorkId));
                return;
            }
            schedulerUnit.
                    process(new SchedulerUnitEvent(appWorkId, SchedulerUnitEventType.LAUNCHED));
        } finally {
            writeLock.unlock();
        }
    }

    private RemoteAppWork updateAppWork(SchedulerUnit schedulerUnit, AppWorkUpdateType updateType) {
        RemoteAppWork appWork = schedulerUnit.getAppWork();
        AppWorkType appWorkType = AppWorkType.TASK;
        if (isWaitingForAMAppWork()) {
            appWorkType = AppWorkType.APP_MASTER;
        }

        if (updateType == null) {
            schedulerUnit.process(
                    new SchedulerUnitEvent(schedulerUnit.getAppWorkId(), SchedulerUnitEventType.ACQUIRED));
        } else {

        }
        return appWork;
    }

    // SchedulerUnit transfer to AppWork
    List<SchedulerUnit> pullAppWorksToTransfer() {
        writeLock.lock();
        try {
            return new ArrayList<>(liveAppWorks.values());
        } finally {
            writeLock.unlock();
        }
    }

    public List<RemoteAppWork> pullPreviousAppWorks() {
        writeLock.lock();
        try {
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    public List<RemoteAppWork> pullNewlyAllocatedAppWorks() {
        writeLock.lock();
        try {
            List<RemoteAppWork> returnAppWorkList =
                    new ArrayList<>(newlyAllocatedSchedulerUnits.size());
            Iterator<SchedulerUnit> iterator = newlyAllocatedSchedulerUnits.iterator();
            while (iterator.hasNext()) {
                SchedulerUnit schedulerUnit = iterator.next();
                RemoteAppWork updatedAppWork = updateAppWork(schedulerUnit, null);
                if (updatedAppWork != null) {
                    returnAppWorkList.add(updatedAppWork);
                    iterator.remove();
                }
            }
            return returnAppWorkList;
        } finally {
            writeLock.unlock();
        }
    }

    public void showRequest() {
        if (log.isDebugEnabled()) {
            readLock.lock();
            try {

            } finally {
                readLock.unlock();
            }
        }
    }

    public synchronized void addToNewlyDemotedAppWorks(AppWorkId appWorkId, SchedulerUnit schedulerUnit) {
        newlyDecreaseAppWorks.put(appWorkId, schedulerUnit);
    }

    public synchronized void addToNewlyDecreaseAppWorks(AppWorkId appWorkId, SchedulerUnit schedulerUnit) {
        newlyDecreaseAppWorks.put(appWorkId, schedulerUnit);
    }

    protected synchronized void addToNewlyAllocatedAppWorks(SchedulerNode schedulerNode, SchedulerUnit schedulerUnit) {
        newlyAllocatedSchedulerUnits.add(schedulerUnit);
    }

    public List<RemoteAppWork> pullNewlyPromotedAppWorks() {
        return null;
    }

    private List<RemoteAppWork> pullNewlyUpdateAppWorks(Map<AppWorkId, SchedulerUnit> newlyUpdateAppWorks, AppWorkUpdateType updateType) {
        return null;
    }

    public Priority getAppPriority() {
        return appPriority;
    }

    public void setAppPriority(Priority priority) {
        this.appPriority = priority;
    }

    public ResourceUsage getSchedulingResourceUsage() {
        return appResourceUsage;
    }

    public void setHeadRoom(Resource globalLimit) {
        this.resourceLimit = Resources.componentwiseMax(globalLimit, Resources.none());
    }

    public Resource getHeadRoom() {
        return resourceLimit;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean appWorkCompleted(SchedulerUnit schedulerUnit,
                                    AppWorkStatus appWorkStatus, SchedulerUnitEventType type) {
        writeLock.lock();
        try {
            AppWorkId appWorkId = schedulerUnit.getAppWorkId();
            if (liveAppWorks.remove(schedulerUnit) == null) {
                return false;
            }
            newlyAllocatedSchedulerUnits.remove(schedulerUnit);
            schedulerUnit.process(new SchedulerUnitFinishedEvent(appWorkId, appWorkStatus, type));
            Resource appWorkResource = schedulerUnit.getAppWork().getResource();
            appResourceUsage.decUsed(appWorkResource);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public void updateResourceRequests(List<ResourceRequest> resourceRequests) {
        writeLock.lock();
        try {
            if (!isStopped) {
                this.resourceRequests = resourceRequests;
            }
        } finally {
            writeLock.unlock();
        }
    }

    public ResourceRequest getPendingAsk() {
        readLock.lock();
        try {
            return resourceRequests.size() > 0 ? resourceRequests.get(0) : null;
        } finally {
            readLock.unlock();
        }
    }

    public void stop(AppState finalAppState) {
        pool.getPoolMetrics();
    }

    public SchedulerUnit allocate(FicaSchedulerNode node, RemoteAppWork appWork) {
        writeLock.lock();
        try {
            if (isStopped) {
                return null;
            }
            SchedulerUnit schedulerUnit = new SchedulerUnitImp(appWork, this.applicationId,
                    node.getNodeId(), user, context);
            schedulerUnit.setPoolName(pool.getPoolName());
            addToNewlyAllocatedAppWorks(node, schedulerUnit);
            AppWorkId appWorkId = appWork.getAppWorkId();
            liveAppWorks.put(appWorkId, schedulerUnit);
            appResourceUsage.incUsed(appWork.getResource());
            schedulerUnit.process(new SchedulerUnitEvent(appWorkId, SchedulerUnitEventType.START));
            if (log.isDebugEnabled()) {
                log.debug("allocate: applicationId = " + applicationId +
                        " AppWork=" + appWorkId + "host=" + appWork.getAgentId().getHostname());
            }
            return schedulerUnit;
        } finally {
            writeLock.unlock();
        }
    }

    private boolean isWaitingForAMAppWork() {
        return app.getMasterAppWork() == null;
    }
}
