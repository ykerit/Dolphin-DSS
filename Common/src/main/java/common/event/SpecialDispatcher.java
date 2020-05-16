package common.event;

import common.exception.DolphinRuntimeException;
import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SpecialDispatcher<T extends Event> extends AbstractService implements EventProcessor<T> {

    private final EventProcessor<T> processor;
    private final BlockingQueue<T> eventQueue =
            new LinkedBlockingDeque<>();
    private final Thread eventProcessor;
    private volatile boolean stopped = false;
    private boolean shouldExitOnError = true;

    private static final Logger LOG =
            LogManager.getLogger(SpecialDispatcher.class);

    private final class Processor implements Runnable {
        @Override
        public void run() {

            T event;

            while (!stopped && !Thread.currentThread().isInterrupted()) {
                try {
                    event = eventQueue.take();
                } catch (InterruptedException e) {
                    LOG.error("Returning, interrupted : " + e);
                    return; // TODO: Kill RM.
                }

                try {
                    processor.process(event);
                } catch (Throwable t) {
                    // An error occurred, but we are shutting down anyway.
                    // If it was an InterruptedException, the very act of
                    // shutdown could have caused it and is probably harmless.
                    if (stopped) {
                        LOG.warn("Exception during shutdown: ", t);
                        break;
                    }
                    LOG.error("Error in processing event type " + event.getType()
                            + " to the Event Dispatcher", t);
                    if (shouldExitOnError) {
                        LOG.info("Exiting, bbye..");
                        System.exit(-1);
                    }
                }
            }
        }
    }

    public SpecialDispatcher(EventProcessor<T> processor, String name) {
        super(name);
        this.processor = processor;
        this.eventProcessor = new Thread(new Processor());
        this.eventProcessor.setName(getName() + ":Event Processor");
    }

    @Override
    protected void serviceStart() throws Exception {
        this.eventProcessor.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        this.stopped = true;
        this.eventProcessor.interrupt();
        try {
            this.eventProcessor.join();
        } catch (InterruptedException e) {
            throw new DolphinRuntimeException(e);
        }
        super.serviceStop();
    }

    @Override
    public void process(T event) {
        try {
            int qSize = eventQueue.size();
            if (qSize !=0 && qSize %1000 == 0) {
                LOG.info("Size of " + getName() + " event-queue is " + qSize);
            }
            int remCapacity = eventQueue.remainingCapacity();
            if (remCapacity < 1000) {
                LOG.info("Very low remaining capacity on " + getName() + "" +
                        "event queue: " + remCapacity);
            }
            this.eventQueue.put(event);
        } catch (InterruptedException e) {
            LOG.info("Interrupted. Trying to exit gracefully.");
        }
    }

    public void disableExitOnError() {
        shouldExitOnError = false;
    }
}
