package config;

import org.junit.Test;

public class ConfigurationTest {
    @Test
    public void testYaml() {
        SchedulerConfiguration configuration = new SchedulerConfiguration();
        configuration.initializeConfig();
        System.out.println(configuration.getConf());
    }
}
