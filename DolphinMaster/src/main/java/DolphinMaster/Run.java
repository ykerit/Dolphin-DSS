package DolphinMaster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Run {
    private static Logger logger = LogManager.getLogger("test");
    public static void main(String[] args) throws Exception {
        DolphinMaster dolphinMaster = new DolphinMaster();
        dolphinMaster.init();
        dolphinMaster.start();
    }
}
