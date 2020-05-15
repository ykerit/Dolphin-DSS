package agent.status;

import agent.appworkmanage.appwork.AppWorkState;
import agent.appworkmanage.appwork.ExitCode;
import common.struct.AppWorkId;
import common.struct.RemoteAppWorkState;

public class AppWorkStatus {
    private final AppWorkId appWorkId;
    private final RemoteAppWorkState state;
    private final int exitStatus;
    private final String tips;

    public AppWorkStatus(AppWorkId appWorkId, RemoteAppWorkState state, int exitStatus) {
        this(appWorkId, state, exitStatus, null);
    }

    public AppWorkStatus(AppWorkId appWorkId, RemoteAppWorkState state, int exitStatus, String tips) {
        this.appWorkId = appWorkId;
        this.state = state;
        this.exitStatus = exitStatus;
        this.tips = tips;
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public RemoteAppWorkState getState() {
        return state;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getTips() {
        return tips;
    }
}
