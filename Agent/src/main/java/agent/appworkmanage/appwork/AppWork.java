package agent.appworkmanage.appwork;

import common.struct.ApplicationId;
import common.context.AppWorkLaunchContext;
import agent.status.AppWorkStatus;
import common.event.EventProcessor;
import common.resource.Resource;
import common.struct.AgentId;

import java.nio.file.Path;

public interface AppWork extends EventProcessor<AppWorkEvent> {
    ApplicationId getAppId();

    AgentId getAgentId();

    String getAppWorkId();

    String getAppWorkStartTime();

    String getAppWorkLaunchedTime();

    Resource getResource();

    String getUser();

    AppWorkLaunchContext getAppWorkLaunchContext();

    AppWorkState getAppWorkState();

    Path getWorkDir();

    void setWorkDir();

    boolean isRunning();

    int getPriority();

    AppWorkExecType getExecType();

    void setIsReInitializing(boolean isReInitializing);

    boolean isReInitializing();

    void sendLaunchEvent();

    void sendKillEvent(int exitStatus, String description);

    boolean isRecovering();

    AppWorkStatus cloneAndGetAppWorkStatus();
}
