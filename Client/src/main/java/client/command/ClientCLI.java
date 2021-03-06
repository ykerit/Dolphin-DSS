package client.command;

import api.mapreduce.Job;
import api.mapreduce.Mapper;
import api.mapreduce.Reducer;
import client.Client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import common.util.Tools;
import org.greatfree.exceptions.RemoteReadException;

import javax.tools.Tool;
import java.io.IOException;

public class ClientCLI {
    // run jar file
    @Parameter(names = "-type", description = "-type <jar or binary>")
    private String type;
    // run jar file need main method
    @Parameter(names = "-name", description = "-name <name> application name")
    private String mainClass;
    // file input path
    @Parameter(names = "-in", description = "-in <input> input file path")
    private String input;
    // file output path
    @Parameter(names = "-out", description = "-out <output> output file path")
    private String output;

    @Parameter(names = "-h", help = true)
    private boolean help;

    public void run(JCommander commander, final Client client) throws RemoteReadException, IOException, ClassNotFoundException {
        if (help) {
            commander.setProgramName("dolphin client");
            commander.usage();
            return;
        }
        System.out.println("type: " + type);
        System.out.println("name: " + mainClass);
        client.submitApplication(type, "jar", mainClass, 1);
    }

    public void run(JCommander commander, final Client client, Job job,
                    Class<? extends Mapper> mapper,
                    Class<? extends Reducer> reducer) throws Exception {
        if (help) {
            commander.setProgramName("dolphin client");
            commander.usage();
            return;
        }
        String home = System.getProperty("user.home");
        type = home + "/WordCount.jar";
        mainClass = "WordCount";
        input = home + "/case.txt";
        output = home + "/result.txt";
        System.out.println("type: " + type);
        System.out.println("name: " + mainClass);
        client.submitApplication(type, "jar", mainClass, 1);
        job.setMapperClass(mapper);
        job.setReducerClass(reducer);
        job.addFileInput(input);
        job.addFileOutput(output);
        job.waitForCompletion();
        client.stop();
    }
}
