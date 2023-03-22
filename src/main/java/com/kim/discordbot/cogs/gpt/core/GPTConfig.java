package com.kim.discordbot.cogs.gpt.core;

import com.kim.discordbot.util.Config;
import com.kim.discordbot.util.Util;

public class GPTConfig extends Config {
    public GPTConfig() {
        super(
            Util.validFile("gpt.properties", "gpt"),
            "/cogs/gpt.properties"
        );
    }
}
