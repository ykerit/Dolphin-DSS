package agent.context;

import agent.appworkmanage.AppWork;

public class AppWorkAlivenessContext {
    private final AppWork appWork;
    private final String user;
    private final int pid;

    public static final class Builder {
        private AppWork appWork;
        private String user;
        private int pid;

        public Builder() {}

        public Builder setAppWork(AppWork appWork) {
            this.appWork = appWork;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setPid(int pid) {
            this.pid = pid;
            return this;
        }

        public AppWorkAlivenessContext build() {
            return new AppWorkAlivenessContext(this);
        }

    }

    private AppWorkAlivenessContext(Builder builder) {
        this.appWork = builder.appWork;
        this.user = builder.user;
        this.pid = builder.pid;
    }

    public AppWork getAppWork() {
        return appWork;
    }

    public String getUser() {
        return user;
    }

    public int getPid() {
        return pid;
    }
}
