package agent.appworkmanage.appwork;

import common.struct.*;
import common.context.AppWorkLaunchContext;
import common.event.EventProcessor;
import common.resource.Resource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AppWork extends EventProcessor<AppWorkEvent> {
    AppWorkId getAppWorkId();

    void setAppWorkId(AppWorkId appWorkId);

    Resource getResource();

    void setResource(Resource resource);

    Priority getPriority();

    void setPriority();

    Set<String> getAllocationTags();

    void setAllocationTags(Set<String> allocationTags);

    String getAppWorkStartTime();

    String getAppWorkLaunchedTime();

    String getUser();

    AppWorkLaunchContext getAppWorkLaunchContext();

    AppWorkState getAppWorkState();

    AgentAppWorkStatus getAgentAppWorkStatus();

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

    Map<Path, List<String>> getLocalizeResource();
}
