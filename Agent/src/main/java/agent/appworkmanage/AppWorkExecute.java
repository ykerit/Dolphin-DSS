package agent.appworkmanage;

import agent.AgentContext;
import agent.context.AppWorkSignalContext;
import agent.context.AppWorkStartContext;
import agent.context.LocalizerStartContext;
import config.Configuration;
import config.DolphinConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AppWorkExecute {
    public abstract void init(AgentContext context);

    public void start() {
    }

    public void stop() {
    }

    public abstract void startLocalizer(LocalizerStartContext ctx);

    public void prepareAppWork() {
    }

    // Launch AppWork on the node, This is a blocking call and return only AppWork exit
    public abstract int launchAppWork(AppWorkStartContext ctx);

    // Relaunch AppWork on the node, This is a blocking call and return only AppWork exit
    public abstract int relaunchAppWork(AppWorkStartContext ctx);

    public abstract boolean signalAppWork(AppWorkSignalContext ctx);

    public abstract boolean reapAppWork();

    // C or C++ program
    public abstract void symLink(String target, String symlink);

    public abstract boolean isAppWorkAlive();

    public enum Signal {
        NULL(0, "NULL"),
        QUIT(1, "SIGQUIT"),
        KILL(2, "SIGKILL");

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
        private final AppWorkExecute appWorkExecute;

        public WaitProcessKiller(AppWork appWork,
                                 String user,
                                 int pid,
                                 long delay,
                                 Signal signal,
                                 AppWorkExecute appWorkExecute) {
            this.appWork = appWork;
            this.user = user;
            this.pid = pid;
            this.delay = delay;
            this.signal = signal;
            this.appWorkExecute = appWorkExecute;

            setName("killer for pid: " + pid);
            setDaemon(false);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                appWorkExecute.signalAppWork(new AppWorkSignalContext.Builder()
                        .setAppWork(appWork)
                        .setPid(pid)
                        .setUser(user)
                        .setSignal(signal).build());
            } catch (InterruptedException e) {
                interrupt();
                e.printStackTrace();
            }
        }
    }
}
