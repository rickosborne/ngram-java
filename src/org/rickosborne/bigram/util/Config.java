package org.rickosborne.bigram.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    Properties properties = new Properties();

    public Config(String filePath) {
        try {
            properties.load(new FileInputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int get(String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)), 10);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

}
