package agent.appworkmanage;

import agent.AgentContext;
import agent.context.*;
import common.exception.ExitCodeException;
import common.util.Shell;
import common.util.Shell.ShellCommandExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DefaultAppWorkExecutor extends AppWorkExecutor {
    private static final Logger log = LogManager.getLogger(DefaultAppWorkExecutor.class);

    public DefaultAppWorkExecutor() {

    }

    @Override
    public void init(AgentContext context) {
        // when something need to do
    }

    @Override
    public void start() { }

    @Override
    public void stop() { }

    protected void setScriptExecutable(Path path, String owner) {

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
        String user = ctx.getUser();
        int pid = ctx.getPid();
        Signal signal = ctx.getSignal();
        log.debug("send signal {} to pid: {} user: {}", signal.getValue(), pid, signal);
        if (!appWorkIsAlive(pid)) {
            return false;
        }
        try {
            killAppWork(pid, signal);
        } catch (IOException e) {
            if (!appWorkIsAlive(pid)) {
                return false;
            }
            throw e;
        }
        return true;
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
        int pid = ctx.getPid();
        return appWorkIsAlive(pid);
    }

    public static boolean appWorkIsAlive(int pid) throws IOException {
        try {
            new ShellCommandExecutor(Shell.getCheckProcessIsAliveCommand(pid)).execute();
            return true;
        } catch (ExitCodeException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void killAppWork(int pid, Signal signal) throws IOException {
        new ShellCommandExecutor(Shell.getSignalKillCommand(signal.getValue(), pid)).execute();
    }

    void createAppDirs(List<String> localDirs, String user, long appId) {

    }
}
