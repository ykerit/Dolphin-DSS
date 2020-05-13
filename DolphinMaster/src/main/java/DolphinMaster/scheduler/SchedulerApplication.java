package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import DolphinMaster.node.NodeCleanAppWorkEvent;
import DolphinMaster.schedulerunit.SchedulerUnit;
import DolphinMaster.schedulerunit.SchedulerUnitEvent;
import DolphinMaster.schedulerunit.SchedulerUnitEventType;
import agent.appworkmanage.appwork.AppWork;
import common.context.ApplicationSubmission;
import common.resource.Resource;
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
    protected String appAMNodePartitionName = "";
    private final Resource resourceLimit = new Resource(0, 0);
    private final boolean amRunning = false;
    private final ApplicationId applicationId;
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
    private final ResourcePool pool;
    private final boolean isStopped = false;
    private Map<String, String> applicationSchedulingEnvs = new HashMap<>();
    private final AtomicLong unconfirmedAllocatedMem = new AtomicLong();
    private final AtomicInteger unconfirmedAllocateVCore = new AtomicInteger();

    public SchedulerApplication(ApplicationId applicationId, String user,
                                ResourcePool pool, DolphinContext context) {
        this.context = context;
        this.pool = pool;
        this.pendingRelease = Collections.
                newSetFromMap(new ConcurrentHashMap<AppWorkId, Boolean>());
        this.applicationId = applicationId;
        if (context.getApps() != null &&
                context.getApps().containsKey(applicationId)) {
            App app = context.getApps().get(applicationId);
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

    public ApplicationId getApplicationId() {
        return null;
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
}
