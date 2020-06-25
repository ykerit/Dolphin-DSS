package example.mapreduce;

import api.mapreduce.Job;
import api.mapreduce.Mapper;
import api.mapreduce.Reducer;
import client.Client;
import client.command.ClientCLI;
import com.beust.jcommander.JCommander;
import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;

public class MRClient {
    private static final Logger log = LogManager.getLogger(MRClient.class);

    private static final int DEFAULT_AM_MEMORY = 100;
    private static final int DEFAULT_AM_CORES = 1;
    private static final int DEFAULT_APP_WORK_MEMORY = 10;
    private static final int DEFAULT_APP_WORK_CORES = 1;

    private Client client;
    private String appName = "";
    private ApplicationId applicationId;
    private int amPriority = 0;
    private long amMemory = DEFAULT_AM_MEMORY;
    private long amCore = DEFAULT_AM_CORES;

    public MRClient() {
        client = new Client();
    }

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

    public static void main(String[] args) throws Exception {
        MRClient client = new MRClient();
        client.client.init();
        client.client.start();
        ClientCLI cli = new ClientCLI();
        JCommander commander = JCommander.newBuilder()
                .addObject(cli)
                .build();
        commander.parse(args);
        Job job = Job.getInstance();
        cli.run(commander, client.client, job, TokenizerMapper.class, IntSumReducer.class);
    }

}
