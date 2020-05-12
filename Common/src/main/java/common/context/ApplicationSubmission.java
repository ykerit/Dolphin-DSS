package common.context;

import common.resource.Resource;
import common.struct.ApplicationId;
import common.struct.Priority;

import java.io.Serializable;
import java.util.Set;

public class ApplicationSubmission implements Serializable {

    private ApplicationId applicationId;
    private String applicationName;
    private Priority priority;
    private String pool;
    private String user;
    private String applicationType;
    private Resource resource;
    private AppWorkLaunchContext appMasterSpec;
    private Set<String> applicationTags;


    public ApplicationSubmission(ApplicationId applicationId,
                                 String applicationName,
                                 Priority priority,
                                 String pool,
                                 String user,
                                 String applicationType,
                                 Resource resource,
                                 Set<String> applicationTags,
                                 AppWorkLaunchContext appMasterSpec) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.priority = priority;
        this.pool = pool;
        this.user = user;
        this.applicationType = applicationType;
        this.resource = resource;
        this.appMasterSpec = appMasterSpec;
        this.applicationTags = applicationTags;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public AppWorkLaunchContext getAppMasterSpec() {
        return appMasterSpec;
    }

    public void setAppMasterSpec(AppWorkLaunchContext appMasterSpec) {
        this.appMasterSpec = appMasterSpec;
    }

    public Set<String> getApplicationTags() {
        return applicationTags;
    }

    public void setApplicationTags(Set<String> applicationTags) {
        this.applicationTags = applicationTags;
    }
}
