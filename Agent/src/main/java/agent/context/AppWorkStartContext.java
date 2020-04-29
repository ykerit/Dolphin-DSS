package agent.context;

import agent.appworkmanage.AppWork;

import java.util.List;

public final class AppWorkStartContext {
    private final AppWork appWork;
    private final String user;
    private final long appId;
    // AppWork running workspace
    private final String workspace;
    // AppWork need resource dir
    private final List<String> appLocalDirs;
    private final String appWorkScriptPath;
    // application file dir
    private final List<String> fileCacheDirs;

    public static final class Builder {
        private AppWork appWork;
        private String user;
        private long appId;
        private String workspace;
        private String appWorkScriptPath;
        private List<String> appLocalDirs;
        private List<String> fileCacheDirs;

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

        public Builder setAppId(long appId) {
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

        public Builder setAppWorkScriptPath(String appWorkScriptPath) {
            this.appWorkScriptPath = appWorkScriptPath;
            return this;
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
        this.appWorkScriptPath = builder.appWorkScriptPath;
        this.workspace = builder.workspace;
    }

    public AppWork getAppWork() {
        return appWork;
    }

    public String getUser() {
        return user;
    }

    public long getAppId() {
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

    public String getAppWorkScriptPath() {
        return appWorkScriptPath;
    }
}
