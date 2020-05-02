package agent.appworkmanage.runtime;

import common.struct.IOStreamPair;

public interface AppWorkRuntime {

    void prepareAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

    void launchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

    void relaunchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

    void signalAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

    void reapAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

    IOStreamPair execAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException;

}
