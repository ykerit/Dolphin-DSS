package agent.status;

import agent.appworkmanage.appwork.AppWorkState;
import agent.appworkmanage.appwork.ExitCode;

public class AppWorkStatus {
    private final String appWorkId;
    private final AppWorkState state;
    private final ExitCode exitStatus;

    public AppWorkStatus(String appWorkId, AppWorkState state, ExitCode exitStatus) {
        this.appWorkId = appWorkId;
        this.state = state;
        this.exitStatus = exitStatus;
    }

    public String getAppWorkId() {
        return appWorkId;
    }

    public AppWorkState getState() {
        return state;
    }

    public ExitCode getExitStatus() {
        return exitStatus;
    }
}
