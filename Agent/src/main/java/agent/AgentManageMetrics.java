package agent;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import common.annotation.Metrics;
import common.util.GaugeHelp.GaugeDouble;
import common.util.GaugeHelp.GaugeInt;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class AgentManageMetrics {
    static final MetricRegistry metrics = new MetricRegistry();

    // Monitored
    @Metrics(name = "Current used memory by this agent in GB")
    private GaugeInt agentUsedMemGB;
    @Metrics(name = "Current CPU utilization")
    private GaugeDouble agentCpuUtilization;
    @Metrics(name = "Current Memory available in GB")
    private GaugeInt availableGB;
    @Metrics(name = "Current CPU available virtual Cores")
    private GaugeInt availableVCores;

    private long availableMB;

    public void autoRegister() {
        Class clazz = null;
        try {
            clazz = Class.forName("agent.AgentManageMetrics");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Metrics metricAnnotation = field.getDeclaredAnnotation(Metrics.class);
            if (metricAnnotation != null) {
                Class<?> cls = field.getType();
                try {
                    Metric metric = (Metric) cls.newInstance();
                    field.set(this, metric);
                    metrics.register(metricAnnotation.name(), metric);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AgentManageMetrics() {
        autoRegister();
    }

    public void setAgentUsedMemGB(long totalUsedMemGB) {
        agentUsedMemGB.set((int) Math.floor(totalUsedMemGB / 1024d));

    }

    public int getAgentUsedMemGB() {
        return agentUsedMemGB.getValue().get();
    }

    public void setAgentCpuUtilization(double cpuUtilization) {
        agentCpuUtilization.set(cpuUtilization);
    }

    public double getAgentCpuUtilization() {
        return agentCpuUtilization.getValue().get();
    }

    void report() {
        final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
    }
}
