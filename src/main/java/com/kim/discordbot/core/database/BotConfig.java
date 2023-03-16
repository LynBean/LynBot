package com.kim.discordbot.core.database;

import com.kim.discordbot.util.Config;
import com.kim.discordbot.util.Util;

public class BotConfig extends Config {
    public BotConfig() {
        super(
            Util.validFile("bot.properties"),
            "/application.properties"
        );
    }
}
