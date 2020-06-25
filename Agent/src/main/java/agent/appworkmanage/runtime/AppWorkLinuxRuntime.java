package agent.appworkmanage.runtime;

import agent.Context;
import common.Privileged.PrivilegedOperation;
import common.Privileged.PrivilegedOperationException;
import common.Privileged.PrivilegedOperationExecutor;
import common.struct.IOStreamPair;
import config.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

import static agent.appworkmanage.runtime.AppWorkRuntimeConstants.*;

public class AppWorkLinuxRuntime implements AppWorkRuntime {

    private final PrivilegedOperationExecutor privilegedOperationExecutor;

    public AppWorkLinuxRuntime(PrivilegedOperationExecutor privilegedOperationExecutor) {
        this.privilegedOperationExecutor = privilegedOperationExecutor;
    }

    @Override
    public void initialize(Configuration config, Context context) throws AppWorkExecutionException {

    }

    @Override
    public void prepareAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

    }

    @Override
    public void launchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {
        PrivilegedOperation launchOp = new PrivilegedOperation(PrivilegedOperation.OperationType.LAUNCH_CONTAINER);

        launchOp.appendArgs(ctx.getExecutionAttribute(RUN_AS_USER),
                ctx.getExecutionAttribute(USER),
                Integer.toString(PrivilegedOperation.RunAsUserCommand.LAUNCH_CONTAINER.getValue()),
                ctx.getExecutionAttribute(APPID),
                ctx.getExecutionAttribute(APP_WORK_ID_STR),
                ctx.getExecutionAttribute(APP_WORK_WORK_DIR).toString(),
                ctx.getExecutionAttribute(PID_FILE_PATH).toString(),
                StringUtils.join(File.separator, ctx.getExecutionAttribute(LOCAL_DIRS)),
                ctx.getExecutionAttribute(RESOURCES_OPTIONS));

        String toCommandFile = ctx.getExecutionAttribute(TC_COMMAND_FILE);

        if (toCommandFile != null) {
            launchOp.appendArgs(toCommandFile);
        }
        launchOp.disableFailureLogging();

        List<String> prefixCommands = (List<String>) ctx.getExecutionAttribute(APP_WORK_LAUNCH_PREFIX_COMMANDS);
        try {
            privilegedOperationExecutor.executePrivilegedOperation(prefixCommands, launchOp, null, null, false, false);
        } catch (PrivilegedOperationException e) {
            throw new AppWorkExecutionException("Launch AppWork failed",
                    e.getExitCode(), e.getOutput(), e.getErrorOutput());
        }
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
