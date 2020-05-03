package common.resource;

import java.io.Serializable;

import static common.resource.ResourceInformation.*;

public class Resource implements Comparable<Resource>, Serializable {
    protected ResourceInformation[] resources = null;

    private ResourceInformation memoryResInfo;
    private ResourceInformation vCoreResInfo;

    public int getMemory() {
        return castToInt(memoryResInfo.getValue());
    }

    public void setMemory(int memory) {
        this.memoryResInfo.setValue(memory);
    }

    public long getMemorySize() {
        return memoryResInfo.getValue();
    }

    public void setMemorySize(long memory) {
        memoryResInfo.setValue(memory);
    }

    public int getVCore() {
        return castToInt(vCoreResInfo.getValue());
    }

    public void setVCore(int vcore) {
        this.vCoreResInfo.setValue(vcore);
    }

    public static Resource newInstance(long mem, int v) {
       return new Resource(mem, v);
    }

    public Resource(long memory, int vCores) {
        initResourceInformation(memory, vCores);
    }

    void initResourceInformation(long memory, int vCores) {
        memoryResInfo = newDefaultInformation(MEMORY_KEY, MEMORY_MB.getUnits(), memory);
        vCoreResInfo = newDefaultInformation(VCORES_KEY, VCORES.getUnits(), vCores);
        resources = new ResourceInformation[2];
        resources[0] = memoryResInfo;
        resources[1] = vCoreResInfo;
    }

    ResourceInformation newDefaultInformation(String name, String units, long value) {
        ResourceInformation ri = new ResourceInformation();
        ri.setName(name);
        ri.setValue(value);
        ri.setUnits(units);
        ri.setMinAllocation(0);
        ri.setMaxAllocation(Long.MAX_VALUE);
        return ri;
    }

    @Override
    public int compareTo(Resource o) {
        long diff = this.getMemorySize() - o.getMemorySize();
        if (diff == 0) {
            return this.getVCore() - o.getVCore();
        } else if (diff > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    static int castToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Long.valueOf(value).intValue();
    }
}
