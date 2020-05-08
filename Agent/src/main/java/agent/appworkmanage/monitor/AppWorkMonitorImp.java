package agent.appworkmanage.monitor;

import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.service.ServiceState;

public class AppWorkMonitorImp implements AppWorkMonitor {
    @Override
    public void process(AppWorkMonitorEvent event) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return AppWorkMonitor.class.getName();
    }

    @Override
    public ServiceState getServiceState() {
        return null;
    }

    @Override
    public ResourceUtilization getAppWorkUtilization() {
        return null;
    }

    @Override
    public void setAllocateResourceForAppWork(Resource resource) {

    }
}
