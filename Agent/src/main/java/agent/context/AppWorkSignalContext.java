package agent.context;

import agent.appworkmanage.AppWork;
import agent.appworkmanage.AppWorkExecute.Signal;

public class AppWorkSignalContext {
    private final AppWork appWork;
    private final String user;
    private final int pid;
    private final Signal signal;

    public static final class Builder {
        private AppWork appWork;
        private String user;
        private int pid;
        private Signal signal;

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

        public Builder setSignal(Signal signal) {
            this.signal = signal;
            return this;
        }

        public AppWorkSignalContext build() {
            return new AppWorkSignalContext(this);
        }
    }

    private AppWorkSignalContext(Builder builder) {
        this.appWork = builder.appWork;
        this.pid = builder.pid;
        this.signal = builder.signal;
        this.user = builder.user;
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

    public Signal getSignal() {
        return signal;
    }
}
