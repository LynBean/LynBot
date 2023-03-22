package com.kim.discordbot.cogs.gpt.image;

import com.kim.discordbot.cogs.gpt.common.BotCompletionBuilder;
import com.kim.discordbot.core.database.ConfigManager;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageRequest.CreateImageRequestBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class BotImageCompletion extends BotCompletionBuilder {
    private @Builder.Default int n = Integer.parseInt(ConfigManager.get("image-n"));
    private @Builder.Default String size = ConfigManager.get("image-size");

    private @Builder.Default List<String> imageUrls = new ArrayList<>();

    private CreateImageRequestBuilder requestBuilder() {
        if (!isValidArgs())
            throw new IllegalArgumentException("Invalid arguments");

        return CreateImageRequest.builder()
            .prompt(this.getPrompt())
            .n(n)
            .size(size)
            .user(this.getUser());
    }

    @Override
    public void buildRequest() {
        CreateImageRequest request = requestBuilder()
            .build();

        this.setThread(new Thread(() -> {
            this.getService()
                .createImage(request)
                .getData()
                .stream()
                .forEach(image -> imageUrls.add(image.getUrl()));

            this.getLatch().countDown();
        }));
    }

    @Override
    public void runRequest() {
        if (this.getThread() == null)
            buildRequest();

        this.getThread().start();
    }
}
