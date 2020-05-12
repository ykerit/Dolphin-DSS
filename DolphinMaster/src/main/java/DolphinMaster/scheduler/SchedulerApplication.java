package DolphinMaster.scheduler;

import DolphinMaster.app.AppState;
import common.struct.Priority;

public class SchedulerApplication {
    private ResourcePool pool;
    private final String user;
    private volatile AppDescribe currentDescribe;
    private volatile Priority priority;

    public SchedulerApplication(ResourcePool pool, String user) {
        this(pool, user, null);
    }

    public SchedulerApplication(ResourcePool pool, String user, Priority priority) {
        this.pool = pool;
        this.user = user;
        this.priority = priority;
    }

    public ResourcePool getPool() {
        return pool;
    }

    public void setPool(ResourcePool pool) {
        this.pool = pool;
    }

    public String getUser() {
        return user;
    }

    public AppDescribe getCurrentDescribe() {
        return currentDescribe;
    }

    public void setCurrentDescribe(AppDescribe currentDescribe) {
        this.currentDescribe = currentDescribe;
    }

    public void stop(AppState appState) {

    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        if (currentDescribe != null) {

        }
    }
}
