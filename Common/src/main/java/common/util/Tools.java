package common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tools {
    private static final String MOUNT_INFO = "/proc/self/mountinfo";

    private static final long reactor = 100000000;

    private static Logger log = LogManager.getLogger(Tools.class);

    public static String GetCgroupPath(String subsystem) throws IOException {
        BufferedReader buffer = new BufferedReader(new FileReader(MOUNT_INFO));
        String line = null;
        while ((line = buffer.readLine()) != null) {
            String[] fields = line.split(" ");
            String[] last = fields[fields.length - 1].split(",");
            for (String opt : last) {
                if (opt.equals(subsystem)) {
                    buffer.close();
                    return fields[4];
                }
            }
        }
        buffer.close();
        return null;
    }

    public static String FindSubsystemPath(String subsystem, String cgroup, boolean autoCreate) {
        String rootPath = null;
        try {
            rootPath = GetCgroupPath(subsystem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String lastPath = rootPath + "/" + cgroup;
        File file = new File(lastPath);
        if (!file.exists() && autoCreate) {
            file.mkdir();
        }
        return lastPath;
    }

    public static String GenerateContainerID() {
        long prefix = (long) ((Math.random() + 1) * reactor);
        return System.currentTimeMillis() + String.valueOf(prefix).substring(1);
    }

    public static String file2String(String path) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String s = null;
            while ((s = br.readLine()) != null) {
                result.append(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String storeStateMachine(String name) {
        Path path = Paths.get(System.getProperty("user.dir"));
        return path.getParent().toString() + "/" + name;
    }

    public static String getProcessId(Path path) throws IOException {
        if (path == null) {
            throw new IOException("Trying to access process id from a pull path");
        }
        log.debug("Accessing pid from pid file {}", path);
        String processId = null;
        BufferedReader bufReader = null;

        try {
            File file = new File(path.toString());
            if (file.exists()) {
                FileInputStream fs = new FileInputStream(file);
                bufReader = new BufferedReader(new InputStreamReader(fs, "UTF-8"));

                while (true) {
                    String line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    String temp = line.trim();
                    if (!temp.isEmpty()) {
                        try {
                            long pid = Long.parseLong(temp);
                            if (pid > 0) {
                                processId = temp;
                                break;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        } finally {
            if (bufReader != null) {
                bufReader.close();
            }
        }
        log.debug("Got pid {} from path {}", (processId != null ? processId : "null"), path);
        return processId;
    }
}
