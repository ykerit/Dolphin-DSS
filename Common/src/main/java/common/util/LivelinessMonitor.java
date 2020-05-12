package common.util;

import common.service.AbstractService;
import common.struct.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LivelinessMonitor<T> extends AbstractService {
    private static final Logger log = LogManager.getLogger(LivelinessMonitor.class.getName());
    // key is monitorID, value is pair that the first value is latest request time and second value is timeout times
    private final Map<T, Pair<Long, Integer>> running = new ConcurrentHashMap<>();
    public static final int DEFAULT_EXPIRE = 1000 * 60;
    private volatile boolean shutdown = false;
    private Thread checkerThread;
    private long monitorInterval;
    private long expireInterval = DEFAULT_EXPIRE;
    private long frequency;

    protected interface CallBack {
        void handle(long id);
    }

    public LivelinessMonitor(String name) {
        super(name);
    }

    @Override
    protected void serviceStart() throws Exception {
        this.checkerThread = new Thread(new Monitor());
        this.checkerThread.start();
        this.checkerThread.setName(getName());
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        this.shutdown = true;
        if (checkerThread != null) {
            checkerThread.interrupt();
        }
        super.serviceStop();
    }

    protected abstract void expire(T key);

    protected void setExpireInterval(long expireInterval) {
        this.expireInterval = expireInterval;
    }

    protected void setExpireFrequency(int frequency) {
        this.frequency = frequency;
    }

    protected void setMonitorInterval(long interval) {
        this.monitorInterval = interval;
    }

    private class Monitor implements Runnable {

        @Override
        public void run() {
            while (!shutdown && !Thread.currentThread().isInterrupted()) {
                // Loop for check node state;
                synchronized (LivelinessMonitor.class) {
                    log.info("the {}, current monitor size is: {}", getName(), running.size());
                    for (Map.Entry<T, Pair<Long, Integer>> agent : running.entrySet()) {
                        T monitor = agent.getKey();
                        Pair<Long, Integer> pair = agent.getValue();
                        long gap = System.currentTimeMillis() - pair.first;
                        if (gap < 0 || gap > expireInterval) {
                            log.info("Agent: {} Heartbeat is expire times = {}", monitor, pair.second);
                            pair.second += 1;
                            if (pair.second >= frequency) {
                                // agent outline
                                log.info("Agent: {} always dead, now remove it", monitor);
                                expire(monitor);
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(monitorInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void addMonitored(T monitor) {
        Pair<Long, Integer> pair = null;
        if ((pair = this.running.get(monitor)) != null) {
            pair.first = System.currentTimeMillis();
        } else
            this.running.put(monitor, new Pair<Long, Integer>(System.currentTimeMillis(), 0));
    }

    public synchronized void removeMonitored(T monitor) {
        this.running.remove(monitor);
    }

}
