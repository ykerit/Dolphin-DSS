package agent.appworkmanage.appwork;

import common.struct.AppWorkId;

public class AppWorkInitEvent extends AppWorkEvent{
    public AppWorkInitEvent(AppWorkId appWorkId) {
        super(appWorkId, AppWorkEventType.INIT_APP_WORK);
    }
}
