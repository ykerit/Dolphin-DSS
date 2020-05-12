package agent.application;

public interface Application {
    String getUser();

    ApplicationState getAppState();

    long getAppId();

    String getFlowName();

    long getFlowRunId();
}
