package common.event;

public abstract class AbstractEvent<Type extends Enum<Type>> implements Event<Type> {
    private Type type;
    private long timestamp;

    public AbstractEvent(Type type) {
        this.type = type;
        timestamp = -1L;
    }

    public AbstractEvent(Type type, long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EVENT_TYPE: " + getType();
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
}
