package io.github.lynbean.lynbot.cogs.openai.chatbox;

import static com.mongodb.client.model.Filters.eq;
import static io.github.lynbean.lynbot.Bot.getMongoManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import io.github.lynbean.lynbot.cogs.openai.chatbox.pojo.ChatBoxPreset;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class ChatBoxDatabaseManager {
    private static final String CHATBOX_PRESET_COLLECTION = "openai_chatbox_presets";
    private static final String DEFAULT_PRESET_ID = "default";

    public static MongoCollection<ChatBoxPreset> getChatBoxPresetCollection() {
        return getMongoManager().getCollection(CHATBOX_PRESET_COLLECTION, ChatBoxPreset.class);
    }

    public @Nullable static ChatBoxPreset findChatBoxPreset(String id) {
        return getChatBoxPresetCollection()
            .find(eq("_id", id))
            .first();
    }

    private static ChatBoxPreset getHardCodedChatBoxPreset() {
        String title = "Optimized AI";

        String description = "This AI is optimized for general conversation. " +
            "It will give longer, more detailed responses.";

        String personality = "You are an AI who is having a conversation with a human. " +
            "The human is trying to ask you about something. " +
            "You should respond to the human's prompts appropriately. " +
            "Try to have a conversation that is as natural as possible.";

        Map<String, String> dialogues = new HashMap<>();
        dialogues.put("Hello, who are you?", "I am an AI created by OpenAI. How can I help you today?");
        dialogues.put("What is your name?", "My name is Optimized AI.");

        return new ChatBoxPreset()
            .setId(DEFAULT_PRESET_ID)
            .setTitle(title)
            .setDescription(description)
            .setPersonality(personality)
            .setDialogues(dialogues);
    }

    public static ChatBoxPreset getDefaultChatBoxPreset() {
        ChatBoxPreset preset = findChatBoxPreset(DEFAULT_PRESET_ID);

        if (preset == null) {
            ChatBoxPreset defaultPreset = getHardCodedChatBoxPreset();
            insertChatBoxPreset(defaultPreset);
            return defaultPreset;
        }

        return preset;
    }

    public static void replaceDefaultChatBoxPreset(ChatBoxPreset preset) {
        replaceChatBoxPreset(preset, new ReplaceOptions().upsert(true));
    }

    public static void insertChatBoxPreset(ChatBoxPreset preset) {
        getChatBoxPresetCollection()
            .insertOne(preset);
    }

    public static void replaceChatBoxPreset(ChatBoxPreset preset) {
        getChatBoxPresetCollection()
            .replaceOne(
                eq("_id", preset.getId()),
                preset
            );
    }

    public static void replaceChatBoxPreset(ChatBoxPreset preset, ReplaceOptions options) {
        getChatBoxPresetCollection()
            .replaceOne(
                eq("_id", preset.getId()),
                preset,
                options
            );
    }

    public static void deleteChatBoxPreset(String id) {
        getChatBoxPresetCollection()
            .deleteOne(
                eq("_id", id)
            );
    }

    public static List<ChatBoxPreset> getPresets() {
        return getChatBoxPresetCollection()
            .find()
            .into(new ArrayList<>());
    }

    public static List<Choice> getPresetChoices() {
        List<Choice> options = getPresets()
            .stream()
            .filter(preset -> preset.getId() != null && preset.getTitle() != null) // Exclude invalid presets
            .map(preset -> new Choice(preset.getTitle(), preset.getId()))
            .collect(Collectors.toList());

        return options;
    }
}
