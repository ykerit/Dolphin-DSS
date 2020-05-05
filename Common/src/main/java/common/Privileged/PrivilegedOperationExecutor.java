package common.Privileged;

import common.exception.ExitCodeException;
import common.struct.IOStreamPair;
import common.util.Shell.ShellCommandExecutor;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PrivilegedOperationExecutor {
    private static final Logger log = LogManager.getLogger(PrivilegedOperationExecutor.class.getName());
    private volatile static PrivilegedOperationExecutor instance;

    private String containerExecutorExe;

    public static String getContainerExecutorExecutablePath(Configuration conf) {

        return null;
    }

    private void init(Configuration conf) {
        containerExecutorExe = getContainerExecutorExecutablePath(conf);
    }

    private PrivilegedOperationExecutor(Configuration conf) {
        init(conf);
    }

    public static PrivilegedOperationExecutor getInstance(Configuration conf) {
        if (instance == null) {
            synchronized (PrivilegedOperationExecutor.class) {
                if (instance == null) {
                    instance = new PrivilegedOperationExecutor(conf);
                }
            }
        }
        return instance;
    }

    /**
     * @param prefixCommands in some cases ( e.g priorities using nice ),
     *                       prefix commands are necessary
     * @param operation      the type and arguments for the operation to be
     *                       executed
     * @return execution string array for priviledged operation
     */

    public String[] getPrivilegedOperationExecutionCommand(List<String>
                                                                   prefixCommands,
                                                           PrivilegedOperation operation) {
        List<String> fullCommand = new ArrayList<String>();

        if (prefixCommands != null && !prefixCommands.isEmpty()) {
            fullCommand.addAll(prefixCommands);
        }

        fullCommand.add(containerExecutorExe);

        String cliSwitch = operation.getOperationType().getOption();

        if (!cliSwitch.isEmpty()) {
            fullCommand.add(cliSwitch);
        }

        fullCommand.addAll(operation.getArguments());

        String[] fullCommandArray =
                fullCommand.toArray(new String[fullCommand.size()]);

        if (log.isDebugEnabled()) {
            log.debug("Privileged Execution Command Array: " +
                    Arrays.toString(fullCommandArray));
        }

        return fullCommandArray;
    }

    /**
     * Executes a privileged operation. It is up to the callers to ensure that
     * each privileged operation's parameters are constructed correctly. The
     * parameters are passed verbatim to the container-executor binary.
     *
     * @param prefixCommands   in some cases ( e.g priorities using nice ),
     *                         prefix commands are necessary
     * @param operation        the type and arguments for the operation to be executed
     * @param workingDir       (optional) working directory for execution
     * @param env              (optional) env of the command will include specified vars
     * @param grabOutput       return (possibly large) shell command output
     * @param inheritParentEnv inherit the env vars from the parent process
     * @return stdout contents from shell executor - useful for some privileged
     * operations - e.g --tc_read
     */
    public String executePrivilegedOperation(List<String> prefixCommands,
                                             PrivilegedOperation operation, File workingDir,
                                             Map<String, String> env, boolean grabOutput, boolean inheritParentEnv)
            throws PrivilegedOperationException {
        String[] fullCommandArray = getPrivilegedOperationExecutionCommand
                (prefixCommands, operation);
        ShellCommandExecutor exec = new ShellCommandExecutor(fullCommandArray,
                workingDir, env, 0L, inheritParentEnv);

        try {
            exec.execute();
            if (log.isDebugEnabled()) {
                log.debug("command array:");
                log.debug(Arrays.toString(fullCommandArray));
                log.debug("Privileged Execution Operation Output:");
                log.debug(exec.getOutput());
            }
        } catch (ExitCodeException e) {
            if (operation.isFailureLoggingEnabled()) {
                StringBuilder logBuilder = new StringBuilder("Shell execution returned "
                        + "exit code: ")
                        .append(exec.getExitCode())
                        .append(". Privileged Execution Operation Stderr: ")
                        .append(System.lineSeparator())
                        .append(e.getMessage())
                        .append(System.lineSeparator())
                        .append("Stdout: " + exec.getOutput())
                        .append(System.lineSeparator());
                logBuilder.append("Full command array for failed execution: ")
                        .append(System.lineSeparator());
                logBuilder.append(Arrays.toString(fullCommandArray));

                log.warn(logBuilder.toString());
            }

            //stderr from shell executor seems to be stuffed into the exception
            //'message' - so, we have to extract it and set it as the error out
            throw new PrivilegedOperationException(e, e.getExitCode(),
                    exec.getOutput(), e.getMessage());
        } catch (IOException e) {
            log.warn("IOException executing command: ", e);
            throw new PrivilegedOperationException(e);
        }

        if (grabOutput) {
            return exec.getOutput();
        }

        return null;
    }

    /**
     * Executes a privileged operation. It is up to the callers to ensure that
     * each privileged operation's parameters are constructed correctly. The
     * parameters are passed verbatim to the container-executor binary.
     *
     * @param operation  the type and arguments for the operation to be executed
     * @param grabOutput return (possibly large) shell command output
     * @return stdout contents from shell executor - useful for some privileged
     * operations - e.g --tc_read
     */
    public String executePrivilegedOperation(PrivilegedOperation operation,
                                             boolean grabOutput) throws PrivilegedOperationException {
        return executePrivilegedOperation(null, operation, null, null, grabOutput,
                false);
    }

    /**
     * @param prefixCommands
     * @param operation
     * @return stdin and stdout of container exec
     * @throws PrivilegedOperationException
     */
    public IOStreamPair executePrivilegedInteractiveOperation(
            List<String> prefixCommands, PrivilegedOperation operation)
            throws PrivilegedOperationException, InterruptedException {
        String[] fullCommandArray = getPrivilegedOperationExecutionCommand(
                prefixCommands, operation);
        ProcessBuilder pb = new ProcessBuilder(fullCommandArray);
        OutputStream stdin;
        InputStream stdout;
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            stdin = p.getOutputStream();
            stdout = p.getInputStream();

            if (log.isDebugEnabled()) {
                log.debug("command array:");
                log.debug(Arrays.toString(fullCommandArray));
            }
        } catch (ExitCodeException e) {
            if (operation.isFailureLoggingEnabled()) {
                StringBuilder logBuilder = new StringBuilder(
                        "Interactive Shell execution returned exit code: ")
                        .append(e.getExitCode())
                        .append(". Privileged Interactive Operation Stderr: ")
                        .append(System.lineSeparator())
                        .append(e.getMessage())
                        .append(System.lineSeparator());
                logBuilder.append("Full command array for failed execution: ")
                        .append(System.lineSeparator());
                logBuilder.append(Arrays.toString(fullCommandArray));

                log.warn(logBuilder.toString());
            }

            //stderr from shell executor seems to be stuffed into the exception
            //'message' - so, we have to extract it and set it as the error out
            throw new PrivilegedOperationException(e, e.getExitCode(),
                    pb.redirectError().toString(), e.getMessage());
        } catch (IOException e) {
            log.warn("IOException executing command: ", e);
            throw new PrivilegedOperationException(e);
        }

        return new IOStreamPair(stdout, stdin);
    }

    //Utility functions for squashing together operations in supported ways
    //At some point, we need to create a generalized mechanism that uses a set
    //of squashing 'rules' to squash an set of PrivilegedOperations of varying
    //types - e.g Launch Container + Add Pid to CGroup(s) + TC rules

    /**
     * Squash operations for cgroups - e.g mount, add pid to cgroup etc .,
     * For now, we only implement squashing for 'add pid to cgroup' since this
     * is the only optimization relevant to launching containers
     *
     * @return single squashed cgroup operation. Null on failure.
     */

    public static PrivilegedOperation squashCGroupOperations
    (List<PrivilegedOperation> ops) throws PrivilegedOperationException {
        if (ops.size() == 0) {
            return null;
        }

        StringBuffer finalOpArg = new StringBuffer(PrivilegedOperation
                .CGROUP_ARG_PREFIX);
        boolean noTasks = true;

        for (PrivilegedOperation op : ops) {
            if (!op.getOperationType()
                    .equals(PrivilegedOperation.OperationType.ADD_PID_TO_CGROUP)) {
                log.warn("Unsupported operation type: " + op.getOperationType());
                throw new PrivilegedOperationException("Unsupported operation type:"
                        + op.getOperationType());
            }

            List<String> args = op.getArguments();
            if (args.size() != 1) {
                log.warn("Invalid number of args: " + args.size());
                throw new PrivilegedOperationException("Invalid number of args: "
                        + args.size());
            }

            String arg = args.get(0);
            String tasksFile = substringAfter(arg,
                    PrivilegedOperation.CGROUP_ARG_PREFIX);

            if (tasksFile == null || tasksFile.isEmpty()) {
                log.warn("Invalid argument: " + arg);
                throw new PrivilegedOperationException("Invalid argument: " + arg);
            }

            if (tasksFile.equals(PrivilegedOperation.CGROUP_ARG_NO_TASKS)) {
                //Don't append to finalOpArg
                continue;
            }

            if (noTasks == false) {
                //We have already appended at least one tasks file.
                finalOpArg.append(PrivilegedOperation.LINUX_FILE_PATH_SEPARATOR);
                finalOpArg.append(tasksFile);
            } else {
                finalOpArg.append(tasksFile);
                noTasks = false;
            }
        }

        if (noTasks) {
            finalOpArg.append(PrivilegedOperation.CGROUP_ARG_NO_TASKS); //there
            // were no tasks file to append
        }

        PrivilegedOperation finalOp = new PrivilegedOperation(
                PrivilegedOperation.OperationType.ADD_PID_TO_CGROUP, finalOpArg
                .toString());

        return finalOp;
    }

    static String substringAfter(String value, String delim) {
        int pos = value.indexOf(delim);
        if (pos >= 0) {
            return value.substring(pos + 1);
        }
        return null;
    }
}