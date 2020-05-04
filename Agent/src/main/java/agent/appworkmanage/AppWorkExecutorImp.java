package agent.appworkmanage;

import agent.Context;
import agent.appworkmanage.runtime.AppWorkExecutionException;
import agent.appworkmanage.runtime.DefaultLinuxRuntime;
import agent.appworkmanage.runtime.LinuxAppWorkRuntime;
import agent.context.AppWorkAlivenessContext;
import agent.context.AppWorkSignalContext;
import agent.context.AppWorkStartContext;
import agent.context.LocalizerStartContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AppWorkExecutorImp extends AppWorkExecutor {

    private static final Logger log = LogManager.getLogger(LinuxAppWorkRuntime.class.getName());

    private boolean appWorkSchedPriorityIsSet = false;
    private int appWorkSchedPriorityAdjustment = 0;
    private LinuxAppWorkRuntime linuxAppWorkRuntime;
    private Context context;

    @Override
    public void init(Context context) throws IOException {
        this.context = context;
        try {
            LinuxAppWorkRuntime runtime = new DefaultLinuxRuntime();
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

    public void mountCgroups(List<String> cgroupKVs, String hierarchy) {

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
