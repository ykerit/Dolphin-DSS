package api.mapreduce;

import java.io.IOException;
import java.util.concurrent.Callable;

// 1. 首先文件会被拆分 split

// 2. mapper获得拆分后的数据进行处理

// 3. 进行shuffle sort

// 4. reduce 统计词频

public class Mapper<KEYIN, VALIN, KEYOUT, VALOUT> {

    public abstract class Context
            implements MapContext<KEYIN, VALIN, KEYOUT, VALOUT> {
    }

    protected void setup(Context context) {
    }

    protected void map(KEYIN key, VALIN value, Context context) {
    }

    protected void cleanup(Context context) {
    }

    public void run(Context context) {
        setup(context);
        try {
            while (context.nextKeyValue()) {
                map(context.getCurrentKey(), context.getCurrentValue(), context);
            }
        } finally {
            cleanup(context);
        }
    }
}
