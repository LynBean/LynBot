package io.github.lynbean.lynbot.cogs.gpt.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import io.github.lynbean.lynbot.cogs.gpt.common.BotCompletionBuilder;
import io.github.lynbean.lynbot.core.database.ConfigManager;
import io.reactivex.functions.Consumer;
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

    private Consumer<? super Throwable> onError = (Throwable::printStackTrace);
    private List<String> responses = new ArrayList<>();
    private int maxNumOfCharsPerResponse = 4096;
    private boolean isDone = false;
    private String finishReason;

    public BotChatCompletion(OpenAiService service, ExecutorService executor, String content, String userId) {
        super(service, executor);
        this.content = content;
        this.userId = userId;
    }

    public static String getContentWithHistory(String header, Map<String, String> chatHistory, String question) {
        StringBuilder content = new StringBuilder("%s\n\n".formatted(header));

        for (Map.Entry<String, String> entry : chatHistory.entrySet()) {
            content.append(
                String.format(
                    "Q: %s\nA: %s\n\n", entry.getKey(), entry.getValue()
                )
            );
        }

        content.append(String.format("Q: %s\nA:", question));
        return content.toString();
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

        service.streamChatCompletion(request)
            .doOnError(
                (Throwable throwable) -> {
                    onError.accept(throwable);
                    isDone = true;
                }
            )
            .doOnComplete(
                () -> isDone = true
            )
            .subscribe(
                (ChatCompletionChunk chunk) -> {
                    String buildString = chunk.getChoices().get(0).getMessage().getContent();

                    if (buildString == null) return;
                    if (responses.size() == 0) {
                        responses.add(buildString);
                        return;
                    }
                    if (responses.get(responses.size() - 1).concat(buildString).length() > maxNumOfCharsPerResponse) {
                        responses.add(buildString);
                        return;
                    }

                    responses.set(responses.size() - 1, responses.get(responses.size() - 1).concat(buildString));
                    setFinishReason(chunk.getChoices().get(0).getFinishReason());
                }
            );

        while (!isDone) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
