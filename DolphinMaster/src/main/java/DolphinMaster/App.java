package DolphinMaster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    private static Logger logger = LogManager.getLogger("test");
    public static void main(String[] args) {
        DolphinMaster dolphinMaster = new DolphinMaster();
        dolphinMaster.init();
        dolphinMaster.start();
    }
}
