package agent.appworkmanage;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkImp;
import agent.context.*;
import common.struct.AppWorkId;
import common.util.Tools;
import config.Configuration;
import config.DolphinConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AppWorkExecutor {
    private static final Logger log = LogManager.getLogger(AppWorkExecutor.class.getName());

    private final ConcurrentMap<AppWorkId, Path> pidFiles = new ConcurrentHashMap<>();

    public abstract void init(Context context) throws IOException;

    public void start() {
    }

    public void stop() {
    }

    public void prepareAppWork(AppWorkPrepareContext ctx) throws IOException {
    }

    public abstract void startLocalizer(LocalizerStartContext ctx) throws IOException;

    // Launch AppWork on the node, This is a blocking call and return only AppWork exit
    public abstract int launchAppWork(AppWorkStartContext ctx) throws IOException;

    // Relaunch AppWork on the node, This is a blocking call and return only AppWork exit
    public abstract int relaunchAppWork(AppWorkStartContext ctx) throws IOException;

    public abstract boolean signalAppWork(AppWorkSignalContext ctx) throws IOException;

    public abstract boolean reapAppWork() throws IOException;

    // C or C++ program
    public abstract void symLink(String target, String symlink);

    public abstract boolean isAppWorkAlive(AppWorkAlivenessContext ctx) throws IOException;

    public enum ExitCode {
        SUCCESS(0),
        FORCE_KILLED(137),
        TERMINATED(143),
        LOST(154);

        private final int code;

        private ExitCode(int exitCode) {
            this.code = exitCode;
        }

        public int getExitCode() {
            return code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    public enum Signal {
        NULL(0, "NULL"),
        QUIT(3, "SIGQUIT"),
        KILL(9, "SIGKILL"),
        TERM(15, "SIGTERM");

        private final int val;
        private final String str;

        private Signal(int val, String str) {
            this.val = val;
            this.str = str;
        }

        public int getValue() {
            return val;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    protected String[] getRunCommandForLinux(String command, Configuration config) {
        boolean appWorkPriorityIsSet = false;
        List<String> retCommand = new ArrayList<>();
        int appWorkPriorityAdjustment = DolphinConfiguration.DEFAULT_APP_WORK_PRIORITY;
        if (config.getAppWorkPriority() != null) {
            appWorkPriorityIsSet = true;
            appWorkPriorityAdjustment = config.getAppWorkPriority();
        }
        if (appWorkPriorityIsSet) {
            retCommand.addAll(Arrays.asList("nice", "-n", Integer.toString(appWorkPriorityAdjustment)));
        }
        retCommand.addAll(Arrays.asList("bash", command));
        return retCommand.toArray(new String[retCommand.size()]);
    }

    // Clean up before relaunch
    public void cleanupBeforeReLaunch(AppWork appWork) {

    }

    // kill process
    public static class WaitProcessKiller extends Thread {
        private final AppWork appWork;
        private final String user;
        private final int pid;
        private final long delay;
        private final Signal signal;
        private final AppWorkExecutor appWorkExecutor;

        public WaitProcessKiller(AppWork appWork,
                                 String user,
                                 int pid,
                                 long delay,
                                 Signal signal,
                                 AppWorkExecutor appWorkExecutor) {
            this.appWork = appWork;
            this.user = user;
            this.pid = pid;
            this.delay = delay;
            this.signal = signal;
            this.appWorkExecutor = appWorkExecutor;

            setName("killer for pid: " + pid);
            setDaemon(false);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                appWorkExecutor.signalAppWork(new AppWorkSignalContext.Builder()
                        .setAppWork(appWork)
                        .setPid(pid)
                        .setUser(user)
                        .setSignal(signal).build());
            } catch (InterruptedException e) {
                interrupt();
            } catch (IOException e) {
                String msg = "Exception when user "
                        + user + "killing task "
                        + pid + "in processKiller: "
                        + e.getMessage();
                log.warn(msg);
            }
        }
    }

    protected Path getPidFilePath(AppWorkId appWokId) {
        return this.pidFiles.get(appWokId);
    }

    public String getProcessId(AppWorkId appWorkId) {
        String pid = null;
        Path pidFile = pidFiles.get(appWorkId);

        // If PID is null, this container hasn't launched yet.
        if (pidFile != null) {
            try {
                pid = Tools.getProcessId(pidFile);
            } catch (IOException e) {
                log.error("Got exception reading pid from pid-file {}", pidFile, e);
            }
        }

        return pid;
    }
}
