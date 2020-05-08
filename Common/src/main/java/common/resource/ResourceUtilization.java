package common.resource;

import java.io.Serializable;

public class ResourceUtilization implements Comparable<ResourceUtilization>, Serializable {

    private int memory;
    private double cpu;

    private static ResourceUtilization instance = new ResourceUtilization();

    private ResourceUtilization() {}

    public static ResourceUtilization newInstance(int mem, double cpu) {
        instance.setCpu(cpu);
        instance.setMemory(mem);
        return instance;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public double getCpu() {
        return cpu;
    }

    @Override
    public int compareTo(ResourceUtilization o) {
        int diff = this.getMemory() - o.getMemory();
        if (diff == 0) {
            diff = Double.compare(this.getCpu(), o.getCpu());
        }
        return diff;
    }
}
