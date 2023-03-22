package com.kim.discordbot.cogs.gpt.core;

import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.util.Config;
import com.theokanning.openai.service.OpenAiService;
import java.util.Properties;

public class GPTApplication {
    private static OpenAiService service;
    private static final Config config = new GPTConfig();

    public GPTApplication() {
        String key = ConfigManager.get("openai-key");
        service = new OpenAiService(key);
    }

    public GPTApplication(String token) {
        service = new OpenAiService(token);
    }

    public OpenAiService getService() {
        return service;
    }

    public Properties getPropertiesConfig() {
        return config.getProperties();
    }
}
