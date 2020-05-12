package common.struct;

import java.io.Serializable;
import java.util.Objects;

public class ApplicationId implements Serializable, Comparable<ApplicationId> {
    public static final String appIdStrPrefix = "application";
    private static final String APPLICATION_ID_PREFIX = appIdStrPrefix + "_";
    // such as "application_12930434004_12323949432"

    private long id;
    private long clusterTimeStamp;

    public ApplicationId(long clusterTimestamp, long id) {
        this.clusterTimeStamp = clusterTimestamp;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    protected void setId(long id) {
        this.id = id;
    }

    public long getClusterTimeStamp() {
        return clusterTimeStamp;
    }

    protected void setClusterTimeStamp(long clusterTimeStamp) {
        this.clusterTimeStamp = clusterTimeStamp;
    }

    @Override
    public int compareTo(ApplicationId o) {
        if (this.getClusterTimeStamp() - o.getClusterTimeStamp() == 0) {
            return (int)(this.getId() - o.getId());
        } else {
            return Long.compare(this.getClusterTimeStamp(), o.getClusterTimeStamp());
        }
    }

    @Override
    public String toString() {
        return APPLICATION_ID_PREFIX + getClusterTimeStamp() + "_" + getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationId that = (ApplicationId) o;
        return id == that.id &&
                clusterTimeStamp == that.clusterTimeStamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clusterTimeStamp);
    }
}
