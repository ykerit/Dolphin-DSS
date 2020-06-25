package api.mapreduce;

import common.struct.Pair;

import java.util.Iterator;

public class WrappedReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
        extends Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    public Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context
    getReducerContext(ReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> reduceContext) {
        return new Context(reduceContext);
    }

    public class Context
            extends Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context {

        protected ReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> reduceContext;

        public Context(ReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> reduceContext)
        {
            this.reduceContext = reduceContext;
        }

        @Override
        public KEYIN getCurrentKey() {
            return reduceContext.getCurrentKey();
        }

        @Override
        public VALUEIN getCurrentValue() {
            return reduceContext.getCurrentValue();
        }

        @Override
        public boolean nextKeyValue() {
            return reduceContext.nextKeyValue();
        }

        @Override
        public void write(KEYOUT key, VALUEOUT value) {
            reduceContext.write(key, value);
        }

        @Override
        public Iterator<Pair<KEYOUT, VALUEOUT>> iterator() {
            return reduceContext.iterator();
        }

        @Override
        public Iterable<VALUEIN> getValues() {
            return reduceContext.getValues();
        }

        @Override
        public boolean nextKey() {
            return reduceContext.nextKey();
        }
    }
}

