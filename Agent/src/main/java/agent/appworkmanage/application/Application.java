package agent.appworkmanage.application;

import agent.appworkmanage.appwork.AppWork;
import common.event.EventProcessor;
import common.struct.AppWorkId;
import common.struct.ApplicationId;

import java.util.Map;

public interface Application extends EventProcessor<ApplicationEvent> {
    String getUser();

    ApplicationState getAppState();

    ApplicationId getApplicationId();

    Map<AppWorkId, AppWork> getAppWorks();
}
