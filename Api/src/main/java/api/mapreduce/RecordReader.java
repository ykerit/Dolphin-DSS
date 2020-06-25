package api.mapreduce;

import common.struct.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordReader<KEYIN, VALIN> {
    List<Pair<KEYIN, VALIN>> kv;
    private Iterator<Pair<KEYIN, VALIN>> iterator;
    private Pair<KEYIN, VALIN> current;

    public RecordReader() {
        kv = new ArrayList<>();
    }

    public void initialize(Object data) {
        kv.add(new Pair<KEYIN, VALIN>(null, (VALIN) data));
        iterator = kv.iterator();
    }

    public boolean nextKeyVal() {
        boolean hasNext = iterator.hasNext();
        if (hasNext) {
            current = iterator.next();
        }
        return hasNext;
    }

    public KEYIN getCurrentKey() {
        return current.first;
    }

    public VALIN getCurrentValue() {
        return current.second;
    }
}
