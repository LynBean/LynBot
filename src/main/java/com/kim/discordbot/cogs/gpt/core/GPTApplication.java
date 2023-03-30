package com.kim.discordbot.cogs.gpt.core;

import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.util.Config;
import com.theokanning.openai.service.OpenAiService;

public class GPTApplication {
    private static OpenAiService service;
    private static final Config config = new GPTConfig();

    public OpenAiService getService() {
        if (service == null)
            service = new OpenAiService(ConfigManager.get("openai-key"));

        return service;
    }

    public Config getConfig() {
        return config;
    }
}
