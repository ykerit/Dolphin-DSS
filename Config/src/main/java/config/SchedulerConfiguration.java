package config;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class SchedulerConfiguration {
    private Yaml yaml;
    private Map<String, Object> conf;

    public SchedulerConfiguration() {
        yaml = new Yaml();
    }

    void initializeConfig() {
        conf = yaml.load(this.getClass().getClassLoader().getResourceAsStream("scheduler.yaml"));
    }

    public Map<String, Object> getConf() {
        return conf;
    }
}
