package common.Privileged;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivilegedOperation {
    public final static char LINUX_FILE_PATH_SEPARATOR = '%';

    public enum OperationType {
        CHECK_SETUP("--checksetup"),
        MOUNT_CGROUPS("--mount-cgroups"),
        INITIALIZE_CONTAINER(""), //no CLI switch supported yet
        LAUNCH_CONTAINER(""), //no CLI switch supported yet
        SIGNAL_CONTAINER(""), //no CLI switch supported yet
        EXEC_CONTAINER("--exec-container"), //no CLI switch supported yet
        DELETE_AS_USER(""), //no CLI switch supported yet
        LAUNCH_DOCKER_CONTAINER(""), //no CLI switch supported yet
        TC_MODIFY_STATE("--tc-modify-state"),
        TC_READ_STATE("--tc-read-state"),
        TC_READ_STATS("--tc-read-stats"),
        ADD_PID_TO_CGROUP(""), //no CLI switch supported yet.
        LIST_AS_USER(""), // no CLI switch supported yet.
        ADD_NUMA_PARAMS(""), // no CLI switch supported yet.
        SYNC_YARN_SYSFS("");

        private final String option;

        OperationType(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

    public static final String CGROUP_ARG_PREFIX = "cgroups=";
    public static final String CGROUP_ARG_NO_TASKS = "none";

    private final OperationType opType;
    private final List<String> args;
    private boolean failureLogging;

    public PrivilegedOperation(OperationType opType) {
        this.opType = opType;
        this.args = new ArrayList<String>();
        this.failureLogging = true;
    }

    public PrivilegedOperation(OperationType opType, String arg) {
        this(opType);

        if (arg != null) {
            this.args.add(arg);
        }
    }

    public PrivilegedOperation(OperationType opType, List<String> args) {
        this(opType);

        if (args != null) {
            this.args.addAll(args);
        }
    }

    public void appendArgs(String... args) {
        for (String arg : args) {
            this.args.add(arg);
        }
    }

    public void appendArgs(List<String> args) {
        this.args.addAll(args);
    }

    public void enableFailureLogging() {
        this.failureLogging = true;
    }

    public void disableFailureLogging() {
        this.failureLogging = false;
    }

    public boolean isFailureLoggingEnabled() {
        return failureLogging;
    }

    public OperationType getOperationType() {
        return opType;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(this.args);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof PrivilegedOperation)) {
            return false;
        }

        PrivilegedOperation otherOp = (PrivilegedOperation) other;

        return otherOp.opType.equals(opType) && otherOp.args.equals(args);
    }

    @Override
    public int hashCode() {
        return opType.hashCode() + 97 * args.hashCode();
    }

    public enum RunAsUserCommand {
        INITIALIZE_CONTAINER(0),
        LAUNCH_CONTAINER(1),
        SIGNAL_CONTAINER(2),
        DELETE_AS_USER(3),
        LIST_AS_USER(5),
        SYNC_YARN_SYSFS(6);

        private int value;
        RunAsUserCommand(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public enum ResultCode {
        OK(0),
        INVALID_USER_NAME(2),
        UNABLE_TO_EXECUTE_CONTAINER_SCRIPT(7),
        INVALID_CONTAINER_PID(9),
        INVALID_CONTAINER_EXEC_PERMISSIONS(22),
        INVALID_CONFIG_FILE(24),
        WRITE_CGROUP_FAILED(27);

        private final int value;
        ResultCode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
}
