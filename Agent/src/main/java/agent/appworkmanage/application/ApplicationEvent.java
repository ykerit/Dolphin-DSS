package agent.appworkmanage.application;

import common.event.AbstractEvent;
import common.struct.ApplicationId;

public class ApplicationEvent extends AbstractEvent<ApplicationEventType> {
    private final ApplicationId applicationId;

    public ApplicationEvent(ApplicationId applicationId, ApplicationEventType applicationEventType) {
        super(applicationEventType);
        this.applicationId = applicationId;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }
}
