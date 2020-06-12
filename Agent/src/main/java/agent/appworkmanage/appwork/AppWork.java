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

    Resource getResource();

    Priority getPriority();

    long getAppWorkStartTime();

    long getAppWorkLaunchedTime();

    String getUser();

    AppWorkLaunchContext getAppWorkLaunchContext();

    AppWorkState getAppWorkState();

    AppWorkStatus cloneAndGetAppWorkStatus();

    AgentAppWorkStatus getAgentAppWorkStatus();

    String getWorkDir();

    void setWorkDir(String workDir);

    boolean isRunning();

    void setIsReInitializing(boolean isReInitializing);

    boolean isReInitializing();

    boolean isMarkedForKilling();

    void sendLaunchEvent();

    void sendKillEvent(int exitStatus, String description);

    boolean isAppWorkInFinalStates();

    Map<Path, List<String>> getLocalizeResource();
}
