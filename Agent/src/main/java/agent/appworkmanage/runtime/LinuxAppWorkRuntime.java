package agent.appworkmanage.runtime;

import agent.Context;

public interface LinuxAppWorkRuntime extends AppWorkRuntime {
    void initialize(Context context) throws AppWorkExecutionException;

    default void start() {}

    default void stop() {}
}
