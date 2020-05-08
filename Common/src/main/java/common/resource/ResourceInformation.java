package common.resource;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ResourceInformation implements Serializable {
    private String name;
    private String units;
    private long value;
    private long maxAllocation;
    private long minAllocation;
    private Set<String> tags = new HashSet<>();

    public static final String MEMORY_KEY = "memory-mb";
    public static final String VCORES_KEY = "vcores";

    public static final ResourceInformation MEMORY_MB = newInstance(MEMORY_KEY, "Mi");
    public static final ResourceInformation VCORES = newInstance(VCORES_KEY);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getMaxAllocation() {
        return maxAllocation;
    }

    public void setMaxAllocation(long maxAllocation) {
        this.maxAllocation = maxAllocation;
    }

    public long getMinAllocation() {
        return minAllocation;
    }

    public void setMinAllocation(long minAllocation) {
        this.minAllocation = minAllocation;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        if (tags != null) {
            this.tags = tags;
        }
    }

    public static ResourceInformation newInstance(String name) {
        return newInstance(name, "");
    }

    public static ResourceInformation newInstance(String name, String units) {
        return newInstance(name, units,
                0L, Long.MAX_VALUE,
                0L, ImmutableSet.of());
    }

    public static ResourceInformation newInstance(String name,
                                                  String units,
                                                  long value,
                                                  long maxAllocation,
                                                  long minAllocation,
                                                  Set<String> tags) {
        ResourceInformation ri = new ResourceInformation();
        ri.setName(name);
        ri.setUnits(units);
        ri.setValue(value);
        ri.setMaxAllocation(maxAllocation);
        ri.setMinAllocation(minAllocation);
        ri.setTags(tags);
        return ri;
    }
}
