package common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Tools {
    private static final String MOUNT_INFO = "/proc/self/mountinfo";

    private static final long reactor = 100000000;

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
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));//构造一个BufferedReader类来读取文件
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
}
