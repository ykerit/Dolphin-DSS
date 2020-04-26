package agent.context;

public final class LocalizerStartContext {
    private final String user;
    private final String appId;
    private final String localDirs;

    public static final class Builder {
        private String user;
        private String appId;
        private String localDirs;

        public Builder() {}

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setLocalDirs(String localDirs) {
            this.localDirs = localDirs;
            return this;
        }

        public LocalizerStartContext build() {
            return new LocalizerStartContext(this);
        }
    }

    private LocalizerStartContext(Builder builder) {
        this.appId = builder.appId;
        this.user = builder.user;
        this.localDirs = builder.localDirs;
    }

    public String getUser() {
        return user;
    }

    public String getAppId() {
        return appId;
    }

    public String getLocalDirs() {
        return localDirs;
    }
}
