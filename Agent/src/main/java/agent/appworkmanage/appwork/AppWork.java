package agent.appworkmanage.appwork;

import agent.context.AppWorkLaunchContext;
import common.event.EventProcessor;
import common.resource.Resource;

import java.nio.file.Path;

public interface AppWork extends EventProcessor<AppWorkEvent> {
    long getAppId();

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
}
