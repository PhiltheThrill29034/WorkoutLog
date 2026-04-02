package com.gainengine.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    
    private static final Properties props = new Properties(); 

    static {
        try (FileInputStream in = new FileInputStream("config/config.properties")) {
            props.load(in); //this loads all the key-value pairs from the specified file.
        } catch (IOException e) {
            System.err.println("⚠️ Warning: config.properties not found. Using internal defaults.");
        }
    }

    public static String get(String key, String defVal){
        return props.getProperty(key,defVal);
    }
}
