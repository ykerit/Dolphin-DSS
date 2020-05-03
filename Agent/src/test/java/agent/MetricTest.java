package agent;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MetricTest {

    static final MetricRegistry metrics = new MetricRegistry();

    void report() {
        final CsvReporter reporter = CsvReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File("~/workspace"));
        reporter.start(1, TimeUnit.SECONDS);
        reporter.report();
    }

    @Test
    public void testMetric() {
        report();
        metrics.register(MetricRegistry.name("cpu usage"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 100;
            }
        });


        while (true) {

        }
    }
}
