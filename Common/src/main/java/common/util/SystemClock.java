package common.util;

public final class SystemClock {
    private static final SystemClock INSTANCE = new SystemClock();

    public static SystemClock getInstance() {
        return INSTANCE;
    }

    @Deprecated
    public SystemClock() {
        // do nothing
    }

    public long getTime() {
        return System.currentTimeMillis();
    }
}
