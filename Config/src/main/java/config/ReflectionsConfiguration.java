package config;

import java.util.*;

public class ReflectionsConfiguration {
    // reflections configuration properties
    private static final String SPILT = ",";
    private static final String REFLECTION = "reflection";
    private final Map<String, List<String>> configuration = new HashMap<>();
    public ReflectionsConfiguration() {
        readProperties();
    }

    private void readProperties() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(REFLECTION);
        Set<String> keys = resourceBundle.keySet();
        for (String key : keys) {
            String value = (String) resourceBundle.getObject(key);
            value = value.replaceAll(" ", "");
            List<String> list = Arrays.asList(value.split(SPILT));
            configuration.put(key, list);
        }
    }

    public Map<String, List<String>> getConfiguration() {
        return configuration;
    }
}
