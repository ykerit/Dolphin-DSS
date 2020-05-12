package DolphinMaster;

public class ApplicationPlacementContext {
    private String pool;
    private String parentPool;

    public ApplicationPlacementContext(String pool) {
        this(pool, null);
    }

    public ApplicationPlacementContext(String pool, String parentPool) {
        this.pool = pool;
        this.parentPool = parentPool;
    }

    public String getParentPool() {
        return parentPool;
    }

    public String getPool() {
        return pool;
    }

    public boolean hasParentPool() {
        return pool != null;
    }
}
