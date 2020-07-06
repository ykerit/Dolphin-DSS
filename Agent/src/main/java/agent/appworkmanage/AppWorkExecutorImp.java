package agent.appworkmanage;

import agent.Context;
import agent.appworkmanage.Localize.AppWorkLocalizer;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.ExecuteExitCode;
import agent.appworkmanage.cgroups.ResourceHandler;
import agent.appworkmanage.cgroups.ResourceHandlerPackage;
import agent.appworkmanage.runtime.AppWorkExecutionException;
import agent.appworkmanage.runtime.AppWorkLinuxRuntime;
import agent.appworkmanage.runtime.AppWorkRuntimeContext;
import agent.context.*;
import com.google.common.base.Optional;
import common.Privileged.PrivilegedOperation;
import common.Privileged.PrivilegedOperationException;
import common.Privileged.PrivilegedOperationExecutor;
import common.exception.ResourceHandleException;
import common.struct.AppWorkId;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static agent.appworkmanage.runtime.AppWorkRuntimeConstants.*;

public class AppWorkExecutorImp extends AppWorkExecutor {

    private static final Logger log = LogManager.getLogger(AppWorkExecutorImp.class.getName());

    private boolean appWorkSchedPriorityIsSet = false;
    private int appWorkSchedPriorityAdjustment = 0;
    private AppWorkLinuxRuntime linuxAppWorkRuntime;
    private ResourceHandler resourceHandlerCenter;
    private Context context;

    protected void addSchedPriorityCommand(List<String> command) {
        if (appWorkSchedPriorityIsSet) {
            command.addAll(Arrays.asList("nice", "-n", Integer.toString(appWorkSchedPriorityAdjustment)));
        }
    }

    protected PrivilegedOperationExecutor getPrivilegedOperationExecutor() {
        return PrivilegedOperationExecutor.getInstance(context.getConfiguration());
    }

    @Override
    public void init(Context context) throws IOException {
        this.context = context;
        PrivilegedOperation checkSetupOp = new PrivilegedOperation(PrivilegedOperation.
                OperationType.CHECK_SETUP);
        PrivilegedOperationExecutor privilegedOperationExecutor = getPrivilegedOperationExecutor();
//        try {
//            privilegedOperationExecutor.executePrivilegedOperation(checkSetupOp, false);
//        } catch (PrivilegedOperationException e) {
//            int exitCode = e.getExitCode();
//            log.warn("Exit code from AppWork executor initialize is {}", exitCode);
//        }
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
            AppWorkLinuxRuntime runtime = new AppWorkLinuxRuntime(privilegedOperationExecutor);
            runtime.initialize(context.getConfiguration(), context);
            this.linuxAppWorkRuntime = runtime;
        } catch (AppWorkExecutionException e) {
            log.error("Failed to initialize AppWork Runtime", e);
            throw new IOException("Failed to initialize AppWork Runtime", e);
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
    public void startLocalizer(LocalizerStartContext ctx) throws IOException {
        String user = ctx.getUser();
        String appId = ctx.getAppId();
        String locId = ctx.getLocId();
        List<String> localDir = null;

        PrivilegedOperation initializeAppWorkOp = new PrivilegedOperation(PrivilegedOperation.OperationType.INITIALIZE_CONTAINER);
        List<String> prefixCommands = new ArrayList<>();
        addSchedPriorityCommand(prefixCommands);
        initializeAppWorkOp.appendArgs(user,
                Integer.toString(PrivilegedOperation.RunAsUserCommand.INITIALIZE_CONTAINER.getValue()),
                appId,
                locId,
                StringUtils.join(File.separator, localDir));

        File jvm = new File(new File(System.getProperty("java.home"), "bin"), "java");
        initializeAppWorkOp.appendArgs(jvm.toString());
        initializeAppWorkOp.appendArgs("-classpath");
        initializeAppWorkOp.appendArgs(System.getProperty("java.class.path"));
        String javaLibPath = System.getProperty("java.library.path");
        if (javaLibPath != null) {
            initializeAppWorkOp.appendArgs("-Djava.library.path=" + javaLibPath);
        }
        initializeAppWorkOp.appendArgs(AppWorkLocalizer.getJavaOpts(context.getConfiguration()));
        List<String> localizerArgs = new ArrayList<>();
        buildMainArgs(localizerArgs, user, appId, locId, localDir);
        initializeAppWorkOp.appendArgs(localizerArgs);

        PrivilegedOperationExecutor privilegedOperationExecutor = getPrivilegedOperationExecutor();
        try {
            privilegedOperationExecutor.executePrivilegedOperation(prefixCommands, initializeAppWorkOp,
                    null, null, false, true);
        } catch (PrivilegedOperationException e) {
            int exitCode = e.getExitCode();
            log.warn("Exit code from AppWork {} startLocalizer is : {}", locId, exitCode, e);
            throw new IOException("Application " + appId + " initalization failed" +
                    "(exitCode=" + exitCode + ") with output: " + e.getOutput(), e);
        }
    }

    public void buildMainArgs(List<String> command, String user, String appId,
                              String locId, List<String> localDirs) {
        AppWorkLocalizer.buildMainArgs(command, user, appId, locId, localDirs);
    }

    @Override
    public void prepareAppWork(AppWorkPrepareContext ctx) throws IOException {
        AppWorkRuntimeContext.Builder builder = new AppWorkRuntimeContext.Builder(ctx.getAppWork());
        builder.setExecutionAttribute(LOCALIZED_RESOURCES, ctx.getLocalizeResource())
                .setExecutionAttribute(USER, ctx.getUser())
                .setExecutionAttribute(APP_WORK_LOCAL_DIRS, ctx.getAppWorkLocalDirs())
                .setExecutionAttribute(APP_WORK_RUN_CMDS, ctx.getCommands())
                .setExecutionAttribute(APP_WORK_ID_STR, ctx.getAppWork().getAppWorkId().toString());
        try {
            linuxAppWorkRuntime.prepareAppWork(builder.build());
        } catch (AppWorkExecutionException e) {
            throw new IOException("unable to prepare AppWork: ", e);
        }
    }

    @Override
    public int launchAppWork(AppWorkStartContext ctx) throws IOException {
        AppWork appWork = ctx.getAppWork();
        String user = ctx.getUser();
        AppWorkId appWorkId = appWork.getAppWorkId();
        String resourceOptions = null;
        String toCommandFile = null;
        try {
            if (resourceHandlerCenter != null) {

                List<PrivilegedOperation> ops = resourceHandlerCenter.preStart(appWork);
                if (ops != null) {
                    List<PrivilegedOperation> resourceOps = new ArrayList<>();
                    resourceOps.add(new PrivilegedOperation(
                            PrivilegedOperation.OperationType.ADD_PID_TO_CGROUP, ""));
                }

            }
        } catch (ResourceHandleException e) {
            log.error("ResourceHandlerCenter.preStart failed!", e);
            throw new IOException("ResourceHandlerCenter.preStart failed!", e);
        }
        try {
            Path pidFilePath = getPidFilePath(appWorkId);
            if (pidFilePath != null) {
                AppWorkRuntimeContext runtimeContext =
                        buildAppWorkRuntimeContext(ctx, pidFilePath, resourceOptions, toCommandFile);
                linuxAppWorkRuntime.launchAppWork(runtimeContext);
            } else {
                log.info("AppWork was marked as inactive, Returning terminated error");
                return ExitCode.TERMINATED.getExitCode();
            }
        } catch (AppWorkExecutionException e) {
            return handleExitCode(e, appWork, appWorkId);
        } finally {
            postComplete(appWorkId);
        }
        return 0;
    }

    private int handleExitCode(AppWorkExecutionException e, AppWork appWork,
                               AppWorkId appWorkId) throws IOException {
        int exitCode = e.getExitCode();
        log.warn("Exit code from AppWork {} is : {}", appWorkId, exitCode);
        // 143 (SIGTERM) and 137 (SIGKILL) exit codes means the container was
        // terminated/killed forcefully. In all other cases, log the
        // output
        if (exitCode != ExitCode.FORCE_KILLED.getExitCode()
                && exitCode != ExitCode.TERMINATED.getExitCode()) {
            log.warn("Exception from AppWork-launch with AppWork ID: {} "
                    + "and exit code: {}", appWorkId, exitCode, e);

            StringBuilder builder = new StringBuilder();
            builder.append("Exception from AppWork-launch.\n")
                    .append("AppWork id: " + appWorkId + "\n")
                    .append("Exit code: " + exitCode + "\n")
                    .append("Exception message: " + e.getMessage() + "\n");
            if (!Optional.fromNullable(e.getErrOutput()).or("").isEmpty()) {
                builder.append("Shell error output: " + e.getErrOutput() + "\n");
            }
            //Skip stack trace
            String output = e.getOutput();
            if (output != null && !output.isEmpty()) {
                builder.append("Shell output: " + output + "\n");
            }
            String diagnostics = builder.toString();
            if (exitCode ==
                    ExecuteExitCode.INVALID_CONTAINER_EXEC_PERMISSIONS.getCode() ||
                    exitCode ==
                            ExecuteExitCode.INVALID_CONFIG_FILE.getCode()) {
                throw new IOException(
                        "Linux AppWork Executor reached unrecoverable exception", e);
            }
        } else {

        }
        return exitCode;
    }

    private AppWorkRuntimeContext buildAppWorkRuntimeContext(
            AppWorkStartContext ctx, Path pidFilePath, String resourcesOptions,
            String tcCommandFile) {

        List<String> prefixCommands = new ArrayList<>();
        addSchedPriorityCommand(prefixCommands);

        AppWork appWork = ctx.getAppWork();

        AppWorkRuntimeContext.Builder builder = new AppWorkRuntimeContext
                .Builder(appWork);
        if (prefixCommands.size() > 0) {
            builder.setExecutionAttribute(APP_WORK_LAUNCH_PREFIX_COMMANDS,
                    prefixCommands);
        }

        builder.setExecutionAttribute(LOCALIZED_RESOURCES,
                ctx.getLocalizedResources())
                .setExecutionAttribute(RUN_AS_USER, appWork.getUser())
                .setExecutionAttribute(USER, ctx.getUser())
                .setExecutionAttribute(APPID, ctx.getAppId())
                .setExecutionAttribute(APP_WORK_ID_STR,
                        appWork.getAppWorkId().toString())
                .setExecutionAttribute(APP_WORK_ID_STR, ctx.getWorkspace())
                .setExecutionAttribute(PID_FILE_PATH, pidFilePath)
                .setExecutionAttribute(LOCAL_DIRS, ctx.getLocalDirs())
                .setExecutionAttribute(FILECACHE_DIRS, ctx.getFileCacheDirs())
                .setExecutionAttribute(USER_LOCAL_DIRS, ctx.getUserLocalDirs())
                .setExecutionAttribute(APP_WORK_LOCAL_DIRS, ctx.getAppWorkLocalDirs())
                .setExecutionAttribute(USER_FILECACHE_DIRS, ctx.getUserFileCacheDirs())
                .setExecutionAttribute(APPLICATION_LOCAL_DIRS,
                        ctx.getApplicationLocalDirs())
                .setExecutionAttribute(RESOURCES_OPTIONS, resourcesOptions);

        if (tcCommandFile != null) {
            builder.setExecutionAttribute(TC_COMMAND_FILE, tcCommandFile);
        }

        return builder.build();
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

    void postComplete(final AppWorkId containerId) {
        try {
            if (resourceHandlerCenter != null) {
                log.debug("{} post complete", containerId);
                resourceHandlerCenter.postComplete(containerId);
            }
        } catch (ResourceHandleException e) {
            log.warn("ResourceHandlerChain.postComplete failed for " +
                    "containerId: {}. Exception: ", containerId, e);
        }
    }

}
