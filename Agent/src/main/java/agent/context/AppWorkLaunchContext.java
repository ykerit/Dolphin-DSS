package agent.context;

import java.util.List;
import java.util.Map;

public abstract class AppWorkLaunchContext {
    public abstract Map<String, String> getEnvironment();

    public abstract void setEnvironment();

    public abstract List<String> getCommands();

    public abstract void setCommands(List<String> commands);
}
