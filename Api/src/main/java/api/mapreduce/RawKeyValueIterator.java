package api.mapreduce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RawKeyValueIterator<KEY, VAL> implements Iterable{
    private List<String> key;

    public RawKeyValueIterator() {
        key = new ArrayList<>();
    }

    @Override
    public Iterator iterator() {
        return null;
    }
}
