package DolphinMaster.schedulerunit;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import common.struct.AppWorkStatus;
import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SchedulerUnitImp implements SchedulerUnit {
    private static final Logger log = LogManager.getLogger(SchedulerUnitImp.class);

    private final Lock readLock;
    private final Lock writeLock;
    private final ApplicationId applicationId;
    private final AgentId agentId;
    private final DolphinContext context;
    private final EventProcessor processor;
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
    private final UntypedStateMachineBuilder schedulerUnitStateMachineBuilder;
    private final UntypedStateMachine schedulerUnitStateMachine;

    public SchedulerUnitImp(RemoteAppWork appWork, ApplicationId applicationId,
                            AgentId agentId, String user,
                            DolphinContext context) {
        this.agentId = agentId;
        this.appWork = appWork;
        this.applicationId = applicationId;
        this.user = user;
        this.context = context;
        this.processor = this.context.getDolphinDispatcher().getEventProcessor();
        this.isAMAppWork = false;
        this.createTime = System.currentTimeMillis();

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();

        schedulerUnitStateMachineBuilder = StateMachineBuilderFactory
                .create(SchedulerUnitImp.AppStateMachine.class);

        schedulerUnitStateMachineBuilder.externalTransition()
                .from(SchedulerUnitState.NEW)
                .to(SchedulerUnitState.ALLOCATED)
                .on(SchedulerUnitEventType.START)
                .callMethod("sendAppWorkAllocated");

        schedulerUnitStateMachineBuilder.externalTransition()
                .from(SchedulerUnitState.ALLOCATED)
                .to(SchedulerUnitState.ACQUIRED)
                .on(SchedulerUnitEventType.ACQUIRED);

        schedulerUnitStateMachineBuilder.externalTransition()
                .from(SchedulerUnitState.ACQUIRED)
                .to(SchedulerUnitState.RUNNING)
                .on(SchedulerUnitEventType.LAUNCHED);

        schedulerUnitStateMachineBuilder.externalTransition()
                .from(SchedulerUnitState.RUNNING)
                .to(SchedulerUnitState.COMPLETED)
                .on(SchedulerUnitEventType.FINISHED);

        schedulerUnitStateMachineBuilder.externalTransition()
                .from(SchedulerUnitState.RUNNING)
                .to(SchedulerUnitState.RELEASED)
                .on(SchedulerUnitEventType.RELEASED);

        schedulerUnitStateMachine = schedulerUnitStateMachineBuilder
                .newStateMachine(SchedulerUnitState.NEW);



        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        schedulerUnitStateMachine.accept(visitor);
        visitor.convertDotFile("/Users/yuankai/schedulerUnitStateMachine");
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
                return finishedStatus.getExitStatus();
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
    public RemoteAppWorkState getAppWorkState() {
        readLock.lock();
        try {
            if (finishedStatus != null) {
                return finishedStatus.getState();
            } else {
                return RemoteAppWorkState.RUNNING;
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void process(SchedulerUnitEvent event) {
        writeLock.lock();
        try {
            log.debug("Process event for {} of type {}", event.getAppWorkId(), event.getType());
            schedulerUnitStateMachine.fire(event.getType(), this);
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

    @StateMachineParameters(stateType = SchedulerUnitState.class, eventType = SchedulerUnitEventType.class, contextType = SchedulerUnitImp.class)
    static class AppStateMachine extends AbstractUntypedStateMachine {

        protected void sendAppWorkAllocated(SchedulerUnitState from, SchedulerUnitState to, SchedulerUnitEventType type, SchedulerUnitImp schedulerUnitImp) {
            schedulerUnitImp.processor.process(new AppEvent(schedulerUnitImp.applicationId, AppEventType.APP_WORK_ALLOCATE));
        }
    }
}
