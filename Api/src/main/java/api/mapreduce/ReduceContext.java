package api.mapreduce;

import java.io.IOException;

public interface ReduceContext<KEYIN,VALIN,KEYOUT,VALOUT>
        extends TaskInputOutputContext<KEYIN,VALIN,KEYOUT,VALOUT> {

    public boolean nextKey();

    public Iterable<VALIN> getValues();
}