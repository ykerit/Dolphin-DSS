package agent.application;

import common.struct.ApplicationId;

public class ApplicationFinishedEvent extends ApplicationEvent {
    private final String tips;

    public ApplicationFinishedEvent(ApplicationId applicationId, String tips) {
        super(applicationId, ApplicationEventType.FINISH_APPLICATION);
        this.tips = tips;
    }

    public String getTips() {
        return tips;
    }
}
