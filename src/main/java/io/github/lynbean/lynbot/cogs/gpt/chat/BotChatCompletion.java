package io.github.lynbean.lynbot.cogs.gpt.chat;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.lynbean.lynbot.cogs.gpt.common.BotCompletionBuilder;
import io.github.lynbean.lynbot.core.database.ConfigManager;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class BotChatCompletion extends BotCompletionBuilder {
    private String content;
    private String userId;
    private String role = ConfigManager.get("chat-role");
    private String model = ConfigManager.get("chat-model");
    private double frequencyPenalty = Double.parseDouble(ConfigManager.get("chat-frequency-penalty"));
    private int maxTokens = Integer.parseInt(ConfigManager.get("chat-max-tokens"));
    private double presencePenalty = Double.parseDouble(ConfigManager.get("chat-presence-penalty"));
    private double temperature = Double.parseDouble(ConfigManager.get("chat-temperature"));
    private double topP = Double.parseDouble(ConfigManager.get("chat-top-p"));

    private List<String> response = new ArrayList<>();

    public BotChatCompletion(OpenAiService service, ExecutorService executor, String content, String userId) {
        super(service, executor);
        this.content = content;
        this.userId = userId;
    }

    private List<ChatMessage> getChatMessages() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(this.role);
        chatMessage.setContent(this.content);
        return List.of(chatMessage);
    }

    @Override
    protected void process() {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .frequencyPenalty(frequencyPenalty)
            .maxTokens(maxTokens)
            .presencePenalty(presencePenalty)
            .temperature(temperature)
            .topP(topP)
            .user(userId)
            .messages(getChatMessages())
            .build();

        service.createChatCompletion(request)
            .getChoices()
            .stream()
            .forEach(
                (choice) -> response.add(
                    choice.getMessage()
                        .getContent()
                )
            );
    }
}
