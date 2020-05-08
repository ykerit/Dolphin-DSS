package agent.appworkmanage.cgroups;

import common.Privileged.PrivilegedOperationExecutor;
import common.exception.ResourceHandleException;
import common.util.SystemClock;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CGroupsHandlerImp implements CGroupsHandler {

    private static final Logger log = LogManager.getLogger(CGroupsHandlerImp.class.getName());

    private static final String MOUNT_INFO = "/proc/mounts";
    private static final String CGROUPS_FSTYPE = "cgroup";

    private final String mountInfoFile;
    private final String cGroupPrefix;
    private final long deleteCGroupTimeout;
    private final long deleteCGroupDelay;
    private Map<CGroupController, String> controllerPaths;
    private Map<String, Set<String>> parsedMount;
    private final ReadWriteLock rwLock;
    private final PrivilegedOperationExecutor privilegedOperationExecutor;
    private final SystemClock clock;

    CGroupsHandlerImp(Configuration config, PrivilegedOperationExecutor privilegedOperationExecutor, String mount) throws ResourceHandleException {
        this.cGroupPrefix = config.DEFAULT_CGROUP_HIERARCHY;
        this.deleteCGroupTimeout = config.DEFAULT_DELETE_CGROUP_TIMEOUT;
        this.deleteCGroupDelay = config.DEFAULT_DELETE_CGROUP_DELAY;
        this.controllerPaths = new HashMap<>();
        this.parsedMount = new HashMap<>();
        this.rwLock = new ReentrantReadWriteLock();
        this.privilegedOperationExecutor = privilegedOperationExecutor;
        this.clock = SystemClock.getInstance();
        this.mountInfoFile = mount;
        initializeControllerPaths();
    }

    CGroupsHandlerImp(Configuration config, PrivilegedOperationExecutor privilegedOperationExecutor) throws ResourceHandleException {
        this(config, privilegedOperationExecutor, MOUNT_INFO);
    }

    private void initializeControllerPaths() throws ResourceHandleException {
        Map<String, Set<String>> newMount = null;
        Map<CGroupController, String> cPaths;
        try {
            newMount = parseMtab(mountInfoFile);
            cPaths = initializeControllerPathsFromMount(newMount);
        } catch (IOException e) {
            log.warn("Failed to init controller paths: " + e);
            throw new ResourceHandleException("Failed to init controller paths");
        }
        rwLock.writeLock().lock();
        try {
            controllerPaths = cPaths;
            parsedMount = newMount;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void mountCGroupController(CGroupController controller) {
        String existingMountPath = getControllerPath(controller);
        String requestedMountPath = new File("/sys/fs/cgroup", controller.getName()).getAbsolutePath();
        log.info("cgroup controller already mounted at: {} {}", existingMountPath, requestedMountPath);
    }

    @Override
    public void initializeCGroupController(CGroupController controller) throws ResourceHandleException {
        initializePreMountedCGroupController(controller);
    }

    private void initializePreMountedCGroupController(CGroupController controller) throws ResourceHandleException {
        String controllerPath = getControllerPath(controller);
        if (controllerPath == null) {
            throw new ResourceHandleException(
                    String.format("Controller %s not mounted, need mount it with %s", controller.getName(), "/sys/fs/cgroup"));
        }

        File rootHierarchy = new File(controllerPath);
        File dolphinHierarchy = new File(rootHierarchy, cGroupPrefix);
        String subsystemName = controller.getName();
        log.info("Initializing mounted controller " + subsystemName + " " + "at " + dolphinHierarchy);

        if (!rootHierarchy.exists()) {
            throw new ResourceHandleException("Cgroups mount point does not exist");
        } else if (!dolphinHierarchy.exists()) {
            log.info("control group does not exist, now creating");
            try {
                if (!dolphinHierarchy.mkdir()) {
                    throw new ResourceHandleException("can't create dolphin cgroup");
                }
            } catch (SecurityException e) {
                throw new ResourceHandleException("No permission to create cgroup hierarchy", e);
            }
        }
    }

    @Override
    public String createCGroup(CGroupController controller, String cGroupId) throws ResourceHandleException {
        String path = getPathForCGroup(controller, cGroupId);
        log.debug("CreateCgroup: {}", path);
        if (!new File(path).mkdir()) {
            throw new ResourceHandleException("Failed to create cgroup at " + path);
        }
        return path;
    }

    @Override
    public void deleteCGroup(CGroupController controller, String cGroupId) throws ResourceHandleException {
        boolean isDelete = false;
        String path = getPathForCGroup(controller, cGroupId);
        log.debug("deleteCGroup: {}", path);

    }

    @Override
    public String getControllerPath(CGroupController controller) {
        rwLock.readLock().lock();
        try {
            return controllerPaths.get(controller);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public String getRelativePathForCGroup(String cGroupId) {
        return cGroupPrefix + "/" + cGroupId;
    }

    @Override
    public String getPathForCGroup(CGroupController controller, String cGroupId) {
        return getControllerPath(controller) + "/" + cGroupPrefix
                + "/" + cGroupId;
    }

    @Override
    public String getPathForCGroupTasks(CGroupController controller, String cGroupId) {
        return getPathForCGroup(controller, cGroupId)
                + "/" + CGROUP_PROCS_FILE;
    }

    @Override
    public String getPathForCGroupParam(CGroupController controller, String cGroupId, String param) {
        return getPathForCGroup(controller, cGroupId)
                + "/" + controller.getName()
                + "." + param;
    }

    @Override
    public void updateCGroupParam(CGroupController controller, String cGroupId, String param, String value) throws ResourceHandleException {
        String cGroupParamPath = getPathForCGroupParam(controller, cGroupId, param);
        PrintWriter pw = null;

        log.debug("updateCGroupParam for path: {} with value {}",
                cGroupParamPath, value);

        try {
            File file = new File(cGroupParamPath);
            Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            pw = new PrintWriter(w);
            pw.write(value);
        } catch (IOException e) {
            throw new ResourceHandleException(
                    String.format("Unable to write to %s with value: %s",
                            cGroupParamPath, value), e);
        } finally {
            if (pw != null) {
                boolean hasError = pw.checkError();
                pw.close();
                if (hasError) {
                    throw new ResourceHandleException(
                            String.format("PrintWriter unable to write to %s with value: %s",
                                    cGroupParamPath, value));
                }
                if (pw.checkError()) {
                    throw new ResourceHandleException(
                            String.format("Error while closing cgroup file %s",
                                    cGroupParamPath));
                }
            }
        }
    }

    @Override
    public String getCGroupParam(CGroupController controller, String cGroupId, String param) throws ResourceHandleException {
        String cGroupParamPath =
                param.equals(CGROUP_PROCS_FILE) ?
                        getPathForCGroup(controller, cGroupId)
                                + "/" + param :
                        getPathForCGroupParam(controller, cGroupId, param);

        try {
            byte[] contents = Files.readAllBytes(Paths.get(cGroupParamPath));
            return new String(contents, "UTF-8").trim();
        } catch (IOException e) {
            throw new ResourceHandleException(
                    "Unable to read from " + cGroupParamPath);
        }
    }

    static String findControllerInMount(String controller,
                                        Map<String, Set<String>> entries) {
        for (Map.Entry<String, Set<String>> e : entries.entrySet()) {
            if (e.getValue().contains(controller)) {
                if (new File(e.getKey()).canRead()) {
                    return e.getKey();
                } else {
                    log.warn(String.format(
                            "Skipping inaccessible cgroup mount point %s", e.getKey()));
                }
            }
        }

        return null;
    }

    static Map<CGroupController, String> initializeControllerPathsFromMount(
            Map<String, Set<String>> parsedMtab)
            throws ResourceHandleException {
        Map<CGroupController, String> ret = new HashMap<>();

        for (CGroupController controller : CGroupController.values()) {
            String subsystemName = controller.getName();
            String controllerPath = findControllerInMount(subsystemName, parsedMtab);

            if (controllerPath != null) {
                ret.put(controller, controllerPath);
            }
        }
        return ret;
    }

    private static final Pattern MTAB_FILE_FORMAT = Pattern.compile(
            "^[^\\s]+\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s[^\\s]+\\s[^\\s]+$");

    static Map<String, Set<String>> parseMtab(String mtab)
            throws IOException {
        Map<String, Set<String>> ret = new HashMap<>();
        BufferedReader in = null;
        Set<String> validCgroups =
                CGroupsHandler.CGroupController.getValidGroups();

        try {
            FileInputStream fis = new FileInputStream(new File(mtab));
            in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

            for (String str = in.readLine(); str != null;
                 str = in.readLine()) {
                Matcher m = MTAB_FILE_FORMAT.matcher(str);
                boolean mat = m.find();
                if (mat) {
                    String path = m.group(1);
                    String type = m.group(2);
                    String options = m.group(3);

                    if (type.equals(CGROUPS_FSTYPE)) {
                        Set<String> cgroupList =
                                new HashSet<>(Arrays.asList(options.split(",")));
                        // Collect the valid subsystem names
                        cgroupList.retainAll(validCgroups);
                        ret.put(path, cgroupList);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error while reading " + mtab, e);
        }
        return ret;
    }

    @Override
    public String toString() {
        return CGroupsHandlerImp.class.getName() + " { " +
                "mountInfo=" + mountInfoFile + '\'' +
                ", cGroupPrefix='" + cGroupPrefix + '\'' +
                ", deleteCGroupTimeout=" + deleteCGroupTimeout +
                ", deleteCGroupDelay=" + deleteCGroupDelay +
                '}';
    }
}
