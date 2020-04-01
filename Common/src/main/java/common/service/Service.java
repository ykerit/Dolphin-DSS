package common.service;

public interface Service {
    void init();
    void start();
    void stop();
    String getName();
    ServiceState getServiceState();
}
