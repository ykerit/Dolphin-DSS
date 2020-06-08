package agent.appworkmanage.application;

import agent.appworkmanage.appwork.AppWork;

public class ApplicationAppWorkInitEvent extends ApplicationEvent {
    private final AppWork appWork;

    public ApplicationAppWorkInitEvent(AppWork appWork) {
        super(appWork.getAppWorkId().getApplicationId(), ApplicationEventType.INIT_APP_WORK);
        this.appWork = appWork;
    }

    public AppWork getAppWork() {
        return appWork;
    }
}
