package agent.context;

import agent.appworkmanage.appwork.AppWork;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AppWorkPrepareContext {
    private final AppWork appWork;
    private final Map<Path, List<String>> localizeResource;
    private final String user;
    private final List<String> appWorkLocalDirs;
    private final List<String> commands;

    public static final class Builder {
        private AppWork appWork;
        private Map<Path, List<String>> localizeResource;
        private String user;
        private List<String> appWorkLocalDirs;
        private List<String> commands;

        public Builder setAppWork(AppWork appWork) {
            this.appWork = appWork;
            return this;
        }

        public Builder setLocalizeResource(Map<Path, List<String>> localizeResource) {
            this.localizeResource = localizeResource;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setAppWorkLocalDirs(List<String> appWorkLocalDirs) {
            this.appWorkLocalDirs = appWorkLocalDirs;
            return this;
        }

        public Builder setCommands(List<String> commands) {
            this.commands = commands;
            return this;
        }

        public AppWorkPrepareContext build() {
            return new AppWorkPrepareContext(this);
        }
    }

    private AppWorkPrepareContext(Builder builder) {
        this.appWork = builder.appWork;
        this.appWorkLocalDirs = builder.appWorkLocalDirs;
        this.commands = builder.commands;
        this.user = builder.user;
        this.localizeResource = builder.localizeResource;
    }

    public AppWork getAppWork() {
        return appWork;
    }

    public Map<Path, List<String>> getLocalizeResource() {
        return localizeResource;
    }

    public String getUser() {
        return user;
    }

    public List<String> getAppWorkLocalDirs() {
        return appWorkLocalDirs;
    }

    public List<String> getCommands() {
        return commands;
    }
}
