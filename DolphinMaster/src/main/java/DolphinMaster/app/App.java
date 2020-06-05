package DolphinMaster.app;

import DolphinMaster.scheduler.SchedulerApplication;
import common.context.ApplicationSubmission;
import common.event.EventProcessor;
import common.struct.AgentId;
import common.struct.ApplicationId;
import common.struct.Priority;
import common.struct.RemoteAppWork;

import java.util.Map;
import java.util.Set;

public interface App extends EventProcessor<AppEvent> {

    ApplicationId getApplicationId();

    ApplicationSubmission getApplicationSubmission();

    AppState getAppState();

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

    Map<String, String> getApplicationEnvs();

    void setMasterAppWork(RemoteAppWork appWork);

    RemoteAppWork getMasterAppWork();
}
