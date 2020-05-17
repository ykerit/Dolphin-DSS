package agent;

import agent.appworkmanage.AppWorkManagerEvent;
import agent.appworkmanage.AppWorkManagerEventType;
import common.struct.RemoteAppWork;

import java.util.List;

public class UpdateAppWorksEvent extends AppWorkManagerEvent {
    private final List<RemoteAppWork> appWorksToUpdate;

    public UpdateAppWorksEvent(List<RemoteAppWork> appWorksToUpdate) {
        super(AppWorkManagerEventType.UPDATE_APP_WORK);
        this.appWorksToUpdate = appWorksToUpdate;
    }

    public List<RemoteAppWork> getAppWorksToUpdate() {
        return appWorksToUpdate;
    }
}
