package api.mapreduce;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

public class FileSplitTest {
    private static final long BLOCK_SIZE = 1024 * 100; // 100k

    private TaskEngine taskEngine;

    public FileSplitTest() {
        taskEngine = new TaskEngine();
    }

    private void splitFile(File file) throws InterruptedException, ExecutionException {
        long size = file.length();
        int count = (int) Math.ceil(file.length() / (double) BLOCK_SIZE);
        System.out.println(count);
        CompletionService ecs = new ExecutorCompletionService<String>(taskEngine.getTaskPool());
        for (int i = 0; i < count; ++i) {
            long startPos = (long) i * BLOCK_SIZE;
            ecs.submit(new SplitCallable(BLOCK_SIZE, startPos, file));
        }

        for (int i = 0; i < count; ++i) {
            String splitContext = (String) ecs.take().get();
            if (splitContext != null) {

            }
        }
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

    @Test
    public void splitTest() {
        File file = new File("/Users/yuankai/Downloads/case.txt");
        try {
            splitFile(file);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
