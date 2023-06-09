package io.github.lynbean.lynbot.cogs.openai.chatbox;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import io.github.lynbean.lynbot.cogs.openai.chatbox.pojo.ChatBoxPreset;
import io.reactivex.functions.BiConsumer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class ChatBox {
    private static final Logger LOG = LoggerFactory.getLogger(ChatBox.class);
    private static final String CHATBOX_PREFIX = "ChatBox";
    private static final String IGNORE_ANNOTATION = "> ` @IGNORE `";

    private @Getter final ThreadChannel channel;
    private @Getter ChatBoxPreset preset;

    public ChatBox(ThreadChannel channel) {
        this.channel = channel;

        try {
            retrievePresetFromChat();

        } catch (RuntimeException e) {
            LOG.error("Failed to retrieve preset from chat, will use default preset instead.", e);
            channel.sendMessage(getIgnoreAnnotatedMessage("Failed to retrieve preset, will use default preset instead."))
                .queue();
            this.preset = ChatBoxDatabaseManager.getDefaultChatBoxPreset();
        }
    }

    public ChatBox(ThreadChannel channel, String json) {
        this.channel = channel;
        this.preset = ChatBoxPreset.fromJson(json);
    }

    public ChatBox(ThreadChannel channel, ChatBoxPreset preset) {
        this.channel = channel;
        this.preset = preset;
    }

    public ChatBox(ThreadChannel channel, String title, String description, String personality, String characterIconUrl, String characterName) {
        this.channel = channel;
        this.preset = new ChatBoxPreset()
            .setTitle(title)
            .setDescription(description)
            .setPersonality(personality)
            .setCharacterIconUrl(characterIconUrl)
            .setCharacterName(characterName);
    }

    public static ChatBox create(Message message, User user) {
        return create(message, user, ChatBoxDatabaseManager.getDefaultChatBoxPreset());
    }

    public static ChatBox create(Message message, User user, String presetId) {
        ChatBoxPreset preset = ChatBoxDatabaseManager.findChatBoxPreset(String.valueOf(presetId));
        return create(message, user, preset);
    }

    public static ChatBox create(Message message, User user, ChatBoxPreset preset) {
        ThreadChannel channel = message.createThreadChannel(CHATBOX_PREFIX).complete();
        ChatBox chatBox = new ChatBox(channel, preset);
        chatBox.lockChannel();

        channel.addThreadMember(user).queue();
        channel.getManager().setSlowmode(5).queue(); // A way to prevent spamming and causing error occurred

        channel.sendMessage(
            new MessageCreateBuilder()
                .setContent(getIgnoreAnnotatedMessage("Do NOT delete this message. Created by " + user.getAsMention() + "."))
                .setActionRow(Button.danger("openai.chatbox.delete_channel", "If you hate me, click here to delete me."))
                .setFiles(preset.getJsonFile(message.getId()))
                .build()
        )
            .queue();

        chatBox.unLockChannel();
        return chatBox;
    }

    private void retrievePresetFromChat() {
        Optional<Message> chatPreset = channel.getHistoryFromBeginning(5)
            .complete()
            .getRetrievedHistory()
            .stream()
            .filter(
                message -> {
                    if (!message.getAttachments().isEmpty())
                        return message.getAttachments()
                            .get(0)
                            .getFileName()
                            .startsWith(CHATBOX_PREFIX);

                    return false;
                }
            )
            .findFirst();

        chatPreset.get()
            .getAttachments()
            .get(0)
            .getProxy()
            .download()
            .thenAccept(
                inputStream -> {
                    String json;
                    try (
                        BufferedInputStream bis = new BufferedInputStream(inputStream)
                    ) {
                        byte[] bytes = new byte[10240];
                        int bytesRead = bis.read(bytes, 0, bytes.length);
                        json = new String(bytes, 0, bytesRead);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    this.preset = ChatBoxPreset.fromJson(json);
                }
            )
            .join();
    }

    private List<Message> retrieveMessages() {
        return channel.getIterableHistory()
            // TODO: Integrate this into the preset message to let user configure this
            .takeAsync(15) // Prevents exceeding model's max token limit
            .thenApply(messages -> messages.stream().collect(Collectors.toList()))
            .join();
    }

    private List<ChatMessage> getDialogues() {
        if (preset == null) {
            retrievePresetFromChat();
        }

        List<ChatMessage> dialogues = preset.getPresetMessages();

        retrieveMessages()
            .stream()
            .filter(message ->
                !message.getContentRaw().startsWith(IGNORE_ANNOTATION) && // Exclude out not ChatBox messages
                !message.getAuthor().isSystem() && // Exclude out system messages
                !message.isEphemeral() && // Exclude out ephemeral messages
                (
                    // Exclude out messages without content or embeds
                    !message.getContentRaw().isEmpty() ||
                    !message.getEmbeds().isEmpty()
                )
            )
            .sorted((a, b) -> a.getTimeCreated().compareTo(b.getTimeCreated()))
            .forEachOrdered(
                message -> {
                    ChatMessage chatMessage = new ChatMessage();

                    try {
                        chatMessage.setContent(
                            message.getAuthor().isBot()
                                ? message.getEmbeds().get(0).getDescription() // Probably only one embed, it's hardcoded
                                : message.getContentRaw()
                        );
                        chatMessage.setRole(
                            message.getAuthor().isBot()
                                ? ChatMessageRole.ASSISTANT.value()
                                : ChatMessageRole.USER.value()
                        );
                        dialogues.add(chatMessage);

                    } catch (Exception e) {
                        // We will just ignore this message so that it won't break the chat
                        // But we will log it for debugging purposes
                        e.printStackTrace();
                    }
                }
            );

        return dialogues;
    }

    public void execute(BiConsumer<List<ChatMessage>, ChatBoxPreset> action) {
        lockChannel();
        try {
            action.accept(getDialogues(), preset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unLockChannel();
        }
    }

    public static String getIgnoreAnnotatedMessage(String message) {
        return String.format("%s (%s)", IGNORE_ANNOTATION, message);
    }

    private void lockChannel() {
        channel.getManager()
            .setLocked(true)
            .queue();
    }

    private void unLockChannel() {
        channel.getManager()
            .setLocked(false)
            .queue();
    }
}
