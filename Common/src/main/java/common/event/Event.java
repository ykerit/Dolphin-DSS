package common.event;

public interface Event<Type extends Enum<Type>> {
    Type getType();
    long getTimestamp();
    String toString();
}
