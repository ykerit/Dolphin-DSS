package DolphinMaster.scheduler;

import java.util.List;

public class PoolInfo {
    private String poolName;
    private float capacity;
    private float maximumCapacity;
    private float currentCapacity;
    private List<PoolInfo> childQueues;
    private PoolStatistics poolStatistics;
    private PoolState poolState;

    public PoolInfo() {
    }

    public PoolInfo(String poolName, float capacity,
                    float maximumCapacity, float currentCapacity,
                    List<PoolInfo> childQueues, PoolStatistics poolStatistics,
                    PoolState poolState) {
        this.poolName = poolName;
        this.capacity = capacity;
        this.maximumCapacity = maximumCapacity;
        this.currentCapacity = currentCapacity;
        this.childQueues = childQueues;
        this.poolStatistics = poolStatistics;
        this.poolState = poolState;
    }

    public PoolState getPoolState() {
        return poolState;
    }

    public void setPoolState(PoolState poolState) {
        this.poolState = poolState;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity(float capacity) {
        this.capacity = capacity;
    }

    public float getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(float maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public float getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(float currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public List<PoolInfo> getChildQueues() {
        return childQueues;
    }

    public void setChildQueues(List<PoolInfo> childQueues) {
        this.childQueues = childQueues;
    }

    public PoolStatistics getPoolStatistics() {
        return poolStatistics;
    }

    public void setPoolStatistics(PoolStatistics poolStatistics) {
        this.poolStatistics = poolStatistics;
    }
}
