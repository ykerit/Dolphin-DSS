package DolphinMaster.schedulerunit;

import DolphinMaster.scheduler.AppDescribeId;
import common.struct.Priority;
import agent.appworkmanage.appwork.AppWork;
import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.AgentId;

import java.util.Set;

// Each scheduler unit maps an AppWork
public interface SchedulerUnit extends EventProcessor<SchedulerUnitEvent>, Comparable<SchedulerUnit> {
    String getAppWorkId();

    void setAppWorkId(String appWorkId);

    SchedulerUnitState getState();

    AppWork getAppWork();

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

    AppDescribeId getApplicationDescribeId();
}
