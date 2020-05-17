package agent;

import agent.appworkmanage.AppWorkManagerEvent;
import agent.appworkmanage.AppWorkManagerEventType;
import common.struct.ApplicationId;

import java.util.List;

public class CompletedAppsEvent extends AppWorkManagerEvent {
    private final List<ApplicationId> appsToCleanup;

    public CompletedAppsEvent(List<ApplicationId> appsToCleanup) {
        super(AppWorkManagerEventType.FINISH_APP);
        this.appsToCleanup = appsToCleanup;
    }

    public List<ApplicationId> getAppsToCleanup() {
        return appsToCleanup;
    }
}
