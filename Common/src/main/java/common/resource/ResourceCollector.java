package common.resource;

import common.util.SystemInfoLinux;

public class ResourceCollector {
    private final SystemInfoLinux sysInfo;

    public ResourceCollector() {
        this(SystemInfoLinux.newInstance());
    }

    public ResourceCollector(SystemInfoLinux sysInfo) {
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

    public static ResourceCollector newInstance() {
        return new ResourceCollector();
    }
}
