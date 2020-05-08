package agent;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class MetricTest {

    static final MetricRegistry metrics = new MetricRegistry();

    void report() {
        final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
    }

    @Test
    public void testMetric() {
        report();
        Gauge<Integer> integerGauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 100;
            }
        };
        metrics.register(MetricRegistry.name("cpu usage"), integerGauge);

        while (true) ;
    }

    @Test
    public void testAutoRegister() {
        AgentManageMetrics metrics = new AgentManageMetrics();
        metrics.autoRegister();
    }

    @Test
    public void testAgentManageMetrics() throws Exception {
        AgentManageMetrics metrics = new AgentManageMetrics();
        metrics.report();
        Thread.sleep(3000L);
        metrics.setAgentUsedMemGB(10000000000L);
        metrics.setAgentCpuUtilization(20);
        while (true) ;
    }
}
