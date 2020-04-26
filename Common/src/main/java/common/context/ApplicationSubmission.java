package common.context;

import java.io.Serializable;

public class ApplicationSubmission implements Serializable {
    private long applicationID;
    private String applicationName;
    // Priority of job execution
    private int priority;
    // Queue group to which the job belongs
    private String group;
    // Submission of users
    private String user;
    // AppMaster spec
    private AppMasterSpec appMasterSpec;

    public ApplicationSubmission(long applicationID, String applicationName, int priority, String group, String user, AppMasterSpec appMasterSpec) {
        this.applicationID = applicationID;
        this.applicationName = applicationName;
        this.priority = priority;
        this.group = group;
        this.user = user;
        this.appMasterSpec = appMasterSpec;
    }

    public long getApplicationID() {
        return applicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getPriority() {
        return priority;
    }

    public String getGroup() {
        return group;
    }

    public String getUser() {
        return user;
    }

    public AppMasterSpec getAppMasterSpec() {
        return appMasterSpec;
    }
}
