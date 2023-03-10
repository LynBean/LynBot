package com.kim.discordbot.core.database;

import com.kim.discordbot.Bot;
import com.kim.discordbot.util.BotLogger;
import com.kim.discordbot.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import org.slf4j.Logger;

public class BotConfig {
    private Logger log = BotLogger.getLogger(BotConfig.class);
    private final Properties properties = new Properties();

    private File actualFile = Util.validFile("bot.properties");

    public BotConfig getInstance() {
        return new BotConfig();
    }

    public Properties getProperties() {
        return properties;
    }

    public BotConfig() {
        if (actualFile.exists() && !actualFile.isDirectory() && actualFile.length() > 0) {
            load();
        } else {
            writeNewFile();
            System.out.printf(
                "Config has been generated in %s%n",
                Util.validFile("bot.properties").getAbsolutePath()
            );
            System.exit(1);
        }
    }

    private void load() {
        File file = Util.validFile("bot.properties");
        if (!file.exists()) {
            log.info("File not found.");
        }
        try {
            properties.load(Util.loadFile(file.toString()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadDefault() {
        try {
            log.info("Loading default properties.");
            properties.load(Util.loadResources(new Bot(), "reference.properties"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeNewFile() {
        String content = Util.loadResourcesAsString(new Bot(), "/reference.properties");
        File file = Util.validFile("bot.properties");
        file.getParentFile().mkdirs();
        if (file.exists()) {
            file.renameTo(Util.validFile("bot.properties.bak"));
        }

        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
