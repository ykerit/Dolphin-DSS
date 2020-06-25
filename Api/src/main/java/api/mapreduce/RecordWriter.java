package api.mapreduce;

import common.struct.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordWriter<KEYOUT, VALOUT> {
    private List<Pair<KEYOUT, VALOUT>> data;

    public RecordWriter() {
        data = new ArrayList<>();
    }

    public void write(KEYOUT key, VALOUT val) {
        data.add(new Pair<>(key, val));
    }

    public Iterator<Pair<KEYOUT, VALOUT>> iterator() {
        return data.iterator();
    }
}
