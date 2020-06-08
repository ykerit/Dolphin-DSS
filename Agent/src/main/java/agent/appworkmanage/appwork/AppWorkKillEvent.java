package agent.appworkmanage.appwork;

import common.struct.AppWorkId;

public class AppWorkKillEvent extends AppWorkEvent{
    private final int exitStatus;
    private final String description;
    public AppWorkKillEvent(AppWorkId appWorkId, int exit, String description) {
        super(appWorkId, AppWorkEventType.KILL_APP_WORK);
        this.exitStatus = exit;
        this.description = description;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getDescription() {
        return description;
    }
}
