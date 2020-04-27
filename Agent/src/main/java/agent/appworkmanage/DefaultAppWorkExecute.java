package agent.appworkmanage;

import agent.AgentContext;
import agent.context.AppWorkAlivenessContext;
import agent.context.AppWorkSignalContext;
import agent.context.AppWorkStartContext;
import agent.context.LocalizerStartContext;
import common.exception.ExitCodeException;
import common.util.Shell;
import common.util.Shell.ShellCommandExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class DefaultAppWorkExecute extends AppWorkExecute{
    private static final Logger log = LogManager.getLogger(DefaultAppWorkExecute.class);

    public DefaultAppWorkExecute() {

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
    public boolean signalAppWork(AppWorkSignalContext ctx) {
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
}