package common.appwork;

import common.util.Tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Container {
    private ResourceLimit resourceLimit;
    private String hierarchy;
    private HashMap<String, String> initMap;

    public Container(String hierarchy, ResourceLimit resourceLimit) {
        this.resourceLimit = resourceLimit;
        this.hierarchy = hierarchy;
        this.initMap = new HashMap<>();
    }

    public void init() throws IOException {
        if (resourceLimit.getCpuSet() != null) {
            initMap.put("cpuset", "cpuset.cpus");
            String path = Tools.FindSubsystemPath("cpuset", this.hierarchy, true);
            BufferedWriter buff = new BufferedWriter(new FileWriter(path + "/cpuset.cpus"));
            buff.write(this.resourceLimit.getCpuSet());
            buff.close();
        }
        if (resourceLimit.getCpuShare() != null) {
            initMap.put("cpu", "cpu.shares");
            String path = Tools.FindSubsystemPath("cpu", this.hierarchy, true);
            BufferedWriter buff = new BufferedWriter(new FileWriter(path + "/cpu.shares"));
            buff.write(this.resourceLimit.getCpuShare());
            buff.close();
        }
        if (resourceLimit.getMemory() != null) {
            initMap.put("memory", "memory.limit_in_bytes");
            String path = Tools.FindSubsystemPath("memory", this.hierarchy, true);
            BufferedWriter buff = new BufferedWriter(new FileWriter(path + "/memory.limit_in_bytes"));
            buff.write(this.resourceLimit.getMemory());
            buff.close();
        }
    }

    public void apply(int pid) throws IOException {
        for (Map.Entry<String, String> entry : this.initMap.entrySet()) {
            String path = Tools.FindSubsystemPath(entry.getKey(), this.hierarchy, false);
            BufferedWriter buff = new BufferedWriter(new FileWriter(path + "/" + "task"));
            buff.write(Integer.toString(pid));
            buff.close();
        }
    }

    public void remove() {
        for (Map.Entry<String, String> entry : this.initMap.entrySet()) {
            String path = Tools.FindSubsystemPath(entry.getKey(), this.hierarchy, false);
            File file = new File(path);
            file.delete();
        }
    }
}