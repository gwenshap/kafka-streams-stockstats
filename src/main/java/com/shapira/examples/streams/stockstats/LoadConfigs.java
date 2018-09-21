package com.shapira.examples.streams.stockstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Load configuration from a file. This is mainly intended for connection info, so I can switch between clusters without recompile
 * But you can put other client configation here, but we may override it...
 */
public class LoadConfigs {

    // default to cloud, duh
    private static final String DEFAULT_CONFIG_File =
            System.getProperty("user.home") + File.separator + ".ccloud" + File.separator + "config";

    static Properties loadConfig() throws IOException {
        return loadConfig(DEFAULT_CONFIG_File);
    }

    static Properties loadConfig(String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new RuntimeException(configFile + " does not exist. You need a file with client configuration, " +
                    "either create one or run `ccloud init` if you are a Confluent Cloud user");
        }
        System.out.println("Loading configs from:" + configFile);
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }

        return cfg;
    }
}
