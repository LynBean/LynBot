package io.github.lynbean.lynbot.cogs.openai.core;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.theokanning.openai.service.OpenAiService;

import io.github.lynbean.lynbot.Bot;
import lombok.ToString;

@ToString
public class OpenAiCore {
    protected static final OpenAiService OPEN_AI_SERVICE = new OpenAiService(
        Bot.getConfig().getString("openai.key"),
        Duration.ofSeconds(120)
    );

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());
}
