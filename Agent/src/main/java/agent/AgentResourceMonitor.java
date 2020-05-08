package agent;

import common.resource.ResourceCollector;
import common.resource.ResourceUtilization;
import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentResourceMonitor extends AbstractService {

    private static final Logger log = LogManager.getLogger(AgentResourceMonitor.class.getName());

    private long monitorInterval;

    private ResourceCollector resourceCollector;
    private ResourceUtilization resourceUtilization;
    private final MonitorThread monitor;
    private final Context context;

    public AgentResourceMonitor(Context context) {
        super(AgentResourceMonitor.class.getName());
        monitor = new MonitorThread();
        this.context = context;
    }

    @Override
    protected void serviceInit() throws Exception {
        this.monitorInterval = context.getConfiguration().getAgentMonitorInterval();
        this.resourceCollector = ResourceCollector.newInstance();
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        if (enable()) {
            monitor.start();
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        if (enable()) {
            this.monitor.interrupt();
            try {
                this.monitor.join(10 * 1000);
            } catch (InterruptedException e) {
                log.warn("Could not wait for the thread to join");
            }
        }
        super.serviceStop();
    }

    boolean enable() {
        if (monitorInterval <= 0) {
            log.info("Agent resource monitor <= 0");
            return false;
        }
        if (resourceCollector == null) {
            log.info("resource collect failed");
            return false;
        }
        return true;
    }

    private class MonitorThread extends Thread {

        public MonitorThread() {
            super("Resource Monitor");
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                long mem = resourceCollector.getMemorySize() - resourceCollector.getAvailableMemorySize();
                float vCore = resourceCollector.getNumVCoresUsed();
                resourceUtilization = ResourceUtilization.newInstance((int) mem >> 20, vCore);

                AgentManageMetrics metrics = context.getMetrics();
                if (metrics != null) {
//                    metrics.setAgentCpuUtilization(resourceUtilization.getCpu());
//                    metrics.setAgentUsedMemGB(resourceUtilization.getMemory());
                }

                try {
                    Thread.sleep(monitorInterval);
                } catch (InterruptedException e) {
                    log.warn(AgentResourceMonitor.class.getName() + " is interrupted, Exiting");
                    break;
                }
            }
        }
    }

    public ResourceUtilization getUtilization() {
        return resourceUtilization;
    }
}
