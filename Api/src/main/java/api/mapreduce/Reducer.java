package api.mapreduce;

import java.io.IOException;

public class Reducer<KEYIN, VALIN, KEYOUT, VALOUT> {
    public abstract class Context
            implements ReduceContext<KEYIN, VALIN, KEYOUT, VALOUT> {
    }

    protected void setup(Context context) {
    }

    protected void reduce(KEYIN key, Iterable<VALIN> values, Context context) {
        for (VALIN value : values) {
            context.write((KEYOUT) key, (VALOUT) value);
        }
    }

    protected void cleanup(Context context) {

    }

    public void run(Context context) {
        setup(context);
        try {
            while (context.nextKey()) {
                reduce(context.getCurrentKey(), context.getValues(), context);
            }
        } finally {
            cleanup(context);
        }
    }
}
