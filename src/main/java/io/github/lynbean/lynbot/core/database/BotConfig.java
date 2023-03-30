package io.github.lynbean.lynbot.core.database;

import io.github.lynbean.lynbot.util.Config;
import io.github.lynbean.lynbot.util.Util;

public class BotConfig extends Config {
    public BotConfig() {
        super(
            Util.validFile("bot.properties"),
            "/application.properties"
        );
    }
}
