package api.mapreduce;

public class MapContextImp<KEYIN, VALIN, KEYOUT, VALOUT>
        extends TaskInputOutputContextImp<KEYIN, VALIN, KEYOUT, VALOUT>
        implements MapContext<KEYIN, VALIN, KEYOUT, VALOUT> {
    private RecordReader<KEYIN, VALIN> recordsReader;

    public MapContextImp(RecordReader<KEYIN, VALIN> reader,
                         RecordWriter<KEYOUT, VALOUT> writer) {
        super(writer);
        recordsReader = reader;
    }

    @Override
    public InputSplit getInputSplit() {
        return null;
    }

    @Override
    public boolean nextKeyValue() {
        return recordsReader.nextKeyVal();
    }

    @Override
    public KEYIN getCurrentKey() {
        return recordsReader.getCurrentKey();
    }

    @Override
    public VALIN getCurrentValue() {
        return recordsReader.getCurrentValue();
    }
}
