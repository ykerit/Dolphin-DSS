package client;

import client.command.ClientCLI;
import com.beust.jcommander.JCommander;

public class App {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init();
        client.start();

        ClientCLI cli = new ClientCLI();
        JCommander commander = JCommander.newBuilder()
                .addObject(cli)
                .build();
        commander.parse(args);
        cli.run(commander, client);
    }
}