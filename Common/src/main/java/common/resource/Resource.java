package common.resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static common.resource.ResourceInformation.*;

public class Resource implements Comparable<Resource>, Serializable {
    protected ResourceInformation[] resources = null;

    private ResourceInformation memoryResInfo;
    private ResourceInformation vCoreResInfo;

    public static final int MEMORY_INDEX = 0;
    public static final int VCORES_INDEX = 1;

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

    public static Resource newInstance(Resource resource) {
        return new Resource(resource.getMemorySize(), resource.getVCore());
    }

    public Resource() {
    }

    public Resource(long memory, int vCores) {
        initResourceInformation(memory, vCores);
    }

    public ResourceInformation[] getResources() {
        return resources;
    }

    public void setResourceValue(int index, long value) {
        resources[index].setValue(value);
    }

    public void setResourceValue(String resource, long value) {
        if (resource.equals(MEMORY_KEY)) {
            this.setMemorySize(value);
            return;
        }
        if (resource.equals(VCORES_KEY)) {
            if (value > Integer.MAX_VALUE) {
                value = (long) Integer.MAX_VALUE;
            }
            this.setVCore((int)value);
            return;
        }

        ResourceInformation storedResourceInfo = getResourceInformation(resource);
        storedResourceInfo.setValue(value);
    }

    public ResourceInformation getResourceInformation(int index) {
        ResourceInformation ri = null;
        ri = resources[index];
        return ri;
    }

    private void initResourceInformation(long memory, int vCores) {
        memoryResInfo = newDefaultInformation(MEMORY_KEY, MEMORY_MB.getUnits(), memory);
        vCoreResInfo = newDefaultInformation(VCORES_KEY, VCORES.getUnits(), vCores);
        resources = new ResourceInformation[2];
        resources[MEMORY_INDEX] = memoryResInfo;
        resources[VCORES_INDEX] = vCoreResInfo;
    }

    private ResourceInformation newDefaultInformation(String name, String units, long value) {
        ResourceInformation ri = new ResourceInformation();
        ri.setName(name);
        ri.setValue(value);
        ri.setUnits(units);
        ri.setMinAllocation(0);
        ri.setMaxAllocation(Long.MAX_VALUE);
        return ri;
    }

    public long getResourceValue(String resource) {
        return getResourceInformation(resource).getValue();
    }

    public ResourceInformation getResourceInformation(String resource) {
        switch (resource) {
            case "memory":
                return resources[MEMORY_INDEX];
            case "vcores":
                return resources[VCORES_INDEX];
        }
        return null;
    }

    public void setResourceInformation(String resource,
                                       ResourceInformation resourceInformation) {
        if (resource.equals(MEMORY_KEY)) {
            this.setMemorySize(resourceInformation.getValue());
            return;
        }
        if (resource.equals(VCORES_KEY)) {
            this.setVCore((int) resourceInformation.getValue());
            return;
        }
        ResourceInformation storedResourceInfo = getResourceInformation(resource);
        ResourceInformation.copy(resourceInformation, storedResourceInfo);
    }

    public void setResourceInformation(int index, ResourceInformation resourceInformation) {
        ResourceInformation.copy(resourceInformation, resources[index]);
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

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<vCores: ").append(getVCore()).append(", memory: ").append(getMemorySize());
        for (int i = 2; i < resources.length; ++i) {
            ResourceInformation ri = resources[i];
            if (ri.getValue() == 0) {
                continue;
            }
            buffer.append(", ")
                    .append(ri.getName()).append(": ")
                    .append(ri.getValue())
                    .append(ri.getUnits());
        }
        buffer.append(">");
        return buffer.toString();
    }

    static int castToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Long.valueOf(value).intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Resource other = (Resource) o;
        ResourceInformation[] otherInfo = other.getResources();
        if (resources.length != otherInfo.length) {
            return false;
        }
        for (int i = 0; i < resources.length; i++) {
            ResourceInformation a = resources[i];
            ResourceInformation b = otherInfo[i];
            if ((a != b) && ((a == null) || !a.equals(b))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(memoryResInfo, vCoreResInfo);
        result = 31 * result + Arrays.hashCode(resources);
        return result;
    }
}
