package io.github.lynbean.lynbot.cogs.gpt.core;

import io.github.lynbean.lynbot.util.Config;
import io.github.lynbean.lynbot.util.Util;

public class GPTConfig extends Config {
    public GPTConfig() {
        super(
            Util.validFile("gpt.properties", "gpt"),
            "/cogs/gpt.properties"
        );
    }
}
