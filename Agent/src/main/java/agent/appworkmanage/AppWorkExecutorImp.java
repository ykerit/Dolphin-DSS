package agent.appworkmanage;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.cgroups.ResourceHandler;
import agent.appworkmanage.cgroups.ResourceHandlerPackage;
import agent.appworkmanage.runtime.AppWorkExecutionException;
import agent.appworkmanage.runtime.AppWorkLinuxRuntime;
import agent.context.AppWorkAlivenessContext;
import agent.context.AppWorkSignalContext;
import agent.context.AppWorkStartContext;
import agent.context.LocalizerStartContext;
import common.Privileged.PrivilegedOperation;
import common.Privileged.PrivilegedOperationException;
import common.Privileged.PrivilegedOperationExecutor;
import common.exception.ResourceHandleException;
import common.struct.AppWorkId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppWorkExecutorImp extends AppWorkExecutor {

    private static final Logger log = LogManager.getLogger(AppWorkExecutorImp.class.getName());

    private boolean appWorkSchedPriorityIsSet = false;
    private int appWorkSchedPriorityAdjustment = 0;
    private AppWorkLinuxRuntime linuxAppWorkRuntime;
    private ResourceHandler resourceHandlerCenter;
    private Context context;

    protected String getAppWorkExecutorExecutablePath() {
        return context.getConfiguration().DEFAULT_APP_WORK_EXECUTOR_PATH;
    }

    @Override
    public void init(Context context) throws IOException {
        this.context = context;
            PrivilegedOperation checkSetupOp = new PrivilegedOperation(PrivilegedOperation.
                    OperationType.CHECK_SETUP);
            PrivilegedOperationExecutor privilegedOperationExecutor = getPrivilegedOperationExecutor();
        try {
            privilegedOperationExecutor.executePrivilegedOperation(checkSetupOp, false);
        } catch (PrivilegedOperationException e) {
            int exitCode = e.getExitCode();
          log.warn("Exit code from AppWork executor initialize is {}", exitCode);
        }
        try {
            resourceHandlerCenter = ResourceHandlerPackage.getResourceHandlerCenter(context.getConfiguration(), context);
            if (resourceHandlerCenter != null) {
                log.debug("now start to bootstrap resource");
                resourceHandlerCenter.bootstrap(context.getConfiguration());
            }
        } catch (ResourceHandleException e) {
            log.error("Failed to bootstrap set resource subsystem");
            throw new IOException("Failed to bootstrap set resource subsystem");
        }

        try {
            AppWorkLinuxRuntime runtime = new AppWorkLinuxRuntime();
            runtime.initialize(context);
            this.linuxAppWorkRuntime = runtime;
        } catch (AppWorkExecutionException e) {
            log.error("Failed to initialize AppWork Runtime", e);
            throw new IOException("Failed to initialize AppWork Runtime", e);
        }
    }

    protected void addSchedPriorityCommand(List<String> command) {
        if (appWorkSchedPriorityIsSet) {
            command.addAll(Arrays.asList("nice", "-n", Integer.toString(appWorkSchedPriorityAdjustment)));
        }
    }

    protected PrivilegedOperationExecutor getPrivilegedOperationExecutor() {
        return PrivilegedOperationExecutor.getInstance(context.getConfiguration());
    }

    public void mountCgroups(List<String> cgroupKVs, String hierarchy) throws IOException {
        PrivilegedOperation mountCgroupsOp = new PrivilegedOperation(PrivilegedOperation.OperationType.MOUNT_CGROUPS,
                hierarchy);
        mountCgroupsOp.appendArgs(cgroupKVs);
        PrivilegedOperationExecutor privilegedOperationExecutor = getPrivilegedOperationExecutor();
        try {
            privilegedOperationExecutor.executePrivilegedOperation(mountCgroupsOp, false);
        } catch (PrivilegedOperationException e) {
            int exitCode = e.getExitCode();
            log.warn("Exception in AppWorkExecutor mountCgroups ", e);

            throw new IOException("Problem mounting cgroups " + cgroupKVs +
                    "; exit code = " + exitCode + " and output: " + e.getOutput(),
                    e);
        }

    }

    @Override
    public void start() {
        super.start();
        linuxAppWorkRuntime.start();
    }

    @Override
    public void stop() {
        super.stop();
        linuxAppWorkRuntime.stop();
    }

    @Override
    public void startLocalizer(LocalizerStartContext ctx) {
        // application need resource to download from ceph
    }

    @Override
    public int launchAppWork(AppWorkStartContext ctx) {
        AppWork appWork = ctx.getAppWork();
        String user = ctx.getUser();
        AppWorkId appWorkId = appWork.getAppWorkId();

        if (resourceHandlerCenter != null) {
            try {
                List<PrivilegedOperation> ops = resourceHandlerCenter.preStart(appWork);
                if (ops != null) {
                    List<PrivilegedOperation> resourceOps = new ArrayList<>();
                    resourceOps.add(new PrivilegedOperation(
                            PrivilegedOperation.OperationType.ADD_PID_TO_CGROUP,"" ));
                }
            } catch (ResourceHandleException e) {

            }
        }
        return 0;
    }

    @Override
    public int relaunchAppWork(AppWorkStartContext ctx) {
        return 0;
    }

    @Override
    public boolean signalAppWork(AppWorkSignalContext ctx) throws IOException {
        return false;
    }

    @Override
    public boolean reapAppWork() {
        return false;
    }

    @Override
    public void symLink(String target, String symlink) {

    }

    @Override
    public boolean isAppWorkAlive(AppWorkAlivenessContext ctx) throws IOException {
        return false;
    }

}
