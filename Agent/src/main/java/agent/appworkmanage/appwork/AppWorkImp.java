package agent.appworkmanage.appwork;

import common.struct.*;
import common.context.AppWorkLaunchContext;
import common.event.EventDispatcher;
import common.resource.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* In the future, will support task interrupt and resume
 ** the status represents future;
 */

public class AppWorkImp implements AppWork {

    private static final Logger log = LogManager.getLogger(AppWorkImp.class.getName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EventDispatcher dispatcher;
    private AppWorkId appWorkId;
    private AgentId agentId;
    private final ApplicationId appId;
    private final String user;
    private final int exitCode;
    private boolean isLaunched;
    private long appWorkLocalizationStartTime;
    private long appWorkLaunchStartTime;
    private AppWorkExecType type;

    private volatile AppWorkLaunchContext launchContext;

    private Path workspace;


    public AppWorkImp(EventDispatcher dispatcher, AppWorkLaunchContext context, long startTime, String user, ApplicationId appId) {
        this.dispatcher = dispatcher;
        this.launchContext = context;
        this.exitCode = 0;
        this.user = user;
        this.appId = appId;
    }

    @Override
    public ApplicationId getAppId() {
        return appId;
    }

    @Override
    public AgentId getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(AgentId agentId) {

    }

    @Override
    public AppWorkId getAppWorkId () {
        return null;
    }

    @Override
    public void setAppWorkId(AppWorkId appWorkId) {

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
    public void setResource(Resource resource) {

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
    public Priority getPriority() {
        return null;
    }

    @Override
    public void setPriority() {

    }

    @Override
    public Set<String> getAllocationTags() {
        return null;
    }

    @Override
    public void setAllocationTags(Set<String> allocationTags) {

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
    public Map<Path, List<String>> getLocalizeResource() {
        return null;
    }

    @Override
    public void process(AppWorkEvent event) {

    }
}
