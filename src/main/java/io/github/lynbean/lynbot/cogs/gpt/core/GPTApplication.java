package io.github.lynbean.lynbot.cogs.gpt.core;

import java.time.Duration;

import com.theokanning.openai.service.OpenAiService;

import io.github.lynbean.lynbot.core.database.ConfigManager;
import io.github.lynbean.lynbot.util.Config;

public class GPTApplication {
    private static OpenAiService service;
    private static final Config config = new GPTConfig();

    public OpenAiService getService() {
        if (service == null)
            service = new OpenAiService(ConfigManager.get("openai-key"), Duration.ofSeconds(60));

        return service;
    }

    public Config getConfig() {
        return config;
    }
}
