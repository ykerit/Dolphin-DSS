package api.mapreduce;

public interface MapContext<KEYIN, VALIN, KEYOUT, VALOUT>
        extends TaskInputOutputContext<KEYIN, VALIN, KEYOUT, VALOUT> {

    public InputSplit getInputSplit();
}
