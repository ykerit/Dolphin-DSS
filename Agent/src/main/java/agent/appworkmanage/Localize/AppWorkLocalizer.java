package agent.appworkmanage.Localize;

import config.Configuration;

import java.util.Arrays;
import java.util.List;

public class AppWorkLocalizer {
    public static void buildMainArgs(List<String> command,
                                     String user,
                                     String appId,
                                     String locId,
                                     List<String> localDirs) {
        command.add(AppWorkLocalizer.class.getName());
        command.add(user);
        command.add(appId);
        command.add(locId);
        for (String dir : localDirs) {
            command.add(dir);
        }
    }

    public static List<String> getJavaOpts(Configuration configuration) {
        String opt = null;
        return Arrays.asList(opt.split(" "));
    }
}
