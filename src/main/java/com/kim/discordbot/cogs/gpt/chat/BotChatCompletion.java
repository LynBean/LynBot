package com.kim.discordbot.cogs.gpt.chat;

import com.kim.discordbot.cogs.gpt.common.BotCompletionBuilder;
import com.kim.discordbot.core.database.ConfigManager;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.completion.chat.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper=true)
public class BotChatCompletion extends BotCompletionBuilder {
    private @Builder.Default String role = ConfigManager.get("chat-role");
    private @Builder.Default String model = ConfigManager.get("chat-model");
    private @Builder.Default Double frequencyPenalty = Double.parseDouble(ConfigManager.get("chat-frequency-penalty"));
    private @Builder.Default int maxTokens = Integer.parseInt(ConfigManager.get("chat-max-tokens"));
    private @Builder.Default Double presencePenalty = Double.parseDouble(ConfigManager.get("chat-presence-penalty"));
    private @Builder.Default Double temperature = Double.parseDouble(ConfigManager.get("chat-temperature"));
    private @Builder.Default Double topP = Double.parseDouble(ConfigManager.get("chat-top-p"));

    private @Builder.Default List<String> response = new ArrayList<>();

    private List<ChatMessage> getChatMessages() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(this.role);
        chatMessage.setContent(this.getPrompt());
        return List.of(chatMessage);
    }

    private ChatCompletionRequestBuilder requestBuilder() {
        if (!isValidArgs())
            throw new IllegalArgumentException("Invalid arguments");

        return ChatCompletionRequest.builder()
            .model(model)
            .frequencyPenalty(frequencyPenalty)
            .maxTokens(maxTokens)
            .presencePenalty(presencePenalty)
            .temperature(temperature)
            .topP(topP)
            .user(this.getUser())
            .stream(false) // Due to the library hasn't implemented the stream yet
            .messages(getChatMessages());
    }

    @Override
    public void buildRequest() {
        ChatCompletionRequest request = requestBuilder()
            .build();

        this.setThread(new Thread(() -> {
            this.getService()
                .createChatCompletion(request)
                .getChoices()
                .stream()
                .forEach(
                    (choice) -> this.response.add(
                        choice.getMessage()
                            .getContent()
                    )
                );

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
