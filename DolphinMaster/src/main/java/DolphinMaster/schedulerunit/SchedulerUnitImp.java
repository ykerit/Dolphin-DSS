package DolphinMaster.schedulerunit;

import DolphinMaster.DolphinContext;
import agent.appworkmanage.appwork.AppWorkState;
import agent.status.AppWorkStatus;
import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SchedulerUnitImp implements SchedulerUnit {
    private static final Logger log = LogManager.getLogger(SchedulerUnitImp.class);

    private final Lock readLock;
    private final Lock writeLock;
    private final ApplicationId applicationId;
    private final AgentId agentId;
    private final DolphinContext dolphinContext;
    private final EventProcessor eventProcessor;
    private final String user;

    private volatile RemoteAppWork appWork;
    private Resource reservedResource;
    private AgentId reservedNode;
    private long createTime;
    private long finishTime;
    private AppWorkStatus finishedStatus;
    private boolean isAMAppWork;

    private Resource lastConfirmeResource;
    private volatile String poolName;
    private volatile Set<String> allocateTags = null;

    public SchedulerUnitImp(RemoteAppWork appWork, ApplicationId applicationId,
                            AgentId agentId, String user,
                            DolphinContext context) {
        this.agentId = agentId;
        this.appWork = appWork;
        this.applicationId = applicationId;
        this.user = user;
        this.dolphinContext = context;
        this.eventProcessor = this.dolphinContext.getDolphinDispatcher().getEventProcessor();
        this.isAMAppWork = false;
        this.createTime = System.currentTimeMillis();

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    @Override
    public AppWorkId getAppWorkId() {
        return this.appWork.getAppWorkId();
    }

    @Override
    public void setAppWorkId(AppWorkId appWorkId) {
        appWork.setAppWorkId(appWorkId);
    }

    @Override
    public SchedulerUnitState getState() {
        readLock.lock();
        try {
            return null;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public RemoteAppWork getAppWork() {
        return appWork;
    }

    @Override
    public void setAppWork(RemoteAppWork appWork) {
        this.appWork = appWork;
    }

    @Override
    public Resource getReservedResource() {
        return reservedResource;
    }

    @Override
    public AgentId getReservedNode() {
        return reservedNode;
    }

    @Override
    public Resource getAllocatedResource() {
        readLock.lock();
        try {
            return this.appWork.getResource();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Resource getLastConfirmedResource() {
        readLock.lock();
        try {
            return this.lastConfirmeResource;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AgentId getAllocateAgent() {
        return appWork.getAgentId();
    }

    @Override
    public Priority getAllocatedPriority() {
        return appWork.getPriority();
    }

    @Override
    public long getCreatedTime() {
        return createTime;
    }

    @Override
    public long getFinishTime() {
        readLock.lock();
        try {
            return finishTime;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getTips() {
        readLock.lock();
        try {
            if (finishedStatus != null) {
                return finishedStatus.getTips();
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int getAppWorkExitState() {
        readLock.lock();
        try {
            if (finishedStatus != null) {
                return finishedStatus.getExitStatus().getCode();
            } else {
                return 0;
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isAMAppWork() {
        readLock.lock();
        try {
            return isAMAppWork;
        } finally {
            readLock.unlock();
        }
    }

    public void setAMAppWork(boolean isAMAppWork) {
        writeLock.lock();
        try {
            this.isAMAppWork = isAMAppWork;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String getPoolName() {
        return poolName;
    }

    @Override
    public void setPoolName(String pool) {
        this.poolName = pool;
    }

    @Override
    public Resource getAllocateOrReservedResource() {
        readLock.lock();
        try {
            if (getState().equals(SchedulerUnitState.RESERVED)) {
                return getReservedResource();
            } else {
                return getAllocatedResource();
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean completed() {
        return finishedStatus != null;
    }

    @Override
    public AgentId getAgentId() {
        return agentId;
    }

    @Override
    public Set<String> getAllocationTags() {
        return allocateTags;
    }

    @Override
    public ApplicationId getApplicationId() {
        return this.applicationId;
    }

    @Override
    public AppWorkState getAppWorkState() {
        readLock.lock();
        try {
            if (finishedStatus != null) {
                return finishedStatus.getState();
            } else {
                return AppWorkState.RUNNING;
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void process(SchedulerUnitEvent event) {
        log.debug("Processing {} of type {}", event.getAppWorkId(), event.getType());
        writeLock.lock();
        try {
            switch (event.getType()) {

            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int compareTo(SchedulerUnit o) {
        if (getAppWorkId() != null && o.getAppWorkId() != null) {
            return getAppWorkId().compareTo(o.getAppWorkId());
        }
        return -1;
    }
}
