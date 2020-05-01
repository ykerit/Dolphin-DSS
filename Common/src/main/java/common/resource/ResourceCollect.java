package common.resource;

import common.util.SystemInfoLinux;

public class ResourceCollect {
    private final SystemInfoLinux sysInfo;

    protected ResourceCollect() {
        this(SystemInfoLinux.newInstance());
    }

    public ResourceCollect(SystemInfoLinux sysInfo) {
        this.sysInfo = sysInfo;
    }

    public long getMemorySize() {
        return sysInfo.getPhysicalMemorySize();
    }

    public long getAvailableMemorySize() {
        return sysInfo.getAvailablePhysicalMemorySize();
    }

    public int getNumProcessors() {
        return sysInfo.getNumProcessors();
    }

    public int getNumCores() {
        return sysInfo.getNumCores();
    }

    public long getCpuFrequency() {
        return sysInfo.getCpuFrequency();
    }

    public float getCpuUsagePercentage() {
        return sysInfo.getCpuUsagePercentage();
    }

    public float getNumVCoresUsed() {
        return sysInfo.getNumVCoresUsed();
    }

    public long getNetworkBytesRead() {
        return sysInfo.getNetworkBytesRead();
    }

    public long getNetworkBytesWritten() {
        return sysInfo.getNetworkBytesWritten();
    }

    public static ResourceCollect newInstance() {
        return new ResourceCollect();
    }
}
