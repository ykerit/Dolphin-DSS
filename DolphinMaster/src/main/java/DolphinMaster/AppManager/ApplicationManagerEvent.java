package DolphinMaster.AppManager;

import common.event.AbstractEvent;
import common.struct.ApplicationId;

public class ApplicationManagerEvent extends AbstractEvent<ApplicationManagerEventType> {
    private final ApplicationId applicationId;
    public ApplicationManagerEvent(ApplicationId applicationId, ApplicationManagerEventType type) {
        super(type);
        this.applicationId = applicationId;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }
}
