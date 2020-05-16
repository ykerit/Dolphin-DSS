package agent.appworkmanage.appwork;

import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.context.AppWorkLaunchContext;
import common.struct.AppWorkStatus;
import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.AgentId;
import common.struct.Priority;

import java.nio.file.Path;
import java.util.Set;

public interface AppWork extends EventProcessor<AppWorkEvent> {
    AppWorkId getAppWorkId();

    void setAppWorkId(AppWorkId appWorkId);

    AgentId getAgentId();

    void setAgentId(AgentId agentId);

    Resource getResource();

    void setResource(Resource resource);

    Priority getPriority();

    void setPriority();

    Set<String> getAllocationTags();

    void setAllocationTags(Set<String> allocationTags);


    ApplicationId getAppId();

    String getAppWorkStartTime();

    String getAppWorkLaunchedTime();

    String getUser();

    AppWorkLaunchContext getAppWorkLaunchContext();

    AppWorkState getAppWorkState();

    Path getWorkDir();

    void setWorkDir();

    boolean isRunning();

    AppWorkExecType getExecType();

    void setIsReInitializing(boolean isReInitializing);

    boolean isReInitializing();

    void sendLaunchEvent();

    void sendKillEvent(int exitStatus, String description);

    boolean isRecovering();

    AppWorkStatus cloneAndGetAppWorkStatus();
}
