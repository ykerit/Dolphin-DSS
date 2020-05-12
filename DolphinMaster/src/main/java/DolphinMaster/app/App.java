package DolphinMaster.app;

import DolphinMaster.scheduler.AppDescribe;
import DolphinMaster.scheduler.AppDescribeId;
import common.context.ApplicationSubmission;
import common.event.EventProcessor;
import common.struct.AgentId;
import common.struct.ApplicationId;
import common.struct.Priority;

import java.util.Set;

public interface App extends EventProcessor<AppEvent> {

    ApplicationId getApplicationId();

    ApplicationSubmission getApplicationSubmission();

    AppState getState();

    String getUser();

    float getProgress();

    String getPool();

    void setPool(String name);

    String getName();

    ApplicationReport createAndGetApplicationReport(String user);

    long getFinishTime();

    long getStartTime();

    long getSubmitTime();

    long getLaunchTime();

    StringBuilder getTips();

    String getApplicationType();

    Set<String> getApplicationTags();

    Set<AgentId> getRunNodes();

    Priority getApplicationPriority();

    boolean isAppInCompletedStates();
}
