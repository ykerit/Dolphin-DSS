package agent.status;

import agent.appworkmanage.appwork.AppWorkState;
import agent.appworkmanage.appwork.ExitCode;
import common.struct.AppWorkId;

public class AppWorkStatus {
    private final AppWorkId appWorkId;
    private final AppWorkState state;
    private final ExitCode exitStatus;

    public AppWorkStatus(AppWorkId appWorkId, AppWorkState state, ExitCode exitStatus) {
        this.appWorkId = appWorkId;
        this.state = state;
        this.exitStatus = exitStatus;
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public AppWorkState getState() {
        return state;
    }

    public ExitCode getExitStatus() {
        return exitStatus;
    }
}
