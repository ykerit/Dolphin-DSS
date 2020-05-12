package DolphinMaster.scheduler;

public class PoolStatistics {

    private long submitted;
    private long running;
    private long pending;
    private long completed;
    private long killed;
    private long failed;
    private long activeUsers;
    private long availableMemoryMB;
    private long allocatedMemoryMB;
    private long pendingMemoryMB;
    private long reservedMemoryMB;
    private long availableVCores;
    private long allocatedVCores;
    private long pendingVCores;
    private long reservedVCores;

    public static PoolStatistics newInstance(long submitted, long running,
                                             long pending, long completed, long killed, long failed, long activeUsers,
                                             long availableMemoryMB, long allocatedMemoryMB, long pendingMemoryMB,
                                             long reservedMemoryMB, long availableVCores, long allocatedVCores,
                                             long pendingVCores, long reservedVCores) {
        PoolStatistics statistics = new PoolStatistics();
        statistics.setNumAppsSubmitted(submitted);
        statistics.setNumAppsRunning(running);
        statistics.setNumAppsPending(pending);
        statistics.setNumAppsCompleted(completed);
        statistics.setNumAppsKilled(killed);
        statistics.setNumAppsFailed(failed);
        statistics.setNumActiveUsers(activeUsers);
        statistics.setAvailableMemoryMB(availableMemoryMB);
        statistics.setAllocatedMemoryMB(allocatedMemoryMB);
        statistics.setPendingMemoryMB(pendingMemoryMB);
        statistics.setReservedMemoryMB(reservedMemoryMB);
        statistics.setAvailableVCores(availableVCores);
        statistics.setAllocatedVCores(allocatedVCores);
        statistics.setPendingVCores(pendingVCores);
        statistics.setReservedVCores(reservedVCores);
        return statistics;
    }

    /**
     * Get the number of apps submitted
     *
     * @return the number of apps submitted
     */
    public long getNumAppsSubmitted() {
        return submitted;
    }

    /**
     * Set the number of apps submitted
     *
     * @param numAppsSubmitted
     *          the number of apps submitted
     */
    public void setNumAppsSubmitted(long numAppsSubmitted) {
        submitted = numAppsSubmitted;
    }

    /**
     * Get the number of running apps
     *
     * @return the number of running apps
     */
    public long getNumAppsRunning() {
        return running;
    }

    /**
     * Set the number of running apps
     *
     * @param numAppsRunning
     *          the number of running apps
     */
    public void setNumAppsRunning(long numAppsRunning) {
        running = numAppsRunning;
    }

    /**
     * Get the number of pending apps
     *
     * @return the number of pending apps
     */
    public long getNumAppsPending() {
        return pending;
    }

    /**
     * Set the number of pending apps
     *
     * @param numAppsPending
     *          the number of pending apps
     */
    public void setNumAppsPending(long numAppsPending) {
        pending = numAppsPending;
    }

    /**
     * Get the number of completed apps
     *
     * @return the number of completed apps
     */
    public long getNumAppsCompleted() {
        return completed;
    }

    /**
     * Set the number of completed apps
     *
     * @param numAppsCompleted
     *          the number of completed apps
     */
    public void setNumAppsCompleted(long numAppsCompleted) {
        completed = numAppsCompleted;
    }

    /**
     * Get the number of killed apps
     *
     * @return the number of killed apps
     */
    public long getNumAppsKilled() {
        return killed;
    }

    /**
     * Set the number of killed apps
     *
     * @param numAppsKilled
     *          the number of killed apps
     */
    public void setNumAppsKilled(long numAppsKilled) {
        killed = numAppsKilled;
    }

    /**
     * Get the number of failed apps
     *
     * @return the number of failed apps
     */
    public long getNumAppsFailed() {
        return failed;
    }

    /**
     * Set the number of failed apps
     *
     * @param numAppsFailed
     *          the number of failed apps
     */
    public void setNumAppsFailed(long numAppsFailed) {
        failed = numAppsFailed;
    }

    /**
     * Get the number of active users
     *
     * @return the number of active users
     */
    public long getNumActiveUsers() {
        return activeUsers;
    }

    /**
     * Set the number of active users
     *
     * @param numActiveUsers
     *          the number of active users
     */
    public void setNumActiveUsers(long numActiveUsers) {
        activeUsers = numActiveUsers;
    }

    /**
     * Get the available memory in MB
     *
     * @return the available memory
     */
    public long getAvailableMemoryMB() {
        return availableMemoryMB;
    }

    /**
     * Set the available memory in MB
     *
     * @param availableMemoryMB
     *          the available memory
     */
    public void setAvailableMemoryMB(long availableMemoryMB) {
        this.availableMemoryMB = allocatedMemoryMB;
    }

    /**
     * Get the allocated memory in MB
     *
     * @return the allocated memory
     */
    public long getAllocatedMemoryMB() {
        return allocatedMemoryMB;
    }

    /**
     * Set the allocated memory in MB
     *
     * @param allocatedMemoryMB
     *          the allocate memory
     */
    public void setAllocatedMemoryMB(long allocatedMemoryMB) {
        this.allocatedMemoryMB = allocatedMemoryMB;
    }

    /**
     * Get the pending memory in MB
     *
     * @return the pending memory
     */
    public long getPendingMemoryMB() {
        return  pendingMemoryMB;
    }

    /**
     * Set the pending memory in MB
     *
     * @param pendingMemoryMB
     *          the pending memory
     */
    public void setPendingMemoryMB(long pendingMemoryMB) {
        this.pendingMemoryMB = pendingMemoryMB;
    }

    /**
     * Get the reserved memory in MB
     *
     * @return the reserved memory
     */
    public long getReservedMemoryMB() {
        return reservedMemoryMB;
    }

    /**
     * Set the reserved memory in MB
     *
     * @param reservedMemoryMB
     *          the reserved memory
     */
    public void setReservedMemoryMB(long reservedMemoryMB) {
        this.reservedMemoryMB = reservedMemoryMB;
    }

    /**
     * Get the available vcores
     *
     * @return the available vcores
     */
    public long getAvailableVCores() {
        return availableVCores;
    }

    /**
     * Set the available vcores
     *
     * @param availableVCores
     *          the available vcores
     */
    public void setAvailableVCores(long availableVCores) {
        this.availableVCores = availableVCores;
    }

    /**
     * Get the allocated vcores
     *
     * @return the allocated vcores
     */
    public long getAllocatedVCores() {
        return allocatedVCores;
    }

    /**
     * Set the allocated vcores
     *
     * @param allocatedVCores
     *          the allocated vcores
     */
    public void setAllocatedVCores(long allocatedVCores) {
        this.allocatedVCores = allocatedVCores;
    }

    /**
     * Get the pending vcores
     *
     * @return the pending vcores
     */
    public long getPendingVCores() {
        return pendingVCores;
    }

    /**
     * Set the pending vcores
     *
     * @param pendingVCores
     *          the pending vcores
     */
    public void setPendingVCores(long pendingVCores) {
        this.pendingVCores = pendingVCores;
    }


    public void setReservedVCores(long reservedVCores) {
        this.reservedVCores = reservedVCores;
    }
}
