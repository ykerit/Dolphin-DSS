package agent.context;

import agent.appworkmanage.appwork.AppWork;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class AppWorkStartContext {
    private final Map<Path, List<String>> localizedResources;
    private final AppWork appWork;
    private final String user;
    private final String appId;
    // AppWork running workspace
    private final String workspace;
    private final List<String> localDirs;
    // AppWork need resource dir
    private final List<String> appLocalDirs;
    // application file dir
    private final List<String> fileCacheDirs;
    private final List<String> userLocalDirs;
    private final List<String> appWorkLocalDirs;
    private final List<String> userFileCacheDirs;
    private final List<String> applicationLocalDirs;

    public static final class Builder {
        private Map<Path, List<String>> localizedResources;
        private AppWork appWork;
        private String user;
        private String appId;
        private List<String> localDirs;
        private String workspace;
        private List<String> appLocalDirs;
        private List<String> fileCacheDirs;
        private List<String> userLocalDirs;
        private List<String> appWorkLocalDirs;
        private List<String> userFileCacheDirs;
        private List<String> applicationLocalDirs;

        public Builder() {
        }

        public Builder setAppWork(AppWork appWork) {
            this.appWork = appWork;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setWorkspace(String workspace) {
            this.workspace = workspace;
            return this;
        }

        public Builder setAppLocalDirs(List<String> appLocalDirs) {
            this.appLocalDirs = appLocalDirs;
            return this;
        }

        public Builder setFileCacheDirs(List<String> fileCacheDirs) {
            this.fileCacheDirs = fileCacheDirs;
            return this;
        }

        public void setLocalizedResources(Map<Path, List<String>> localizedResources) {
            this.localizedResources = localizedResources;
        }

        public void setLocalDirs(List<String> localDirs) {
            this.localDirs = localDirs;
        }

        public void setApplicationLocalDirs(List<String> applicationLocalDirs) {
            this.applicationLocalDirs = applicationLocalDirs;
        }

        public void setAppWorkLocalDirs(List<String> appWorkLocalDirs) {
            this.appWorkLocalDirs = appWorkLocalDirs;
        }

        public void setUserFileCacheDirs(List<String> userFileCacheDirs) {
            this.userFileCacheDirs = userFileCacheDirs;
        }

        public void setUserLocalDirs(List<String> userLocalDirs) {
            this.userLocalDirs = userLocalDirs;
        }

        public AppWorkStartContext build() {
            return new AppWorkStartContext(this);
        }
    }

    private AppWorkStartContext(Builder builder) {
        this.appId = builder.appId;
        this.appLocalDirs = builder.appLocalDirs;
        this.appWork = builder.appWork;
        this.user = builder.user;
        this.fileCacheDirs = builder.fileCacheDirs;
        this.workspace = builder.workspace;
        this.localizedResources = builder.localizedResources;
        this.localDirs = builder.localDirs;
        this.applicationLocalDirs = builder.applicationLocalDirs;
        this.appWorkLocalDirs = builder.appWorkLocalDirs;
        this.userFileCacheDirs = builder.userFileCacheDirs;
        this.userLocalDirs = builder.userLocalDirs;
    }

    public AppWork getAppWork() {
        return appWork;
    }

    public String getUser() {
        return user;
    }

    public String getAppId() {
        return appId;
    }

    public String getWorkspace() {
        return workspace;
    }

    public List<String> getAppLocalDirs() {
        return appLocalDirs;
    }

    public List<String> getFileCacheDirs() {
        return fileCacheDirs;
    }

    public Map<Path, List<String>> getLocalizedResources() {
        return localizedResources;
    }

    public List<String> getLocalDirs() {
        return localDirs;
    }

    public List<String> getApplicationLocalDirs() {
        return applicationLocalDirs;
    }

    public List<String> getAppWorkLocalDirs() {
        return appWorkLocalDirs;
    }

    public List<String> getUserFileCacheDirs() {
        return userFileCacheDirs;
    }

    public List<String> getUserLocalDirs() {
        return userLocalDirs;
    }
}
