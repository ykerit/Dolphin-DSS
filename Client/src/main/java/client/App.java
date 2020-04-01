package client;

import message.client_master_message.ApplicationIDResponse;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws RemoteReadException, IOException, ClassNotFoundException {
        Client client = new Client();
        client.init();
        client.start();

        ApplicationIDResponse applicationID = client.getApplicationID();
        System.out.println(applicationID.getID());
    }
}
