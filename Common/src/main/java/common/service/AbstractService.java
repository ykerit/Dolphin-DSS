package common.service;

import config.Configuration;

/**
 * This is basic service for single service
 */
public abstract class AbstractService implements Service {
    private ServiceState state;
    private final String name;

    private Object lock = new Object();
    private ServiceState[] stateTable =
            new ServiceState[]{ServiceState.READY, ServiceState.INITED,
                    ServiceState.STARTED, ServiceState.STOPPED};

    public AbstractService(String name) {
        this.name = name;
        this.state = ServiceState.READY;
    }

    @Override
    public ServiceState getServiceState() {
        return this.state;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init() throws Exception {
        if (this.state == ServiceState.INITED)
            return;
        synchronized (lock) {
            if (isNextState(ServiceState.INITED)) {
                this.state = ServiceState.INITED;
                serviceInit();
            }
        }
    }

    @Override
    public void start() {
        if (this.state == ServiceState.STARTED)
            return;
        synchronized (lock) {
            if (isNextState(ServiceState.STARTED)) {
                this.state = ServiceState.STARTED;
                serviceStart();
            }
        }
    }

    @Override
    public void stop() {
        if (this.state == ServiceState.STOPPED)
            return;
        synchronized (lock) {
            if (isNextState(ServiceState.STOPPED)) {
                this.state = ServiceState.STOPPED;
                serviceStop();
            }
        }
    }

    protected void serviceInit() throws Exception {

    }
    protected void serviceStart() {

    }
    protected void serviceStop() {

    }

    private boolean isNextState(ServiceState state) {
        return stateTable[(this.state.getValue()+1)%this.stateTable.length] == state;
    }
}
