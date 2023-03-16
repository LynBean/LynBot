package com.kim.discordbot.util;

import com.kim.discordbot.Bot;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import org.slf4j.Logger;

public abstract class Config {
    private final Logger log;
    private final Properties properties = new Properties();
    private final File configFile;
    private final String resourceFile;

    public Config(File configFile) {
        this.log = BotLogger.getLogger(this.getClass());
        this.configFile = configFile;
        this.resourceFile = null;
        initialize();
    }

    public Config(File configFile, String resourceFile) {
        this.log = BotLogger.getLogger(this.getClass());
        this.configFile = configFile;
        this.resourceFile = resourceFile;
        initialize();
    }

    private void initialize() {
        if (configFile.exists() && !configFile.isDirectory() && configFile.length() > 0) {
            loadFile();
            return;
        }

        dumpFile(true);
        log.warn("{} has been generated in {}", configFile.getName(), getAbsolutePath());
        loadFromResource();
    }

    private void loadFile() {
        try {
            properties.load(Util.loadFile(getAbsolutePath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadFromResource() {
        try {
            properties.load(Util.loadResources(new Bot(), resourceFile));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void dumpFile() {
        // TODO: Implement this method
    }
    
    private void dumpFile(Boolean absence) {
        if (!absence) {
            dumpFile();
            return;
        }

        if (resourceFile == null) {
            log.error("Resource file is not specified.");
            return;
        }
        
        String content = Util.loadResourcesAsString(new Bot(), resourceFile);
        configFile.getParentFile().mkdirs();
        
        try {
            Files.writeString(configFile.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getAbsolutePath() {
        return configFile.getAbsolutePath();
    }
}
