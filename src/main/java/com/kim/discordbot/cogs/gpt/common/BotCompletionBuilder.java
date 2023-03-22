package com.kim.discordbot.cogs.gpt.common;

import com.theokanning.openai.service.OpenAiService;
import java.util.concurrent.CountDownLatch;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class BotCompletionBuilder {
    private OpenAiService service;

    private String prompt;
    private String user;

    private Thread thread;
    private CountDownLatch latch;

    public BotCompletionBuilder() {
        this.service = null;
        this.prompt = null;
        this.user = null;
        this.latch = new CountDownLatch(1);
    }

    public void buildRequest() {}
    public void runRequest() {}

    public Boolean isValidArgs() {
        return !(
            this.service == null ||
            this.prompt == null ||
            this.user == null
        );
    }

    public Boolean isRunning() {
        if (thread == null)
            return false;

        return thread.isAlive();
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
