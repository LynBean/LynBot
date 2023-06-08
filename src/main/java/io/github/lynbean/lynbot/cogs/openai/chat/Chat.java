package io.github.lynbean.lynbot.cogs.openai.chat;

import java.util.List;

import com.google.common.base.Optional;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.openai.core.OpenAiCore;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class Chat extends OpenAiCore {
    private @Getter @Setter Double frequencyPenalty;
    private @Getter @Setter Double presencePenalty;
    private @Getter @Setter Double temperature;
    private @Getter @Setter Double topP;
    private @Getter @Setter Integer maxTokens;
    private @Getter List<ChatMessage> messages;
    private @Getter @Setter String model;
    private @Getter @Setter String user;

    public static Chat fromString(String content) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setRole(ChatMessageRole.USER.value());
        return new Chat(message);
    }

    public Chat(ChatMessage message) {
        super();
        this.messages = List.of(message);
    }

    public Chat(List<ChatMessage> messages) {
        super();
        this.messages = messages;
    }

    private ChatCompletionRequest createCompletionRequest() {
        return ChatCompletionRequest.builder()
            .frequencyPenalty(
                Optional.fromNullable(frequencyPenalty)
                    .or(Bot.getConfig().getDouble("openai.chat.frequency_penalty"))
            )
            .maxTokens(
                Optional.fromNullable(maxTokens)
                    .or(Bot.getConfig().getInt("openai.chat.max_tokens"))
            )
            .messages(messages)
            .model(
                Optional.fromNullable(model)
                    .or(Bot.getConfig().getString("openai.chat.default_model"))
            )
            .presencePenalty(
                Optional.fromNullable(presencePenalty)
                    .or(Bot.getConfig().getDouble("openai.chat.presence_penalty"))
            )
            .topP(
                Optional.fromNullable(topP)
                    .or(Bot.getConfig().getDouble("openai.chat.top_p"))
            )
            .user(user)
            .build();
    }

    public ChatCompletionResult complete() {
        return OPEN_AI_SERVICE.createChatCompletion(createCompletionRequest());
    }

    public void complete(Consumer<ChatCompletionChunk> onNext, Consumer<Throwable> onError, Action onComplete) {
        OPEN_AI_SERVICE.streamChatCompletion(createCompletionRequest())
            .subscribe(
                chunk -> onNext.accept(chunk),
                error -> onError.accept(error),
                onComplete
            );
    }
}
