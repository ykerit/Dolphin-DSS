package agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    private static Logger logger = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) {
        Agent agent = new Agent();
        agent.init();
        agent.start();
    }
}
