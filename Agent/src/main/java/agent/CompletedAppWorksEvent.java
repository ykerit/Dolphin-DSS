package agent;

import agent.appworkmanage.AppWorkManagerEvent;
import agent.appworkmanage.AppWorkManagerEventType;
import common.struct.AppWorkId;

import java.util.List;

public class CompletedAppWorksEvent extends AppWorkManagerEvent {
    private final List<AppWorkId> appWorkIds;

    public CompletedAppWorksEvent(List<AppWorkId> appWorkIds) {
        super(AppWorkManagerEventType.FINISH_APP_WORK);
        this.appWorkIds = appWorkIds;
    }

    public List<AppWorkId> getAppWorkIds() {
        return appWorkIds;
    }
}
