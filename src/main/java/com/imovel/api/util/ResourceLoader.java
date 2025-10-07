package com.imovel.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceLoader {
    private static final String DEFAULT_CONFIG_FILE = "endpoints.properties";
    
    private final Properties properties;

    public ResourceLoader() throws IOException {
        this(DEFAULT_CONFIG_FILE);
    }

    public ResourceLoader(String configFileName) throws IOException {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IOException("Unable to find properties file: " + configFileName);
            }
            properties.load(input);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Properties getAllProperties() {
        return new Properties(properties); // Return a copy for safety
    }
}
