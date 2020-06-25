package api.mapreduce;

import common.struct.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReduceContextImpl<KEYIN, VALIN, KEYOUT, VALOUT>
        extends TaskInputOutputContextImp<KEYIN, VALIN, KEYOUT, VALOUT>
        implements ReduceContext<KEYIN, VALIN, KEYOUT, VALOUT> {
    private Map<String, List<Integer>> data;
    Iterator<Map.Entry<String, List<Integer>>> iterator;
    Map.Entry<String, List<Integer>> current;
    public ReduceContextImpl(RecordWriter<KEYOUT, VALOUT> out, Map<String, List<Integer>> input) {
        super(out);
        this.data = input;
        iterator = data.entrySet().iterator();
    }

    @Override
    public boolean nextKey() {
        boolean hasNext = iterator.hasNext();
        if (hasNext) {
            current = iterator.next();
        }
        return hasNext;
    }

    @Override
    public Iterable getValues() {
        return current.getValue();
    }

    @Override
    public boolean nextKeyValue() {
        return false;
    }

    @Override
    public KEYIN getCurrentKey() {
        return (KEYIN) (current.getKey());
    }

    @Override
    public VALIN getCurrentValue() {
        return null;
    }
}
