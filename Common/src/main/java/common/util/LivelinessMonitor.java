package common.util;

import common.service.AbstractService;
import common.struct.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LivelinessMonitor extends AbstractService {
    private static final Logger log = LogManager.getLogger(LivelinessMonitor.class.getName());
    // key is monitorID, value is pair that the first value is latest request time and second value is timeout times
    private final Map<Long, Pair<Long, Integer>> agentMonitor = new ConcurrentHashMap<>();
    private volatile boolean shutdown = false;
    private Thread checkerThread;
    private final long monitorInterval;
    private final long timeout;
    private final long frequency;
    private final CallBack callBack;

    protected interface CallBack {
        void handle(long id);
    }

    public LivelinessMonitor(String name, long monitorInterval, long timeout, long frequency, CallBack callBack) {
        super(name);
        this.monitorInterval = monitorInterval;
        this.timeout = timeout;
        this.frequency = frequency;
        this.callBack = callBack;
    }

    @Override
    protected void serviceStart() {
        this.checkerThread = new Thread(new Monitor());
        this.checkerThread.start();
        this.checkerThread.setName(getName());
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        this.shutdown = true;
        super.serviceStop();
    }

    private class Monitor implements Runnable {

        @Override
        public void run() {
            while (!shutdown && !Thread.currentThread().isInterrupted()) {
                // Loop for check node state;
                synchronized (LivelinessMonitor.class) {
                    log.info("current monitor: {}", agentMonitor.size());
                    for(Map.Entry<Long, Pair<Long, Integer>> agent : agentMonitor.entrySet()) {
                        long monitorID = agent.getKey();
                        Pair<Long, Integer> pair = agent.getValue();
                        long gap = System.currentTimeMillis() - pair.first;
                        if (gap < 0 || gap > timeout) {
                            log.info("agentID: {} -- request timeout times={}", monitorID, pair.second);
                            pair.second += 1;
                            if (pair.second >= frequency) {
                                // agent outline
                                log.info("lose connect");
                                callBack.handle(monitorID);
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

    public synchronized void addMonitored(long monitorID) {
        Pair<Long, Integer> pair = null;
        if ((pair = this.agentMonitor.get(monitorID)) != null) {
            pair.first = System.currentTimeMillis();
        } else
            this.agentMonitor.put(monitorID, new Pair<Long, Integer>(System.currentTimeMillis(), 0));
    }

    public synchronized void removeMonitored(long monitorID) {
        this.agentMonitor.remove(monitorID);
    }

}
