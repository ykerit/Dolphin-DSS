package common.context;

import common.resource.LocalResource;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class AppWorkLaunchContext implements Serializable {
    private Map<String, LocalResource> localResource;
    private Map<String, String> environment;
    private List<String> commands;
    private Map<String, ByteBuffer> serviceDate;

    public AppWorkLaunchContext(Map<String, LocalResource> localResource,
                                Map<String, String> environment,
                                List<String> commands,
                                Map<String, ByteBuffer> serviceDate) {
        this.localResource = localResource;
        this.environment = environment;
        this.commands = commands;
        this.serviceDate = serviceDate;
    }

    public Map<String, LocalResource> getLocalResource() {
        return localResource;
    }

    public void setLocalResource(Map<String, LocalResource> localResource) {
        this.localResource = localResource;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public Map<String, ByteBuffer> getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Map<String, ByteBuffer> serviceDate) {
        this.serviceDate = serviceDate;
    }
}
