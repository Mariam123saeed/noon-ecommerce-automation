package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ConfigHandler {
    private static final Logger log = LogManager.getLogger(ConfigHandler.class);
    private Properties properties;

    public ConfigHandler(String filepath) {
        log.info(" Initializing ConfigLoader with file: {}", filepath);
        loadProperties(filepath);
    }

    private void loadProperties(String filepath) {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
            properties.load(fileInputStream);
            log.info("Properties loaded successfully from: {}", filepath);
        } catch (IOException e) {
            log.error("Failed to load properties from file: {}", filepath, e);
            throw new UncheckedIOException("Could not load config file: " + filepath, e);
        }
    }

    public String getProperty(String key) {
        log.debug(" Fetching property for key: {}", key);
        String value = properties.getProperty(key);
        if (value == null) {
            log.warn("Property {} not found", key);
        }
        return value;
    }

    public String[] getValue(String key) {
        log.debug(" Fetching array value for key: {}", key);
        String value = properties.getProperty(key);
        if (value != null) {
            return value.split(",", -1);
        }
        log.warn("Key {} not found, returning empty array", key);
        return new String[0];
    }
}