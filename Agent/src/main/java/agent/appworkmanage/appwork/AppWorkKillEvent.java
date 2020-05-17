package agent.appworkmanage.appwork;

import common.struct.AppWorkId;

public class AppWorkKillEvent extends AppWorkEvent{
    public AppWorkKillEvent(AppWorkId appWorkId) {
        super(appWorkId, AppWorkEventType.KILL_APP_WORK);
    }
}
