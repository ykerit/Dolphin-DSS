package api.mapreduce;

import common.struct.Pair;

import java.util.Iterator;

public interface TaskInputOutputContext<KEYIN, VALIN, KEYOUT, VALOUT> {

    public boolean nextKeyValue();

    public KEYIN getCurrentKey();

    public VALIN getCurrentValue();

    public void write(KEYOUT key, VALOUT value);

    public Iterator<Pair<KEYOUT, VALOUT>> iterator();
}
