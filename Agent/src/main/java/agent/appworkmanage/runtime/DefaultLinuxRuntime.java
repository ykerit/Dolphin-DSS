package agent.appworkmanage.runtime;

import agent.Context;
import common.struct.IOStreamPair;

public class DefaultLinuxRuntime implements LinuxAppWorkRuntime {
    @Override
    public void initialize(Context context) throws AppWorkExecutionException {

    }

    @Override
    public void prepareAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public void launchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public void relaunchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public void signalAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public void reapAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public IOStreamPair execAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {
        return null;
    }
}
