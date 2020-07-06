package api.mapreduce;

import common.util.Tools;

import java.util.concurrent.Callable;

public class MapTask implements Callable<MapContext> {
    private Class<? extends Mapper> mapClass;
    private Object data;

    public MapTask(Object input, Class<? extends Mapper> cls) {
        data = input;
        mapClass = cls;
    }

    @Override
    public MapContext call() throws Exception {
        return runMapper();
    }

    private <INKEY, INVAL, OUTKEY, OUTVAL> MapContext runMapper() {
        Mapper<INKEY, INVAL, OUTKEY, OUTVAL> mapper =
                (Mapper<INKEY, INVAL, OUTKEY, OUTVAL>) Tools.newInstance(mapClass);

        RecordReader<INKEY, INVAL> input = new RecordReader<>();
        input.initialize(data);
        RecordWriter<OUTKEY, OUTVAL> output = new RecordWriter<>();
        MapContext<INKEY, INVAL, OUTKEY, OUTVAL> mapContext = new MapContextImp(input, output);
        Mapper<INKEY, INVAL, OUTKEY, OUTVAL>.Context mapperContext =
                new WrappedMapper<INKEY, INVAL, OUTKEY, OUTVAL>().getMapContext(mapContext);
        mapper.run(mapperContext);
        System.out.println("MapTask completed 100%");
        return mapContext;
    }
}
