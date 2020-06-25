package api.mapreduce;

import java.util.concurrent.*;

public class TaskEngine {
    private ExecutorService taskPool;
    private final static int MAX_THREAD_SIZE = 1000;

    public TaskEngine() {
        int core = Runtime.getRuntime().availableProcessors() * 2;
        taskPool = new ThreadPoolExecutor(core, 1000, 1, TimeUnit.SECONDS, new PriorityBlockingQueue<>());
    }

    public void submit(Callable task) {
        taskPool.submit(task);
    }

    public void shutDown() {
        if (!taskPool.isShutdown()) {
            taskPool.shutdown();
        }
        while (taskPool.isTerminated()) {
            break;
        }
    }

    public ExecutorService getTaskPool() {
        return taskPool;
    }
}
