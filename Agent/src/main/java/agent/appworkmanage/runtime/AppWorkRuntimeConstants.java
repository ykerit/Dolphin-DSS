package agent.appworkmanage.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.runtime.AppWorkRuntimeContext.Attribute;

public class AppWorkRuntimeConstants {
    public static final Attribute<Map> LOCALIZED_RESOURCES = Attribute
            .attribute(Map.class, "localized_resources");
    public static final Attribute<List> APP_WORK_LAUNCH_PREFIX_COMMANDS =
            Attribute.attribute(List.class, "app_work_launch_prefix_commands");
    public static final Attribute<String> RUN_AS_USER =
            Attribute.attribute(String.class, "run_as_user");
    public static final Attribute<String> USER = Attribute.attribute(String.class,
            "user");
    public static final Attribute<String> APPID =
            Attribute.attribute(String.class, "appid");
    public static final Attribute<String> APP_WORK_ID_STR = Attribute
            .attribute(String.class, "app_work_id_str");
    public static final Attribute<Path> APP_WORK_WORK_DIR = Attribute
            .attribute(Path.class, "app_work_work_dir");
    public static final Attribute<Path> PID_FILE_PATH = Attribute.attribute(
            Path.class, "pid_file_path");
    public static final Attribute<List> LOCAL_DIRS = Attribute.attribute(
            List.class, "local_dirs");
    public static final Attribute<List> LOG_DIRS = Attribute.attribute(
            List.class, "log_dirs");
    public static final Attribute<List> FILECACHE_DIRS = Attribute.attribute(
            List.class, "filecache_dirs");
    public static final Attribute<List> USER_LOCAL_DIRS = Attribute.attribute(
            List.class, "user_local_dirs");
    public static final Attribute<List> APP_WORK_LOCAL_DIRS = Attribute
            .attribute(List.class, "app_work_local_dirs");
    public static final Attribute<List> USER_FILECACHE_DIRS = Attribute
            .attribute(List.class, "user_filecache_dirs");
    public static final Attribute<List> APPLICATION_LOCAL_DIRS = Attribute
            .attribute(List.class, "application_local_dirs");
    public static final Attribute<List> APP_WORK_LOG_DIRS = Attribute.attribute(
            List.class, "app_work_log_dirs");
    public static final Attribute<String> RESOURCES_OPTIONS = Attribute.attribute(
            String.class, "resources_options");
    public static final Attribute<String> TC_COMMAND_FILE = Attribute.attribute(
            String.class, "tc_command_file");
    public static final Attribute<List> APP_WORK_RUN_CMDS = Attribute.attribute(
            List.class, "APP_WORK_run_cmds");
    public static final Attribute<String> CGROUP_RELATIVE_PATH = Attribute
            .attribute(String.class, "cgroup_relative_path");

    public static final Attribute<String> PID = Attribute.attribute(
            String.class, "pid");
    public static final Attribute<AppWorkExecutor.Signal> SIGNAL = Attribute
            .attribute(AppWorkExecutor.Signal.class, "signal");
    public static final Attribute<String> PROCFS = Attribute.attribute(
            String.class, "procfs");
}
