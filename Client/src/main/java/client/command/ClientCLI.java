package client.command;

import client.Client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import common.context.AppMasterSpec;
import common.resource.Resource;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class ClientCLI {
    // run jar file
    @Parameter(names = "-exe", description = "-e <exe> run exe file", required = true, validateWith = PositiveFile.class)
    private String exe;
    @Parameter(names = "-type", description = "-type <jar or binary>", required = true)
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
        Resource appMaster = new Resource();
        appMaster.cpuSet = "1";
        appMaster.cpuShare = "asdsa";
        appMaster.memory = "1asdj";

        client.submitApplication(exe, "yker",
                "boss",
                1,
                new AppMasterSpec(appMaster, "", null));

    }

}