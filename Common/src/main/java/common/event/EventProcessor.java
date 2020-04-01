package common.event;

public interface EventProcessor<T extends Event> {
    void process(T event);
}
