package agent.appworkmanage.monitor;

import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.service.AbstractService;
import common.service.ServiceState;
import common.struct.AppWorkId;

public class AppWorkMonitorImp extends AbstractService implements AppWorkMonitor {

    private ResourceUtilization appWorkUtilization;

    public AppWorkMonitorImp() {
        super(AppWorkMonitorImp.class.getName());

        this.appWorkUtilization = ResourceUtilization.newInstance(0, 0);
    }

    @Override
    protected void serviceInit() throws Exception {
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
    }

    @Override
    public void process(AppWorkMonitorEvent event) {

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
        return appWorkUtilization;
    }

    @Override
    public void setAllocateResourceForAppWork(Resource resource) {

    }

    public static class ProcessTreeInfo {
        private AppWorkId appWorkId;
        private String pid;
        private long vmemLimit;
        private long pmemLimit;
        private int cpuVcores;

        public ProcessTreeInfo(AppWorkId appWorkId,
                               String pid,
                               long vmemLimit,
                               long pmemLimit,
                               int cpuVcores) {
            this.appWorkId = appWorkId;
            this.cpuVcores = cpuVcores;
            this.pid = pid;
            this.vmemLimit = vmemLimit;
            this.pmemLimit = pmemLimit;
        }

        public AppWorkId getAppWorkId() {
            return appWorkId;
        }

        public void setAppWorkId(AppWorkId appWorkId) {
            this.appWorkId = appWorkId;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public long getVmemLimit() {
            return vmemLimit;
        }

        public void setVmemLimit(long vmemLimit) {
            this.vmemLimit = vmemLimit;
        }

        public long getPmemLimit() {
            return pmemLimit;
        }

        public void setPmemLimit(long pmemLimit) {
            this.pmemLimit = pmemLimit;
        }

        public int getCpuVcores() {
            return cpuVcores;
        }

        public void setCpuVcores(int cpuVcores) {
            this.cpuVcores = cpuVcores;
        }
    }
}
