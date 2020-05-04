package common.event;

import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class EventDispatcher extends AbstractService {
    private static final Logger log = LogManager.getLogger(EventDispatcher.class.getName());
    private final BlockingQueue<Event> eventQueue;
    private ConcurrentMap<Class<? extends Enum>, EventProcessor> eventProcessors;
    private volatile boolean shutdown = false;
    private volatile boolean strike = false;
    private volatile boolean emptyEventQueue = true;
    private final ForOutEventHandler forOutInstance = new ForOutEventHandler();
    private Thread processor;
    private final Object lock = new Object();

    public EventDispatcher(String name) {
        super(name);
        this.eventQueue = new LinkedBlockingQueue<>();
        this.eventProcessors = new ConcurrentHashMap<>();
    }

    public EventDispatcher() {
        super(EventDispatcher.class.getName());
        this.eventQueue = new LinkedBlockingQueue<>();
        this.eventProcessors = new ConcurrentHashMap<>();
    }

    public EventDispatcher(BlockingQueue<Event> queue) {
        super(EventDispatcher.class.getName());
        this.eventQueue = queue;
        this.eventProcessors = new ConcurrentHashMap<>();
    }

    @Override
    protected void serviceInit() throws Exception {
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        processor = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("EVENT DISPATCHER!!!");
                while (!shutdown && !Thread.currentThread().isInterrupted()) {
                    emptyEventQueue = eventQueue.isEmpty();
                    if (strike) {
                        synchronized (lock) {
                            if (emptyEventQueue) {
                                lock.notify();
                            }
                        }
                    }
                    Event event = null;
                    try {
                        event = eventQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (event != null) {
                        dispatch(event);
                    }
                }
            }
        });
        processor.start();
        processor.setName(getName());
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        // Wait for all processor to complete before exit
        this.strike = true;
        log.info("EVENT_DISPATCHER DON'T DISPATCH");
        synchronized (lock) {
            while (!emptyEventQueue && processor != null && processor.isAlive()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("WAITING FOR DISPATCH END...");
            }
        }
        this.shutdown = true;
        super.serviceStop();
    }

    public EventProcessor<Event> getEventProcessor() {
        return forOutInstance;
    }

    protected void dispatch(Event event) {
        log.info("DISPATCH EVENT NOW: {}", event.getClass().getName());
        Class<? extends Enum> type = event.getType().getDeclaringClass();
        EventProcessor<Event> processor = this.eventProcessors.get(type);
        if (processor == null) {
            log.info("NO PROCESSOR TO DEAL WHIT: {}", type);
        } else {
            processor.process(event);
        }
    }

    public void register(Class<? extends Enum> eventType, EventProcessor processor) {
        EventProcessor<Event> registeredProcessor = this.eventProcessors.get(eventType);
        if (registeredProcessor == null) {
            this.eventProcessors.put(eventType, processor);
        } else if (registeredProcessor instanceof PipelineEventProcessor){
            PipelineEventProcessor pipelineEventProcessor = (PipelineEventProcessor) registeredProcessor;
            pipelineEventProcessor.addProcessor(processor);
        } else {
            PipelineEventProcessor pipelineEventProcessor = new PipelineEventProcessor();
            pipelineEventProcessor.addProcessor(registeredProcessor);
            pipelineEventProcessor.addProcessor(processor);
            this.eventProcessors.put(eventType, processor);
        }
    }

    class ForOutEventHandler implements EventProcessor<Event> {
        @Override
        public void process(Event event) {
            if (strike) {
                return;
            }
            emptyEventQueue = false;
            log.info("Event Queue Size is: {}", eventQueue.size());
            try {
                eventQueue.put(event);
            } catch (InterruptedException e) {
                if (!shutdown) {
                    log.warn("EventDispatcher thread interrupted", e);
                }
                emptyEventQueue = eventQueue.isEmpty();
            }
        }
    }

    static class PipelineEventProcessor implements EventProcessor<Event> {
        List<EventProcessor<Event>> pipelineProcessor;

        public PipelineEventProcessor() {
            this.pipelineProcessor = new ArrayList<>();
        }

        @Override
        public void process(Event event) {
            for(EventProcessor<Event> processor : pipelineProcessor) {
                processor.process(event);
            }
        }

        public void addProcessor(EventProcessor<Event> processor) {
            pipelineProcessor.add(processor);
        }
    }

}
