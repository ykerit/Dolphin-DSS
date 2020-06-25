package api.mapreduce;

import common.struct.Pair;

import java.util.Iterator;

public abstract class TaskInputOutputContextImp<KEYIN, VALIN, KEYOUT, VALOUT>
        implements TaskInputOutputContext<KEYIN, VALIN, KEYOUT, VALOUT> {

    private RecordWriter<KEYOUT,VALOUT> output;

    public TaskInputOutputContextImp(RecordWriter<KEYOUT,VALOUT> out) {
        output = out;
    }

    @Override
    public abstract boolean nextKeyValue();

    @Override
    public abstract KEYIN getCurrentKey();

    @Override
    public abstract VALIN getCurrentValue();

    @Override
    public void write(KEYOUT key, VALOUT value) {
        output.write(key, value);
    }

    @Override
    public Iterator<Pair<KEYOUT, VALOUT>> iterator() {
        return output.iterator();
    }
}
