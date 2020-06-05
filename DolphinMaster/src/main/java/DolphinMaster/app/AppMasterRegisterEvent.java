package DolphinMaster.app;

import common.struct.ApplicationId;

public class AppMasterRegisterEvent extends AppEvent {
    public AppMasterRegisterEvent(ApplicationId appId) {
        super(appId, AppEventType.AM_REGISTER);
    }
}
