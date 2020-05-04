package agent.appworkmanage.appwork;

import agent.context.AppWorkLaunchContext;
import agent.status.AppWorkStatus;
import common.event.EventDispatcher;
import common.resource.Resource;
import common.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.text.SimpleDateFormat;

/* In the future, will support task interrupt and resume
 ** the status represents future;
 */

public class AppWorkImp implements AppWork {

    private static final Logger log = LogManager.getLogger(AppWorkImp.class.getName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EventDispatcher dispatcher;
    private final String appWorkId;
    private final long appId;
    private final String user;
    private final int exitCode;
    private boolean isLaunched;
    private long appWorkLocalizationStartTime;
    private long appWorkLaunchStartTime;
    private AppWorkExecType type;

    private volatile AppWorkLaunchContext launchContext;

    private Path workspace;


    public AppWorkImp(EventDispatcher dispatcher, AppWorkLaunchContext context, long startTime, String user, long appId) {
        this.dispatcher = dispatcher;
        this.launchContext = context;
        this.appWorkId = Tools.GenerateContainerID();
        this.exitCode = 0;
        this.user = user;
        this.appId = appId;
    }

    @Override
    public long getAppId() {
        return appId;
    }

    @Override
    public String getAppWorkId() {
        return null;
    }

    @Override
    public String getAppWorkStartTime() {
        return null;
    }

    @Override
    public String getAppWorkLaunchedTime() {
        return null;
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return launchContext;
    }

    @Override
    public AppWorkState getAppWorkState() {
        return null;
    }

    @Override
    public Path getWorkDir() {
        return null;
    }

    @Override
    public void setWorkDir() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public AppWorkExecType getExecType() {
        return type;
    }

    @Override
    public void setIsReInitializing(boolean isReInitializing) {

    }

    @Override
    public boolean isReInitializing() {
        return false;
    }

    @Override
    public void sendLaunchEvent() {

    }

    @Override
    public void sendKillEvent(int exitStatus, String description) {

    }

    @Override
    public boolean isRecovering() {
        return false;
    }

    @Override
    public AppWorkStatus cloneAndGetAppWorkStatus() {
        return null;
    }

    @Override
    public void process(AppWorkEvent event) {

    }
}
