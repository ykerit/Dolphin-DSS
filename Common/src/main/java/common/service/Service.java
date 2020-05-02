package common.service;

public interface Service {
    void init() throws Exception;
    void start();
    void stop();
    String getName();
    ServiceState getServiceState();
}
