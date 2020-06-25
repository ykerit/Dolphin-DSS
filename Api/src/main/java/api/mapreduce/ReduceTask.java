package api.mapreduce;

import common.util.Tools;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ReduceTask implements Callable<ReduceContext> {

    private Map<String, List<Integer>> input;
    private Class<? extends Reducer> reduceClass;

    public ReduceTask(Map<String, List<Integer>> input, Class<? extends Reducer> cls) {
        this.input = input;
        reduceClass = cls;
    }

    @Override
    public ReduceContext call() throws Exception {
        return runReduce();
    }

    private <INKEY, INVAL, OUTKEY, OUTVAL> ReduceContext<INKEY, INVAL, OUTKEY, OUTVAL> runReduce() {
        Reducer<INKEY, INVAL, OUTKEY, OUTVAL> reducer =
                (Reducer<INKEY, INVAL, OUTKEY, OUTVAL>) Tools.newInstance(reduceClass);
        RecordWriter<OUTKEY, OUTVAL> output = new RecordWriter<>();
        ReduceContext<INKEY, INVAL, OUTKEY, OUTVAL> reduceContext =
                new ReduceContextImpl<INKEY, INVAL, OUTKEY, OUTVAL>(output, input);

        Reducer<INKEY, INVAL, OUTKEY, OUTVAL>.Context
                reducerContext = new WrappedReducer<INKEY, INVAL, OUTKEY, OUTVAL>().
                getReducerContext(reduceContext);

        reducer.run(reducerContext);
        return reduceContext;
    }
}
