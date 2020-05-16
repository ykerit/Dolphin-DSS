package common.context;

import common.event.EventDispatcher;
import common.service.ServiceState;
import config.Configuration;

public class ServiceContext {
    private EventDispatcher dispatcher;
    private ServiceState status;
    private Configuration configuration;

    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(EventDispatcher specialDispatcher) {
        this.dispatcher = specialDispatcher;
    }

    public ServiceState getStatus() {
        return status;
    }

    public void setStatus(ServiceState status) {
        this.status = status;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
