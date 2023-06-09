package io.github.lynbean.lynbot.cogs.openai.chatbox;

import static com.mongodb.client.model.Filters.eq;
import static io.github.lynbean.lynbot.Bot.getMongoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import io.github.lynbean.lynbot.cogs.openai.chatbox.pojo.ChatBoxPreset;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class ChatBoxDatabaseManager {
    private static final String CHATBOX_PRESET_COLLECTION = "openai_chatbox_presets";

    public static MongoCollection<ChatBoxPreset> getChatBoxPresetCollection() {
        return getMongoManager().getCollection(CHATBOX_PRESET_COLLECTION, ChatBoxPreset.class);
    }

    public @Nullable static ChatBoxPreset findChatBoxPreset(String id) {
        return getChatBoxPresetCollection()
            .find(eq("_id", id))
            .first();
    }

    public static ChatBoxPreset getDefaultChatBoxPreset() {
        return new ChatBoxPreset().setDefault();
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
