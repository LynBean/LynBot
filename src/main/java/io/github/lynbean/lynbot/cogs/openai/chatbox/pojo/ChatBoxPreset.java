package io.github.lynbean.lynbot.cogs.openai.chatbox.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import com.google.gson.Gson;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.utils.FileUpload;

@Accessors(chain = true)
@BsonDiscriminator(value = "ChatBoxPreset", key = "_cls")
@Data
@ToString
public class ChatBoxPreset {
    @BsonIgnore
    public static final String FILE_PREFIX = "ChatBox-Preset-";

    @BsonCreator
    public ChatBoxPreset() {}

    public static ChatBoxPreset fromJson(String json) {
        return new Gson().fromJson(json, ChatBoxPreset.class);
    }

    @BsonIgnore
    public List<ChatMessage> getPresetMessages() {
        List<ChatMessage> messages = new ArrayList<>();

        ChatMessage personality = new ChatMessage();
        personality.setRole(ChatMessageRole.SYSTEM.value());
        personality.setContent(
            "%s\n%s".formatted(
                this.characterName != null ? "You're %s.".formatted(this.characterName) : "",
                this.personality != null ? this.personality : ""
            )
        );
        messages.add(personality);

        if (dialogues != null) {
            dialogues.entrySet()
                .stream()
                .forEachOrdered(
                    entry -> {
                        ChatMessage assistant = new ChatMessage();
                        assistant.setContent(entry.getKey());
                        assistant.setRole(ChatMessageRole.ASSISTANT.value());
                        messages.add(assistant);

                        ChatMessage user = new ChatMessage();
                        user.setContent(entry.getValue());
                        user.setRole(ChatMessageRole.USER.value());
                        messages.add(user);
                    }
                );
        }

        return messages;
    }

    @BsonIgnore
    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public FileUpload getJsonFile(String id) {
        return FileUpload.fromData(getJson().getBytes(), "%s%s.json".formatted(FILE_PREFIX, id));
    }

    public ChatBoxPreset setDefault() {
        this.title = "Assistant";
        this.description = "A friendly assistant.";
        this.personality = "You are a friendly assistant.";
        return this;
    }

    @BsonId
    @BsonProperty("_id")
    private String id;

    @BsonProperty("guild_id")
    private String guildId;

    @BsonProperty("title")
    private String title;

    @BsonProperty("description")
    private String description;

    @BsonProperty("personality")
    private String personality;

    @BsonProperty("dialogues")
    private Map<String, String> dialogues;

    @BsonProperty("character_icon_url")
    private String characterIconUrl;

    @BsonProperty("character_name")
    private String characterName;

    @BsonProperty("greeting_message")
    private String greetingMessage;
}
