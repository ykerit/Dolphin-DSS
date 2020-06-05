package DolphinMaster.app.event;

import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import common.struct.ApplicationId;

public class StartAppEvent extends AppEvent {
    public StartAppEvent(ApplicationId appId) {
        super(appId, AppEventType.START);
    }
}
