package agent.application;

import agent.appworkmanage.appwork.AppWork;
import common.struct.AppWorkId;
import common.struct.ApplicationId;

import java.util.Map;

public class ApplicationImp implements Application {
    @Override
    public String getUser() {
        return null;
    }

    @Override
    public ApplicationState getAppState() {
        return null;
    }

    @Override
    public ApplicationId getApplicationId() {
        return null;
    }

    @Override
    public Map<AppWorkId, AppWork> getAppWorks() {
        return null;
    }

    @Override
    public void process(ApplicationEvent event) {

    }
}
