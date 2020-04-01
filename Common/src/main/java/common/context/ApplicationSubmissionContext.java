package common.context;

import common.container.ResourceLimit;

import java.util.HashMap;

public class ApplicationSubmissionContext {
    private String user;
    private ResourceLimit resourceLimit;
    private String command;
    private HashMap<String, String> env;

    public ApplicationSubmissionContext(String user, ResourceLimit resourceLimit, String command) {
        this.user = user;
        this.resourceLimit = resourceLimit;
        this.command = command;
        this.env = new HashMap<>();
    }

    public String getUser() {
        return user;
    }

    public ResourceLimit getResourceLimit() {
        return resourceLimit;
    }

    public String getCommand() {
        return command;
    }

    public HashMap<String, String> getEnv() {
        return env;
    }
}
