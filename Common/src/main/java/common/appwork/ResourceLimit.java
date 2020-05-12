package common.appwork;

public class ResourceLimit {
    private String cpuShare;    // cpu time slice
    private String cpuSet;      // cpu core number
    private String memory;      // memory limit

    public String getCpuShare() {
        return cpuShare;
    }

    public void setCpuShare(String cpuShare) {
        this.cpuShare = cpuShare;
    }

    public String getCpuSet() {
        return cpuSet;
    }

    public void setCpuSet(String cpuSet) {
        this.cpuSet = cpuSet;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }
}
