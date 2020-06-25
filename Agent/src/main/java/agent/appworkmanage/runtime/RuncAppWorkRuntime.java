/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package agent.appworkmanage.runtime;

import agent.Context;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.Localize.ResourceLocalizationService;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.cgroups.CGroupsHandler;
import agent.appworkmanage.cgroups.ResourceHandlerPackage;
import agent.appworkmanage.runtime.runc.ImageManifest;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig.OCILayer;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig.OCIRuntimeConfig;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig.OCIRuntimeConfig.OCIProcessConfig;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig.OCIRuntimeConfig.OCIMount;
import agent.appworkmanage.runtime.runc.RuncContainerExecutorConfig.OCIRuntimeConfig.OCILinuxConfig;
import agent.appworkmanage.runtime.runc.RuncImageTagToManifestPlugin;
import agent.appworkmanage.runtime.runc.RuncManifestToResourcesPlugin;
import common.Privileged.PrivilegedOperation;
import common.Privileged.PrivilegedOperationException;
import common.Privileged.PrivilegedOperationExecutor;
import common.resource.LocalResource;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.IOStreamPair;
import config.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static agent.appworkmanage.runtime.AppWorkRuntimeConstants.*;


public class RuncAppWorkRuntime extends OCIAppWorkRuntime {

  private static final Logger LOG = LogManager.getLogger(RuncAppWorkRuntime.class);

  private static final String RUNTIME_TYPE = "RUNC";

  public static final String ENV_RUNC_CONTAINER_IMAGE =
      "YARN_CONTAINER_RUNTIME_RUNC_IMAGE";
  public static final String ENV_RUNC_CONTAINER_MOUNTS =
      "YARN_CONTAINER_RUNTIME_RUNC_MOUNTS";
  public static final String ENV_RUNC_CONTAINER_HOSTNAME =
      "YARN_CONTAINER_RUNTIME_RUNC_CONTAINER_HOSTNAME";

  public final static String ENV_RUNC_CONTAINER_PID_NAMESPACE =
      formatOciEnvKey(RUNTIME_TYPE, CONTAINER_PID_NAMESPACE_SUFFIX);
  public final static String ENV_RUNC_CONTAINER_RUN_PRIVILEGED_CONTAINER =
      formatOciEnvKey(RUNTIME_TYPE, RUN_PRIVILEGED_CONTAINER_SUFFIX);

  private Configuration conf;
  private Context nmContext;
  private PrivilegedOperationExecutor privilegedOperationExecutor;
  private CGroupsHandler cGroupsHandler;
  private RuncImageTagToManifestPlugin imageTagToManifestPlugin;
  private RuncManifestToResourcesPlugin manifestToResourcesPlugin;
  private ObjectMapper mapper;
  private String seccomp;
  private int layersToKeep;
  private String defaultRuncImage;
  private ScheduledExecutorService exec;
  private String seccompProfile;
  private Set<String> defaultROMounts = new HashSet<>();
  private Set<String> defaultRWMounts = new HashSet<>();
  private Set<String> allowedNetworks = new HashSet<>();
  private Set<String> allowedRuntimes = new HashSet<>();

  public RuncAppWorkRuntime(PrivilegedOperationExecutor
      privilegedOperationExecutor) {
    this(privilegedOperationExecutor, ResourceHandlerPackage
        .getCGroupsHandler());
  }

  //A constructor with an injected cGroupsHandler primarily used for testing.
  public RuncAppWorkRuntime(PrivilegedOperationExecutor
      privilegedOperationExecutor, CGroupsHandler cGroupsHandler) {
    super(privilegedOperationExecutor, cGroupsHandler);
    this.privilegedOperationExecutor = privilegedOperationExecutor;

    if (cGroupsHandler == null) {
      LOG.info("cGroupsHandler is null - cgroups not in use.");
    } else {
      this.cGroupsHandler = cGroupsHandler;
    }
  }

  @Override
  public void initialize(Configuration configuration, Context nmCtx)
      throws AppWorkExecutionException {
    super.initialize(configuration, nmCtx);
//    this.conf = configuration;
//    this.nmContext = nmCtx;
//    imageTagToManifestPlugin = chooseImageTagToManifestPlugin();
//    imageTagToManifestPlugin.init(conf);
//    manifestToResourcesPlugin = chooseManifestToResourcesPlugin();
//    manifestToResourcesPlugin.init(conf);
//    mapper = new ObjectMapper();
//    defaultRuncImage = conf.get(YarnConfiguration.NM_RUNC_IMAGE_NAME);
//
//    allowedNetworks.clear();
//    allowedRuntimes.clear();
//
//    allowedNetworks.addAll(Arrays.asList(
//        conf.getTrimmedStrings(
//        YarnConfiguration.NM_RUNC_ALLOWED_CONTAINER_NETWORKS,
//        YarnConfiguration.DEFAULT_NM_RUNC_ALLOWED_CONTAINER_NETWORKS)));
//
//    allowedRuntimes.addAll(Arrays.asList(
//        conf.getTrimmedStrings(
//        YarnConfiguration.NM_RUNC_ALLOWED_CONTAINER_RUNTIMES,
//        YarnConfiguration.DEFAULT_NM_RUNC_ALLOWED_CONTAINER_RUNTIMES)));
//
//    privilegedContainersAcl = new AccessControlList(conf.getTrimmed(
//        YarnConfiguration.NM_RUNC_PRIVILEGED_CONTAINERS_ACL,
//        YarnConfiguration.DEFAULT_NM_RUNC_PRIVILEGED_CONTAINERS_ACL));
//
//    seccompProfile = conf.get(YarnConfiguration.NM_RUNC_SECCOMP_PROFILE);
//
//    defaultROMounts.addAll(Arrays.asList(
//        conf.getTrimmedStrings(
//        YarnConfiguration.NM_RUNC_DEFAULT_RO_MOUNTS)));
//
//    defaultRWMounts.addAll(Arrays.asList(
//        conf.getTrimmedStrings(
//        YarnConfiguration.NM_RUNC_DEFAULT_RW_MOUNTS)));
//
//    try {
//      //TODO Remove whitespace in seccomp that gets output to config.json
//      if (seccompProfile != null) {
//        seccomp = new String(Files.readAllBytes(Paths.get(seccompProfile)),
//            StandardCharsets.UTF_8);
//      }
//    } catch (IOException ioe) {
//      throw new ContainerExecutionException(ioe);
//    }
//
//    layersToKeep = conf.getInt(NM_RUNC_LAYER_MOUNTS_TO_KEEP,
//        DEFAULT_NM_RUNC_LAYER_MOUNTS_TO_KEEP);

  }

  @Override
  public void start() {
//    int reapRuncLayerMountsInterval =
//        conf.getInt(NM_REAP_RUNC_LAYER_MOUNTS_INTERVAL,
//        DEFAULT_NM_REAP_RUNC_LAYER_MOUNTS_INTERVAL);
//    exec = HadoopExecutors.newScheduledThreadPool(1);
//    exec.scheduleAtFixedRate(
//        new Runnable() {
//          @Override
//          public void run() {
//            try {
//              PrivilegedOperation launchOp = new PrivilegedOperation(
//                  PrivilegedOperation.OperationType.REAP_RUNC_LAYER_MOUNTS);
//              launchOp.appendArgs(Integer.toString(layersToKeep));
//              try {
//                String stdout = privilegedOperationExecutor
//                    .executePrivilegedOperation(null,
//                    launchOp, null, null, false, false);
//                if(stdout != null) {
//                  LOG.info("Reap layer mounts thread: " + stdout);
//                }
//              } catch (PrivilegedOperationException e) {
//                LOG.warn("Failed to reap old runc layer mounts", e);
//              }
//            } catch (Exception e) {
//              LOG.warn("Reap layer mount thread caught an exception: ", e);
//            }
//          }
//        }, 0, reapRuncLayerMountsInterval, TimeUnit.SECONDS);
//    imageTagToManifestPlugin.start();
//    manifestToResourcesPlugin.start();
  }

  @Override
  public void stop() {
    exec.shutdownNow();
//    imageTagToManifestPlugin.stop();
//    manifestToResourcesPlugin.stop();
  }

  @Override
  public void launchAppWork(AppWorkRuntimeContext ctx)
      throws AppWorkExecutionException {
    List<String> env = new ArrayList<>();
    AppWork container = ctx.getAppWork();
    String runAsUser = ctx.getExecutionAttribute(RUN_AS_USER);
    String user = ctx.getExecutionAttribute(USER);
    AppWorkId containerId = container.getAppWorkId();
    ApplicationId appId = containerId.getApplicationId();

    Map<String, String> environment = container.getAppWorkLaunchContext()
        .getEnvironment();
    List<OCIMount> mounts = new ArrayList<>();
    List<OCILayer> layers = new ArrayList<>();
    String hostname = environment.get(ENV_RUNC_CONTAINER_HOSTNAME);

    validateHostname(hostname);

    String containerIdStr = containerId.toString();
    String applicationId = appId.toString();
    Path containerWorkDir = ctx.getExecutionAttribute(APP_WORK_WORK_DIR);

//    RuncRuntimeObject runcRuntimeObject =
//        container.getContainerRuntimeData(RuncRuntimeObject.class);
//    List<LocalResource> layerResources = runcRuntimeObject.getOCILayers();
//
//    ResourceLocalizationService localizationService =
//        nmContext.getContainerManager().getResourceLocalizationService();

    List<String> args = new ArrayList<>();
//
//    try {
//      try {
//        LocalResource rsrc = runcRuntimeObject.getConfig();
//        LocalResourceRequest req = new LocalResourceRequest(rsrc);
//        LocalizedResource localRsrc = localizationService
//            .getLocalizedResource(req, user, appId);
//        if (localRsrc == null) {
//          throw new ContainerExecutionException("Could not successfully " +
//              "localize layers. rsrc: " + rsrc.getResource().getFile());
//        }
//
//        File file = new File(localRsrc.getLocalPath().toString());
//        List<String> imageEnv = extractImageEnv(file);
//        if (imageEnv != null && !imageEnv.isEmpty()) {
//          env.addAll(imageEnv);
//        }
//        List<String> entrypoint = extractImageEntrypoint(file);
//        if (entrypoint != null && !entrypoint.isEmpty()) {
//          args.addAll(entrypoint);
//        }
//      } catch (IOException ioe) {
//        throw new AppWorkExecutionException(ioe);
//      }
//
//      for (LocalResource rsrc : layerResources) {
//        LocalResourceRequest req = new LocalResourceRequest(rsrc);
//        LocalizedResource localRsrc = localizationService
//            .getLocalizedResource(req, user, appId);
//
//        OCILayer layer = new OCILayer("application/vnd.squashfs",
//            localRsrc.getLocalPath().toString());
//        layers.add(layer);
//      }
//    } catch (URISyntaxException e) {
//      throw new AppWorkExecutionException(e);
//    }
//
//    setAppWorkMounts(mounts, ctx, containerWorkDir, environment);
//
//    String resourcesOpts = ctx.getExecutionAttribute(RESOURCES_OPTIONS);
//
//    Path nmPrivateContainerScriptPath = ctx.getExecutionAttribute(
//        NM_PRIVATE_CONTAINER_SCRIPT_PATH);
//
//    Path nmPrivateTokensPath =
//        ctx.getExecutionAttribute(NM_PRIVATE_TOKENS_PATH);
//
//    int cpuShares = container.getResource().getVirtualCores();
//
//    // Zero sets to default of 1024.  2 is the minimum value otherwise
//    if (cpuShares < 2) {
//      cpuShares = 2;
//    }
//
//    Path launchDst =
//        new Path(containerWorkDir, ContainerLaunch.CONTAINER_SCRIPT);

//    args.add("bash");
//    args.add(launchDst.toUri().getPath());
//
//    String cgroupPath = getCgroupPath(resourcesOpts, "runc-" + containerIdStr);
//
//    String pidFile = ctx.getExecutionAttribute(PID_FILE_PATH).toString();
//
//    @SuppressWarnings("unchecked")
//    List<String> localDirs = ctx.getExecutionAttribute(LOCAL_DIRS);
//    @SuppressWarnings("unchecked")
//    List<String> logDirs = ctx.getExecutionAttribute(LOG_DIRS);
//    OCIProcessConfig processConfig = createOCIProcessConfig(
//        containerWorkDir.toString(), env, args);
//    OCILinuxConfig linuxConfig = createOCILinuxConfig(cpuShares,
//        cgroupPath, seccomp);
//
//    OCIRuntimeConfig ociRuntimeConfig = new OCIRuntimeConfig(null, mounts,
//        processConfig, hostname, null, null, linuxConfig);
//
//    RuncContainerExecutorConfig runcContainerExecutorConfig =
//        createRuncContainerExecutorConfig(runAsUser, user, containerIdStr,
//        applicationId, pidFile, nmPrivateContainerScriptPath.toString(),
//        nmPrivateTokensPath.toString(),
//        localDirs, logDirs, layers,
//        ociRuntimeConfig);
//
//    String commandFile = writeCommandToFile(
//        runcContainerExecutorConfig, container);
//    PrivilegedOperation launchOp = new PrivilegedOperation(
//        PrivilegedOperation.OperationType.RUN_RUNC_CONTAINER);
//
//    launchOp.appendArgs(commandFile);
//
//    try {
//      privilegedOperationExecutor.executePrivilegedOperation(null,
//          launchOp, null, null, false, false);
//    } catch (PrivilegedOperationException e) {
//      LOG.info("Launch container failed: ", e);
//      try {
//        LOG.debug("config.json used: " +
//            mapper.writeValueAsString(runcContainerExecutorConfig));
//      } catch (IOException ioe) {
//        LOG.info("Json Generation Exception", ioe);
//      }
//
//      throw new AppWorkExecutionException("Launch container failed", e
//          .getExitCode(), e.getOutput(), e.getErrorOutput());
//    }
  }

  @Override
  public void relaunchAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

  }

  private String getCgroupPath(String resourcesOptions, String containerIdStr) {
    if (cGroupsHandler == null) {
      LOG.debug("cGroupsHandler is null. cgroups are not in use. nothing to"
          + " do.");
      return null;
    }

    if (resourcesOptions.equals(
        (PrivilegedOperation.CGROUP_ARG_PREFIX + PrivilegedOperation
        .CGROUP_ARG_NO_TASKS))) {
      LOG.debug("no resource restrictions specified. not using runc's "
          + "cgroup options");
    } else {
      LOG.debug("using runc's cgroups options");

      String cGroupPath = "/" + cGroupsHandler.getRelativePathForCGroup(
          containerIdStr);

      LOG.debug("using cgroup parent: " + cGroupPath);

      return cGroupPath;
    }
    return null;
  }

  private void addUserMounts(List<OCIMount> mounts,
      Map<String, String> environment,
      Map<Path, List<String>> localizedResources)
      throws AppWorkExecutionException {
    if (environment.containsKey(ENV_RUNC_CONTAINER_MOUNTS)) {
      Matcher parsedMounts = USER_MOUNT_PATTERN.matcher(
          environment.get(ENV_RUNC_CONTAINER_MOUNTS));
      if (!parsedMounts.find()) {
        throw new AppWorkExecutionException(
            "Unable to parse user supplied mount list: "
            + environment.get(ENV_RUNC_CONTAINER_MOUNTS));
      }
      parsedMounts.reset();
      long mountCount = 0;
      while (parsedMounts.find()) {
        mountCount++;
        String src = parsedMounts.group(1);
        java.nio.file.Path srcPath = java.nio.file.Paths.get(src);
        if (!srcPath.isAbsolute()) {
          src = mountReadOnlyPath(src, localizedResources);
        }
        String dst = parsedMounts.group(2);
        String mode = parsedMounts.group(4);
        boolean isReadWrite;
        if (mode == null) {
          isReadWrite = true;
        } else if (mode.equals("rw")) {
          isReadWrite = true;
        } else if (mode.equals("ro")) {
          isReadWrite = false;
        } else {
          throw new AppWorkExecutionException(
              "Unable to parse mode of some mounts in user supplied "
              + "mount list: "
              + environment.get(ENV_RUNC_CONTAINER_MOUNTS));
        }
        addRuncMountLocation(mounts, src, dst, false, isReadWrite);
      }
      long commaCount = environment.get(ENV_RUNC_CONTAINER_MOUNTS).chars()
          .filter(c -> c == ',').count();
      if (mountCount != commaCount + 1) {
        // this means the matcher skipped an improperly formatted mount
        throw new AppWorkExecutionException(
            "Unable to parse some mounts in user supplied mount list: "
            + environment.get(ENV_RUNC_CONTAINER_MOUNTS));
      }
    }
  }

  private void addDefaultMountLocation(List<RuncContainerExecutorConfig.OCIRuntimeConfig.OCIMount> mounts,
      Set<String> defaultMounts, boolean createSource, boolean isReadWrite)
      throws AppWorkExecutionException {
    if(defaultMounts != null && !defaultMounts.isEmpty()) {
      for (String mount : defaultMounts) {
        String[] dir = StringUtils.split(mount, ':');
        if (dir.length != 2) {
          throw new AppWorkExecutionException("Invalid mount : " +
              mount);
        }
        String src = dir[0];
        String dst = dir[1];
        addRuncMountLocation(mounts, src, dst, createSource, isReadWrite);
      }
    }
  }

  private void addRuncMountLocation(List<OCIMount> mounts, String srcPath,
                                    String dstPath, boolean createSource, boolean isReadWrite) {
    if (!createSource) {
      boolean sourceExists = new File(srcPath).exists();
      if (!sourceExists) {
        return;
      }
    }

    ArrayList<String> options = new ArrayList<>();
    if (isReadWrite) {
      options.add("rw");
    } else {
      options.add("ro");
    }
    options.add("rbind");
    options.add("rprivate");
    mounts.add(new OCIMount(dstPath, "bind", srcPath, options));
  }

  private void addAllRuncMountLocations(List<OCIMount> mounts,
      List<String> paths, boolean createSource, boolean isReadWrite) {
    for (String dir: paths) {
      this.addRuncMountLocation(mounts, dir, dir, createSource, isReadWrite);
    }
  }

  public Map<String, LocalResource> getLocalResources(
      AppWork appWork) throws IOException {
    Map<String, LocalResource> containerLocalRsrc =
        appWork.getAppWorkLaunchContext().getLocalResource();
    long layerCount = 0;
    Map<String, String> environment =
        appWork.getAppWorkLaunchContext().getEnvironment();
    String imageName = environment.get(ENV_RUNC_CONTAINER_IMAGE);
    if (imageName == null || imageName.isEmpty()) {
      environment.put(ENV_RUNC_CONTAINER_IMAGE,
          defaultRuncImage);
      imageName = defaultRuncImage;
    }

    ImageManifest manifest =
        imageTagToManifestPlugin.getManifestFromImageTag(imageName);
    LocalResource config =
        manifestToResourcesPlugin.getConfigResource(manifest);
    List<LocalResource> layers =
        manifestToResourcesPlugin.getLayerResources(manifest);

    RuncRuntimeObject runcRuntimeObject =
        new RuncRuntimeObject(config, layers);
//    appWork.setContainerRuntimeData(runcRuntimeObject);

    for (LocalResource localRsrc : layers) {
      while(containerLocalRsrc.putIfAbsent("runc-layer" +
          Long.toString(layerCount), localRsrc) != null) {
        layerCount++;
      }
    }

    while(containerLocalRsrc.putIfAbsent("runc-config" +
      Long.toString(layerCount), config) != null) {
      layerCount++;
    }

    return containerLocalRsrc;
  }

  protected RuncImageTagToManifestPlugin chooseImageTagToManifestPlugin()
      throws AppWorkExecutionException {
    String pluginName = "";
    RuncImageTagToManifestPlugin runcImageTagToManifestPlugin;
    try {
      Class<?> clazz = Class.forName(pluginName);
      runcImageTagToManifestPlugin =
          (RuncImageTagToManifestPlugin) clazz.newInstance();
    } catch (Exception e) {
      throw new AppWorkExecutionException(e);
    }
    return runcImageTagToManifestPlugin;
  }

  protected RuncManifestToResourcesPlugin chooseManifestToResourcesPlugin()
      throws AppWorkExecutionException {
    String pluginName = "";
    LOG.info("pluginName = " + pluginName);
    RuncManifestToResourcesPlugin runcManifestToResourcesPlugin;
    try {
      Class<?> clazz = Class.forName(pluginName);
      runcManifestToResourcesPlugin =
          (RuncManifestToResourcesPlugin) clazz.newInstance();
    } catch (Exception e) {
      throw new AppWorkExecutionException(e);
    }
    return runcManifestToResourcesPlugin;
  }

  @SuppressWarnings("unchecked")
  protected List<String> extractImageEnv(File config) throws IOException {
    JsonNode node = mapper.readTree(config);
    JsonNode envNode = node.path("config").path("Env");
    if (envNode.isMissingNode()) {
      return null;
    }
    return mapper.readValue(envNode, List.class);
  }

  @SuppressWarnings("unchecked")
  protected List<String> extractImageEntrypoint(File config)
      throws IOException {
    JsonNode node = mapper.readTree(config);
    JsonNode entrypointNode = node.path("config").path("Entrypoint");
    if (entrypointNode.isMissingNode()) {
      return null;
    }
    return mapper.readValue(entrypointNode, List.class);
  }

  private RuncContainerExecutorConfig createRuncContainerExecutorConfig(
      String runAsUser, String username, String containerId,
      String applicationId, String pidFile,
      String containerScriptPath, String containerCredentialsPath,
      List<String> localDirs, List<String> logDirs,
      List<OCILayer> layers, OCIRuntimeConfig ociRuntimeConfig) {

    return new RuncContainerExecutorConfig(runAsUser, username, containerId,
        applicationId, pidFile, containerScriptPath, containerCredentialsPath,
        localDirs, logDirs, layers, layersToKeep, ociRuntimeConfig);
  }

  private OCIProcessConfig createOCIProcessConfig(String cwd,
                                                                   List<String> env, List<String> args) {
    return new OCIProcessConfig(false, null, cwd, env,
        args, null, null, null, false, 0, null, null);
  }

  private OCILinuxConfig createOCILinuxConfig(long cpuShares,
      String cgroupsPath, String seccompProf) {
    OCILinuxConfig.Resources.CPU cgroupCPU =
        new OCILinuxConfig.Resources.CPU(cpuShares, 0, 0, 0, 0,
        null, null);
    OCILinuxConfig.Resources cgroupResources =
        new OCILinuxConfig.Resources(null, null, cgroupCPU, null, null, null,
        null, null);

    return new OCILinuxConfig(null, null, null, null,
        cgroupsPath, cgroupResources, null, null, seccompProf, null, null,
        null, null);
  }

  private void setAppWorkMounts(ArrayList<OCIMount> mounts,
                                AppWorkRuntimeContext ctx, Path containerWorkDir,
                                Map<String, String> environment)
      throws AppWorkExecutionException {
    @SuppressWarnings("unchecked")
    List<String> filecacheDirs = ctx.getExecutionAttribute(FILECACHE_DIRS);
//    @SuppressWarnings("unchecked")
//    List<String> containerLogDirs = ctx.getExecutionAttribute(
//        CONTAINER_LOG_DIRS);
    @SuppressWarnings("unchecked")
    List<String> userFilecacheDirs =
        ctx.getExecutionAttribute(USER_FILECACHE_DIRS);
    @SuppressWarnings("unchecked")
    List<String> applicationLocalDirs =
        ctx.getExecutionAttribute(APPLICATION_LOCAL_DIRS);
    @SuppressWarnings("unchecked")
    Map<Path, List<String>> localizedResources = ctx.getExecutionAttribute(
        LOCALIZED_RESOURCES);

    addRuncMountLocation(mounts, containerWorkDir.toString() +
        "/private_slash_tmp", "/tmp", true, true);
    addRuncMountLocation(mounts, containerWorkDir.toString() +
        "/private_var_slash_tmp", "/var/tmp", true, true);

//    addAllRuncMountLocations(mounts, containerLogDirs, true, true);
    addAllRuncMountLocations(mounts, applicationLocalDirs, true, true);
    addAllRuncMountLocations(mounts, filecacheDirs, false, false);
    addAllRuncMountLocations(mounts, userFilecacheDirs, false, false);
    addDefaultMountLocation(mounts, defaultROMounts, false, false);
    addDefaultMountLocation(mounts, defaultRWMounts, false, true);
    addUserMounts(mounts, environment, localizedResources);
  }

  public String writeCommandToFile(
      RuncContainerExecutorConfig runcContainerExecutorConfig,
      AppWork container)
      throws AppWorkExecutionException {
    AppWorkId containerId = container.getAppWorkId();
    String filePrefix = containerId.toString();
    ApplicationId appId = containerId.getApplicationId();
    File commandFile;
    try {
      File cmdDir = null;

      String cmdDirStr = ResourceLocalizationService.DOLPHIN_HOME + File.separator +
                      appId + File.separator + filePrefix + File.separator;
      cmdDir = new File(cmdDirStr);
      if (!cmdDir.mkdirs() && !cmdDir.exists()) {
        throw new IOException("Cannot create container private directory "
                + cmdDir);
      }
      commandFile = new File(cmdDir + "/runc-config.json");
      try {
        mapper.writeValue(commandFile, runcContainerExecutorConfig);
      } catch (IOException ioe) {
        throw new AppWorkExecutionException(ioe);
      }

      return commandFile.getAbsolutePath();
    } catch (IOException e) {
      LOG.warn("Unable to write runc config.json to temporary file!");
      throw new AppWorkExecutionException(e);
    }
  }

  @Override
  public void signalAppWork(AppWorkRuntimeContext ctx)
      throws AppWorkExecutionException {
    AppWorkExecutor.Signal signal = ctx.getExecutionAttribute(SIGNAL);
    AppWork container = ctx.getAppWork();

    if (signal == AppWorkExecutor.Signal.KILL ||
        signal == AppWorkExecutor.Signal.TERM) {

    }

    PrivilegedOperation signalOp = new PrivilegedOperation(
        PrivilegedOperation.OperationType.SIGNAL_CONTAINER);

    signalOp.appendArgs(ctx.getExecutionAttribute(RUN_AS_USER),
        ctx.getExecutionAttribute(USER),
        Integer.toString(PrivilegedOperation.RunAsUserCommand
        .SIGNAL_CONTAINER.getValue()),
        ctx.getExecutionAttribute(PID),
        Integer.toString(signal.getValue()));

    //Some failures here are acceptable. Let the calling executor decide.
    signalOp.disableFailureLogging();

    try {
      PrivilegedOperationExecutor executor = PrivilegedOperationExecutor
          .getInstance(conf);

      executor.executePrivilegedOperation(null,
          signalOp, null, null, false, false);
    } catch (PrivilegedOperationException e) {
      //Don't log the failure here. Some kinds of signaling failures are
      // acceptable. Let the calling executor decide what to do.
      throw new AppWorkExecutionException("Signal container failed", e
          .getExitCode(), e.getOutput(), e.getErrorOutput());
    }
  }

  @Override
  public void reapAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {

  }

  @Override
  public IOStreamPair execAppWork(AppWorkRuntimeContext ctx) throws AppWorkExecutionException {
    return null;
  }

  static class RuncRuntimeObject {
    private final List<LocalResource> layers;
    private final LocalResource config;

    RuncRuntimeObject(LocalResource config,
        List<LocalResource> layers) {
      this.config = config;
      this.layers = layers;
    }

    public LocalResource getConfig() {
      return this.config;
    }

    public List<LocalResource> getOCILayers() {
      return this.layers;
    }
  }

  Set<String> getAllowedNetworks() {
    return allowedNetworks;
  }

  Set<String> getAllowedRuntimes() {
    return allowedRuntimes;
  }

  @Override
  boolean getHostPidNamespaceEnabled() {
    return false;
  }

  @Override
  boolean getPrivilegedContainersEnabledOnCluster() {
    return false;
  }

  String getEnvOciContainerPidNamespace() {
    return ENV_RUNC_CONTAINER_PID_NAMESPACE;
  }

  String getEnvOciContainerRunPrivilegedContainer() {
    return ENV_RUNC_CONTAINER_RUN_PRIVILEGED_CONTAINER;
  }
}
