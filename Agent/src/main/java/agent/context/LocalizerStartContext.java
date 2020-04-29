package agent.context;

public final class LocalizerStartContext {
    private final String user;
    private final long appId;
    private final String localDirs;

    public static final class Builder {
        private String user;
        private long appId;
        private String localDirs;

        public Builder() {}

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setAppId(long appId) {
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

    public long getAppId() {
        return appId;
    }

    public String getLocalDirs() {
        return localDirs;
    }
}
