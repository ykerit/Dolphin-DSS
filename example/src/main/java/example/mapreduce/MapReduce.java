package example.mapreduce;

import api.mapreduce.Mapper;
import api.mapreduce.Reducer;

import java.util.StringTokenizer;

public class MapReduce {
    public static class TokenizerMapper
            extends Mapper<Object, String, String, Integer> {

        private final static int one = 1;
        private String word = new String();

        public void map(Object key, String value, Context context) {
            StringTokenizer itr = new StringTokenizer(value.toString(), " ,?.!:\"\"''\n");
            while (itr.hasMoreTokens()) {
                word = itr.nextToken();
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<String, Integer, String, Integer> {

        private int result;

        public void reduce(String key, Iterable<Integer> values, Context context) {
            int sum = 0;
            for (int val : values) {
                sum += val;
            }
            result = sum;
            context.write(key, result);
        }
    }
}
