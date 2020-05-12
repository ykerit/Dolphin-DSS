package common.resource;

import java.net.URL;

public class LocalResource {
    private URL resource;
    private LocalResourceType type;
    private long resourceSize;
    private long timestamp;

    public LocalResource(URL resource, LocalResourceType type, long resourceSize, long timestamp) {
        this.resource = resource;
        this.type = type;
        this.resourceSize = resourceSize;
        this.timestamp = timestamp;
    }

    public URL getResource() {
        return resource;
    }

    public LocalResourceType getType() {
        return type;
    }

    public long getResourceSize() {
        return resourceSize;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
