package agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LogTest {
    @Test
    public void logTest() {
        Logger logger = LogManager.getLogger();
        logger.info("this is test {}", 1);
        logger.info("this is test ${}", 1);
    }
}
