package DolphinMaster.schedulerunit;

import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.*;

import java.util.Set;

// Each scheduler unit maps an AppWork
public interface SchedulerUnit extends EventProcessor<SchedulerUnitEvent>, Comparable<SchedulerUnit> {
    AppWorkId getAppWorkId();

    void setAppWorkId(AppWorkId appWorkId);

    SchedulerUnitState getState();

    RemoteAppWork getAppWork();

    Resource getReservedResource();

    AgentId getReservedNode();

    Resource getAllocatedResource();

    Resource getLastConfirmedResource();

    AgentId getAllocateAgent();

    Priority getAllocatedPriority();

    long getCreatedTime();

    long getFinishTime();

    String getTips();

    int getAppWorkExitState();

    boolean isAMAppWork();

    String getQueueName();

    Resource getAllocateOrReservedResource();

    boolean completed();

    AgentId getAgentId();

    Set<String> getAllocationTags();

    ApplicationId getApplicationId();
}
