package agent;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.AtomicDouble;
import common.service.AbstractService;
import common.util.StreamReporter;

import java.util.concurrent.atomic.AtomicInteger;

public class AgentManageMetrics extends AbstractService {
    static final MetricRegistry metrics = new MetricRegistry();

    // Metrics listen
    Gauge<AtomicInteger> agentUsedMemGBListener;
    Gauge<AtomicDouble> agentCpuUtilizationListener;

    // Monitored
    private AtomicInteger agentUsedMemGB = new AtomicInteger(0);
    private AtomicDouble agentCpuUtilization = new AtomicDouble(0);

    private StreamReporter reporter;

    public AgentManageMetrics() {
        super(AgentManageMetrics.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        agentCpuUtilizationListener = new Gauge<AtomicDouble>() {
            @Override
            public AtomicDouble getValue() {
                return agentCpuUtilization;
            }
        };

        agentUsedMemGBListener = new Gauge<AtomicInteger>() {
            @Override
            public AtomicInteger getValue() {
                return agentUsedMemGB;
            }
        };

        metrics.register("Current used memory by this node in GB", agentUsedMemGBListener);
        metrics.register("Current CPU utilization", agentCpuUtilizationListener);

        reporter = StreamReporter.forRegistry(metrics).build();
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

    public void setAgentUsedMemGB(long totalUsedMemGB) {
        agentUsedMemGB.set((int)Math.floor(totalUsedMemGB/1024d));
    }

    public int getAgentUsedMemGB() {
        return agentUsedMemGB.get();
    }

    public void setAgentCpuUtilization(double cpuUtilization) {
        agentCpuUtilization.set(cpuUtilization);
    }

    public double getAgentCpuUtilization() {
        return agentCpuUtilization.get();
    }

    public StreamReporter getReporter() {
        return reporter;
    }
}
