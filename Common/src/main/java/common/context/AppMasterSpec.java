package common.context;

import common.resource.Resource;

import java.io.Serializable;
import java.util.Map;

public class AppMasterSpec implements Serializable {
    private Resource resource;
    private String command;
    private Map<String, String> environment;

    public AppMasterSpec(Resource resource, String command, Map<String, String> environment) {
        this.resource = resource;
        this.command = command;
        this.environment = environment;
    }

    public Resource getResource() {
        return resource;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

}
