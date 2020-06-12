package example.distributedshell;

import common.resource.Resource;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AppMaster {
    private static final Logger log = LogManager.getLogger(AppMaster.class);

    private Configuration configuration;
    private ApplicationId appId;
    private String appName;

    protected  int numTotalAppWroks = 1;
    private static final long DEFAULT_APP_WORK_MEMORY = 10;
    private static final int DEFAULT_APP_WORK_VCORES = 1;
    private int appWorkVirtualCores = DEFAULT_APP_WORK_VCORES;
    private Map<String, Long> appWorkResources = new HashMap<>();
    private int requestPriority;
    private boolean autoPromoteAppWorks = false;
    Map<String, Resource> resourceProfiles;
    private AtomicInteger numCompletedAppWorks = new AtomicInteger(0);
    private AtomicInteger numAllocatedAppWorks = new AtomicInteger(0);
    private AtomicInteger numFailedAppWorks = new AtomicInteger(0);
    protected AtomicInteger numRequestedAppWorks= new AtomicInteger(0);

    private String shellCommand = "";
    private String shellArgs = "";
    private Map<String, String> shellEnv = new HashMap<>();
    private String scriptPath = "";
    private long shellScriptPathTimestamp = 0;
    private long shellScriptPathLen = 0;
    private Map<String, PlacementSpec> placementSpecMap = null;

    private List<String> localizableFiles = new ArrayList<>();
    private volatile boolean done;
    private List<Thread> launchThreads = new ArrayList<>();
    private final String bash_command = "bash";

    private int shellIdCounter = 1;
    private final AtomicLong allocIdCounter = new AtomicLong(1);
    protected final Set<AppWorkId> launchedAppWorks =
            Collections.newSetFromMap(new ConcurrentHashMap<AppWorkId, Boolean>());
    private final ConcurrentMap<AppWorkId, Long> appWorkStartTimes =
            new ConcurrentHashMap<>();
    private ConcurrentMap<AppWorkId, Long> getAppWorkStartTimes() {
        return appWorkStartTimes;
    }

    public static void main(String[] args) {
        boolean result = false;
        AppMaster appMaster = null;
    }

    public AppMaster() {
        configuration = new Configuration();
    }


}
