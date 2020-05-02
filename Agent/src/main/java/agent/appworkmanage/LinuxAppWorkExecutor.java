package agent.appworkmanage;

import agent.Context;
import agent.context.AppWorkAlivenessContext;
import agent.context.AppWorkSignalContext;
import agent.context.AppWorkStartContext;
import agent.context.LocalizerStartContext;

import java.io.IOException;

public class LinuxAppWorkExecutor extends AppWorkExecutor {

    public enum ExitCode {
        SUCCESS(0),
        INVALID_ARGUMENT_NUMBER(1),
        INVALID_COMMAND_PROVIDED(3),
        INVALID_NM_ROOT_DIRS(5),
        SETUID_OPER_FAILED(6),
        UNABLE_TO_EXECUTE_CONTAINER_SCRIPT(7),
        UNABLE_TO_SIGNAL_CONTAINER(8),
        INVALID_CONTAINER_PID(9),
        OUT_OF_MEMORY(18),
        INITIALIZE_USER_FAILED(20),
        PATH_TO_DELETE_IS_NULL(21),
        INVALID_CONTAINER_EXEC_PERMISSIONS(22),
        INVALID_CONFIG_FILE(24),
        SETSID_OPER_FAILED(25),
        WRITE_PIDFILE_FAILED(26),
        WRITE_CGROUP_FAILED(27),
        TRAFFIC_CONTROL_EXECUTION_FAILED(28),
        DOCKER_RUN_FAILED(29),
        ERROR_OPENING_DOCKER_FILE(30),
        ERROR_READING_DOCKER_FILE(31),
        FEATURE_DISABLED(32),
        COULD_NOT_CREATE_SCRIPT_COPY(33),
        COULD_NOT_CREATE_CREDENTIALS_FILE(34),
        COULD_NOT_CREATE_WORK_DIRECTORIES(35),
        COULD_NOT_CREATE_APP_LOG_DIRECTORIES(36),
        COULD_NOT_CREATE_TMP_DIRECTORIES(37),
        ERROR_CREATE_CONTAINER_DIRECTORIES_ARGUMENTS(38);

        private final int code;
        ExitCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    @Override
    public void init(Context context) throws IOException {

    }

    @Override
    public void startLocalizer(LocalizerStartContext ctx) {

    }

    @Override
    public int launchAppWork(AppWorkStartContext ctx) {
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
