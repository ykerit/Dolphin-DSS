package api.mapreduce;

import common.struct.Pair;

import java.util.Iterator;

public class WrappedMapper<KEYIN, VALIN, KEYOUT, VALOUT> extends Mapper<KEYIN, VALIN, KEYOUT, VALOUT> {

    public Mapper<KEYIN, VALIN, KEYOUT, VALOUT>.Context getMapContext(MapContext<KEYIN, VALIN, KEYOUT, VALOUT> mapContext) {
        return new Context(mapContext);
    }

    public class Context extends Mapper<KEYIN, VALIN, KEYOUT, VALOUT>.Context {

        protected MapContext<KEYIN, VALIN, KEYOUT, VALOUT> mapContext;

        public Context(MapContext<KEYIN, VALIN, KEYOUT, VALOUT> mapContext) {
            this.mapContext = mapContext;
        }

        public InputSplit getInputSplit() {
            return mapContext.getInputSplit();
        }

        @Override
        public KEYIN getCurrentKey() {
            return mapContext.getCurrentKey();
        }

        @Override
        public VALIN getCurrentValue() {
            return mapContext.getCurrentValue();
        }

        @Override
        public boolean nextKeyValue() {
            return mapContext.nextKeyValue();
        }

        @Override
        public void write(KEYOUT key, VALOUT value) {
            mapContext.write(key, value);
        }

        @Override
        public Iterator<Pair<KEYOUT, VALOUT>> iterator() {
            return mapContext.iterator();
        }
    }
}
