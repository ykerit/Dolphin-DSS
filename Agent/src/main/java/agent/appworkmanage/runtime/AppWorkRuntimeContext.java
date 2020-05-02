package agent.appworkmanage.runtime;

import agent.appworkmanage.appwork.AppWork;

public class AppWorkRuntimeContext {
    private final AppWork appWork;

    public static final class Builder {
        private final AppWork appWork;

        public Builder(AppWork appWork) {
            this.appWork = appWork;
        }

        public AppWorkRuntimeContext build() {
            return new AppWorkRuntimeContext(this);
        }
    }

    private AppWorkRuntimeContext(Builder builder) {
        this.appWork = builder.appWork;
    }

    public AppWork getAppWork() {
        return appWork;
    }
}
