package agent;

import common.resource.ResourceCollector;
import org.junit.Test;

public class ResourceCollectorTest {
    @Test
    public void testCollector() {
        ResourceCollector collector = new ResourceCollector();
        System.out.printf("AvailableMemorySize: " + collector.getAvailableMemorySize());
        System.out.printf("CpuFrequency: " + collector.getCpuFrequency());
        System.out.printf("MemorySize: " + collector.getMemorySize());
        System.out.printf("NumCores: " + collector.getNumCores());
        System.out.printf("NumProcessors: " + collector.getNumProcessors());
        System.out.printf("NumVCoresUsed: " + collector.getNumVCoresUsed());
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("CpuUsagePercentage: " + collector.getCpuUsagePercentage());
    }
}
