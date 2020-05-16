package common.event;

import common.service.Service;

public interface EventProcessor<T extends Event> {
    void process(T event);
}
