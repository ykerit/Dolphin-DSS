package api.mapreduce;

import common.struct.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

public class Job {
    private static final Logger log = LogManager.getLogger(Job.class);

    private Class<? extends Mapper> mapperType;
    private Class<? extends Reducer> reducerType;
    private Class jarType;
    private Class<? extends Reducer> combinerType;
    private Class outputKeyType;
    private Class outputValType;

    private static final long BLOCK_SIZE = 1024 * 100; // 100k

    private TaskEngine taskEngine;
    private File inputFile;
    private File outputFile;

    private MapReducerHandler mapReducerHandler;
    private volatile boolean isCompleted = false;
    private List<MapContext> channel;

    public Job() {
        taskEngine = new TaskEngine();
        mapReducerHandler = new MapReducerHandler();
        channel = new ArrayList<>();
    }

    private static Job instance = new Job();

    public static Job getInstance() {
        return new Job();
    }

    public void setJarByClass(Class<?> cls) {
        jarType = cls;
    }

    public void setMapperClass(Class<? extends Mapper> cls) {
        mapperType = cls;
    }

    public void setCombinerClass(Class<? extends Reducer> cls) {
        combinerType = cls;
    }

    public void setReducerClass(Class<? extends Reducer> cls) {
        reducerType = cls;
    }

    public void setOutputKeyClass(Class<?> cls) {
        outputKeyType = cls;
    }

    public void setOutputValueClass(Class<?> cls) {
        outputValType = cls;
    }

    public void addFileInput(String filePath) {
        File file = new File(filePath);
        if (!file.exists() && file.isDirectory()) {
            log.error("input file is not exists");
            System.exit(-1);
        }
        inputFile = file;
    }

    public void addFileOutput(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputFile = file;
    }

    private List<String> splitFile(File file) throws InterruptedException, ExecutionException {
        long size = file.length();
        int count = (int) Math.ceil(file.length() / (double) BLOCK_SIZE);
        CompletionService ecs = new ExecutorCompletionService<String>(taskEngine.getTaskPool());
        for (int i = 0; i < count; ++i) {
            long startPos = (long) i * BLOCK_SIZE;
            ecs.submit(new SplitCallable(BLOCK_SIZE, startPos, file));
        }
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            String splitContext = (String) ecs.take().get();
            ret.add(splitContext);
        }
        return ret;
    }

    private class SplitCallable implements Callable<String> {
        long byteSize;
        String partFileName;
        File originFile;
        long startPos;

        public SplitCallable(long byteSize, long startPos,
                             File originFile) {
            this.startPos = startPos;
            this.byteSize = byteSize;
            this.partFileName = partFileName;
            this.originFile = originFile;
        }

        @Override
        public String call() throws Exception {
            RandomAccessFile rFile;
            OutputStream os;
            try {
                rFile = new RandomAccessFile(originFile, "r");
                byte[] b = new byte[(int) byteSize];
                rFile.seek(startPos);
                int s = rFile.read(b);
                return new String(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private class MapReducerHandler extends Thread {
        public MapReducerHandler() {
            super("mapperHandler");
        }

        @Override
        public void run() {
            List<String> fileContexts = null;
            try {
                fileContexts = splitFile(inputFile);
            } catch (InterruptedException | ExecutionException e) {
                log.error("split file error!");
            }
            int mapperSize = fileContexts.size();
            log.info("MapReduce Block size: {}", BLOCK_SIZE);
            log.info("MapReduce Split size: {}", mapperSize);
            CompletionService ecs = new ExecutorCompletionService<MapContext>(taskEngine.getTaskPool());
            for (int i = 0; i < mapperSize; ++i) {
                MapTask mapTask = new MapTask(fileContexts.get(i), mapperType);
                ecs.submit(mapTask);
            }
            for (int i = 0; i < mapperSize; ++i) {
                MapContext outContext;
                try {
                    outContext = (MapContext) ecs.take().get();
                    channel.add(outContext);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Map<String, List<Integer>> groupRes = new HashMap<>();
            group(groupRes);
            ReduceContext reduceContext = null;
            try {
                reduceContext = (ReduceContext) ecs.submit(new ReduceTask(groupRes, reducerType)).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Iterator iterator = reduceContext.iterator();
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputFile);
                while (iterator.hasNext()) {
                    Pair<String, Integer> kv = (Pair<String, Integer>) iterator.next();
                    String ct = kv.first + "=" + kv.second + "\n";
                    outputStream.write(ct.getBytes());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isCompleted = true;
        }
    }

    private void group(Map<String, List<Integer>> groups) {
        for (MapContext context : channel) {
            Iterator iter = context.iterator();
            pack(iter, groups);
        }
    }

    private void pack(Iterator iterator, Map<String, List<Integer>> groups) {
        while (iterator.hasNext()) {
            Pair kv = (Pair) iterator.next();
            if (groups.containsKey(kv.first)) {
                groups.get(kv.first).add((Integer) kv.second);
            } else {
                List<Integer> values = new ArrayList<>();
                values.add((Integer) kv.second);
                groups.put((String) kv.first, values);
            }
        }
    }

    private void submit() {
        mapReducerHandler.start();
    }

    public void waitForCompletion() {
        submit();
        while (!isCompleted) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        taskEngine.shutDown();
        log.info("MapReduce end");
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
