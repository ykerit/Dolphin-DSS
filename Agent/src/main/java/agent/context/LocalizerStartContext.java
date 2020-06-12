package agent.context;

public final class LocalizerStartContext {
    private final String user;
    private final String appId;
    private final String locId;
    private final String localDirs;

    public static final class Builder {
        private String user;
        private String appId;
        private String locId;
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

        public void setLocId(String locId) {
            this.locId = locId;
        }

        public LocalizerStartContext build() {
            return new LocalizerStartContext(this);
        }
    }

    private LocalizerStartContext(Builder builder) {
        this.appId = builder.appId;
        this.user = builder.user;
        this.localDirs = builder.localDirs;
        this.locId = builder.locId;
    }

    public String getUser() {
        return user;
    }

    public String getAppId() {
        return appId;
    }

    public String getLocId() {
        return locId;
    }

    public String getLocalDirs() {
        return localDirs;
    }
}
