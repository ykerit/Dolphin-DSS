package common.service;

public interface Service {
    void init() throws Exception;
    void start() throws Exception;
    void stop() throws Exception;
    String getName();
    ServiceState getServiceState();
}
