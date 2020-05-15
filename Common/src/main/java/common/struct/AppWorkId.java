package common.struct;

import java.io.Serializable;
import java.util.Objects;

public class AppWorkId implements Serializable, Comparable<AppWorkId> {
    private static final String APP_WORK_PREFIX = "AppWork_";
    private long appWorkId;
    private ApplicationId applicationId;

    public AppWorkId() {}

    public AppWorkId(ApplicationId applicationId, long appWorkId) {
        this.appWorkId = appWorkId;
        this.applicationId = applicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppWorkId appWorkId1 = (AppWorkId) o;
        return appWorkId == appWorkId1.appWorkId &&
                Objects.equals(applicationId, appWorkId1.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appWorkId, applicationId);
    }

    @Override
    public int compareTo(AppWorkId o) {
        int res = this.getApplicationId().compareTo(o.getApplicationId());
        if (res == 0) {
            return Long.compare(getAppWorkId(), o.getAppWorkId());
        } else {
            return res;
        }
    }

    @Override
    public String toString() {
        return APP_WORK_PREFIX + appWorkId;
    }

    public long getAppWorkId() {
        return appWorkId;
    }

    public void setAppWorkId(long id) {
        this.appWorkId = id;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }
}
