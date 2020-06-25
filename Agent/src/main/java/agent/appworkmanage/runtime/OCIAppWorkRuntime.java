package agent.appworkmanage.runtime;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.cgroups.CGroupsHandler;
import agent.appworkmanage.cgroups.ResourceHandlerPackage;
import common.Privileged.PrivilegedOperationExecutor;
import common.util.Shell;
import config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class OCIAppWorkRuntime implements AppWorkRuntime {
    private static final Logger LOG =
            LoggerFactory.getLogger(OCIAppWorkRuntime.class);

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9_.-]+$");
    static final Pattern USER_MOUNT_PATTERN = Pattern.compile(
            "(?<=^|,)([^:\\x00]+):([^:\\x00]+)" +
                    "(:(r[ow]|(r[ow][+])?(r?shared|r?slave|r?private)))?(?:,|$)");
    static final Pattern TMPFS_MOUNT_PATTERN = Pattern.compile(
            "^/[^:\\x00]+$");
    static final String PORTS_MAPPING_PATTERN =
            "^:[0-9]+|^[0-9]+:[0-9]+|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]" +
                    "|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
                    ":[0-9]+:[0-9]+$";
    private static final int HOST_NAME_LENGTH = 64;

    public static final String RUNTIME_PREFIX = "YARN_CONTAINER_RUNTIME_%s_%s";
    public static final String CONTAINER_PID_NAMESPACE_SUFFIX =
            "CONTAINER_PID_NAMESPACE";
    public static final String RUN_PRIVILEGED_CONTAINER_SUFFIX =
            "RUN_PRIVILEGED_CONTAINER";


    abstract Set<String> getAllowedNetworks();

    abstract Set<String> getAllowedRuntimes();

    abstract boolean getHostPidNamespaceEnabled();

    abstract boolean getPrivilegedContainersEnabledOnCluster();

    abstract String getEnvOciContainerPidNamespace();

    abstract String getEnvOciContainerRunPrivilegedContainer();

    public OCIAppWorkRuntime(PrivilegedOperationExecutor
                                       privilegedOperationExecutor) {
        this(privilegedOperationExecutor, ResourceHandlerPackage
                .getCGroupsHandler());
    }

    public OCIAppWorkRuntime(PrivilegedOperationExecutor
                                       privilegedOperationExecutor, CGroupsHandler cGroupsHandler) {
    }

    public void initialize(Configuration conf, Context nmContext)
            throws AppWorkExecutionException {

    }

    public static boolean isOCICompliantContainerRequested(
            Configuration daemonConf, Map<String, String> env) {
        return false;
    }

    protected String mountReadOnlyPath(String mount,
                                       Map<Path, List<String>> localizedResources)
            throws AppWorkExecutionException {
        for (Map.Entry<Path, List<String>> resource :
                localizedResources.entrySet()) {
            if (resource.getValue().contains(mount)) {
                java.nio.file.Path path = Paths.get(resource.getKey().toString());
                if (!path.isAbsolute()) {
                    throw new AppWorkExecutionException("Mount must be absolute: " +
                            mount);
                }
                if (Files.isSymbolicLink(path)) {
                    throw new AppWorkExecutionException("Mount cannot be a symlink: " +
                            mount);
                }
                return path.toString();
            }
        }
        throw new AppWorkExecutionException("Mount must be a localized " +
                "resource: " + mount);
    }

    @Override
    public void prepareAppWork(AppWorkRuntimeContext ctx)
            throws AppWorkExecutionException {
    }

    protected String getUserIdInfo(String userName)
            throws AppWorkExecutionException {
        String id;
        Shell.ShellCommandExecutor shexec = new Shell.ShellCommandExecutor(
                new String[]{"id", "-u", userName});
        try {
            shexec.execute();
            id = shexec.getOutput().replaceAll("[^0-9]", "");
        } catch (Exception e) {
            throw new AppWorkExecutionException(e);
        }
        return id;
    }

    protected String[] getGroupIdInfo(String userName)
            throws AppWorkExecutionException {
        String[] id;
        Shell.ShellCommandExecutor shexec = new Shell.ShellCommandExecutor(
                new String[]{"id", "-G", userName});
        try {
            shexec.execute();
            id = shexec.getOutput().replace("\n", "").split(" ");
        } catch (Exception e) {
            throw new AppWorkExecutionException(e);
        }
        return id;
    }

    protected void validateContainerNetworkType(String network)
            throws AppWorkExecutionException {
        Set<String> allowedNetworks = getAllowedNetworks();
        if (allowedNetworks.contains(network)) {
            return;
        }

        String msg = "Disallowed network:  '" + network
                + "' specified. Allowed networks: are " + allowedNetworks
                .toString();
        throw new AppWorkExecutionException(msg);
    }

    protected void validateContainerRuntimeType(String runtime)
            throws AppWorkExecutionException {
        Set<String> allowedRuntimes = getAllowedRuntimes();
        if (runtime == null || runtime.isEmpty()
                || allowedRuntimes.contains(runtime)) {
            return;
        }

        String msg = "Disallowed runtime:  '" + runtime
                + "' specified. Allowed runtimes: are " + allowedRuntimes
                .toString();
        throw new AppWorkExecutionException(msg);
    }

    /**
     * Return whether the YARN container is allowed to run using the host's PID
     * namespace for the OCI-compliant container. For this to be allowed, the
     * submitting user must request the feature and the feature must be enabled
     * on the cluster.
     */
    protected boolean allowHostPidNamespace(AppWork appWork)
            throws AppWorkExecutionException {
        Map<String, String> environment = appWork.getAppWorkLaunchContext()
                .getEnvironment();
        String envOciContainerPidNamespace = getEnvOciContainerPidNamespace();

        String pidNamespace = environment.get(envOciContainerPidNamespace);

        if (pidNamespace == null) {
            return false;
        }

        if (!pidNamespace.equalsIgnoreCase("host")) {
            LOG.warn("NOT requesting PID namespace. Value of " +
                    envOciContainerPidNamespace
                    + "is invalid: " + pidNamespace);
            return false;
        }

        boolean hostPidNamespaceEnabled = getHostPidNamespaceEnabled();

        if (!hostPidNamespaceEnabled) {
            String message = "Host pid namespace being requested but this is not "
                    + "enabled on this cluster";
            LOG.warn(message);
            throw new AppWorkExecutionException(message);
        }

        return true;
    }


    protected static void validateHostname(String hostname) throws
            AppWorkExecutionException {
        if (hostname != null && !hostname.isEmpty()) {
            if (!HOSTNAME_PATTERN.matcher(hostname).matches()) {
                throw new AppWorkExecutionException("Hostname '" + hostname
                        + "' doesn't match OCI-compliant hostname pattern");
            }
            if (hostname.length() > HOST_NAME_LENGTH) {
                throw new AppWorkExecutionException(
                        "Hostname can not be greater than " + HOST_NAME_LENGTH
                                + " characters: " + hostname);
            }
        }
    }

    /**
     * Return whether the YARN container is allowed to run in a privileged
     * OCI-compliant container. For a privileged container to be allowed all of
     * the following three conditions must be satisfied:
     *
     * <ol>
     *   <li>Submitting user must request for a privileged container</li>
     *   <li>Privileged containers must be enabled on the cluster</li>
     *   <li>Submitting user must be white-listed to run a privileged
     *   container</li>
     * </ol>
     *      is requested but is not allowed
     */
    protected boolean allowPrivilegedContainerExecution(AppWork appWork)
            throws AppWorkExecutionException {

        if (!isContainerRequestedAsPrivileged(appWork)) {
            return false;
        }

        LOG.info("Privileged container requested for : " + appWork
                .getAppWorkId().toString());

        //Ok, so we have been asked to run a privileged container. Security
        // checks need to be run. Each violation is an error.

        //check if privileged containers are enabled.
        boolean privilegedContainersEnabledOnCluster =
                getPrivilegedContainersEnabledOnCluster();

        if (!privilegedContainersEnabledOnCluster) {
            String message = "Privileged container being requested but privileged "
                    + "containers are not enabled on this cluster";
            LOG.warn(message);
            throw new AppWorkExecutionException(message);
        }

        //check if submitting user is in the whitelist.

        LOG.info("All checks pass. Launching privileged container for : "
                + appWork.getAppWorkId().toString());

        return true;
    }

    /**
     * This function only returns whether a privileged container was requested,
     * not whether the container was or will be launched as privileged.
     *
     * @param container
     * @return true if container is requested as privileged
     */
    protected boolean isContainerRequestedAsPrivileged(
            AppWork container) {
        String envOciContainerRunPrivilegedContainer =
                getEnvOciContainerRunPrivilegedContainer();
        String runPrivilegedContainerEnvVar = container.getAppWorkLaunchContext()
                .getEnvironment().get(envOciContainerRunPrivilegedContainer);
        return Boolean.parseBoolean(runPrivilegedContainerEnvVar);
    }



    public static String formatOciEnvKey(String runtimeTypeUpper,
                                         String envKeySuffix) {
        return String.format(RUNTIME_PREFIX, runtimeTypeUpper, envKeySuffix);
    }
}
