package io.github.lynbean.lynbot.cogs.guildmoderation;

import static com.mongodb.client.model.Filters.eq;
import static io.github.lynbean.lynbot.Bot.getMongoManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;

import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.GuildEventListenerManager;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.ServerModeration;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildApplicationCommandPermissionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildChannelEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEmojiEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildForumTagEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInteractionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInviteEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMemberEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMessageEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildPermissionOverrideEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildRoleEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildScheduledEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStageInstanceEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStickerEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildThreadEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildVoiceEvent;
import net.dv8tion.jda.api.entities.Guild;

public class GuildDatabaseManager {
    public static final String SERVER_MODERATION_COLLECTION = "server_moderations";

    private final Guild guild;
    private final MongoCollection<ServerModeration> collection;
    private final ServerModeration document;

    public GuildDatabaseManager(Guild guild) {
        this.guild = guild;
        this.collection = getAdminCollection();
        this.document = findServerModeration(guild);
    }

    public static MongoCollection<ServerModeration> getAdminCollection() {
        return getMongoManager().getCollection(SERVER_MODERATION_COLLECTION, ServerModeration.class);
    }

    public @Nullable static ServerModeration findServerModeration(String guildId) {
        return getAdminCollection().find(eq("_id", guildId)).first();
    }

    public @Nonnull static ServerModeration findServerModeration(Guild guild) {
        return findServerModeration(getAdminCollection(), guild);
    }

    private @Nonnull static ServerModeration findServerModeration(MongoCollection<ServerModeration> collection, Guild guild) {
        ServerModeration document = collection.find(eq("_id", guild.getId())).first();
        return document == null ? new ServerModeration(guild) : document;
    }

    public static void insertServerModeration(ServerModeration admin) {
        getAdminCollection()
            .insertOne(admin);
    }

    public static void insertServerModeration(ServerModeration admin, InsertOneOptions options) {
        getAdminCollection()
            .insertOne(
                admin,
                options
            );
    }

    public static void replaceServerModeration(ServerModeration admin) {
        getAdminCollection()
            .replaceOne(
                new Document("_id", admin.getId()),
                admin
            );
    }

    public static void replaceServerModeration(ServerModeration admin, ReplaceOptions options) {
        replaceServerModeration(getAdminCollection(), admin, options);
    }

    private static void replaceServerModeration(MongoCollection<ServerModeration> collection, ServerModeration admin, ReplaceOptions options) {
        collection.replaceOne(
                new Document("_id", admin.getId()),
                admin,
                options
            );
    }

    public static GuildEventListenerManager getGuildEventListenerManager(Guild guild) {
        return findServerModeration(guild).getGuildEventListenerManager();
    }

    public static GuildApplicationCommandPermissionEvent getGuildApplicationCommandPermissionEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildApplicationCommandPermissionEvent();
    }

    public static GuildChannelEvent getGuildChannelEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildChannelEvent();
    }

    public static GuildEmojiEvent getGuildEmojiEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildEmojiEvent();
    }

    public static GuildEvent getGuildEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildEvent();
    }

    public static GuildForumTagEvent getGuildForumTagEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildForumTagEvent();
    }

    public static GuildInteractionEvent getGuildInteractionEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildInteractionEvent();
    }

    public static GuildInviteEvent getGuildInviteEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildInviteEvent();
    }

    public static GuildMemberEvent getGuildMemberEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildMemberEvent();
    }

    public static GuildMessageEvent getGuildMessageEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildMessageEvent();
    }

    public static GuildPermissionOverrideEvent getGuildPermissionOverrideEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildPermissionOverrideEvent();
    }

    public static GuildRoleEvent getGuildRoleEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildRoleEvent();
    }

    public static GuildScheduledEvent getGuildScheduledEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildScheduledEvent();
    }

    public static GuildStageInstanceEvent getGuildStageInstanceEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildStageInstanceEvent();
    }

    public static GuildStickerEvent getGuildStickerEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildStickerEvent();
    }

    public static GuildThreadEvent getGuildThreadEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildThreadEvent();
    }

    public static GuildVoiceEvent getGuildVoiceEvent(Guild guild) {
        return getGuildEventListenerManager(guild).getGuildVoiceEvent();
    }

    public Object retrieveField(String guildEventName, String guildEventFieldName) {
        GuildEventListenerManager eventListenerManager = document.getGuildEventListenerManager();
        String upperEventString = Character.toUpperCase(guildEventName.charAt(0)) + guildEventName.substring(1);
        String upperEventFieldString = Character.toUpperCase(guildEventFieldName.charAt(0)) + guildEventFieldName.substring(1);

        try {
            Object eventMethod = eventListenerManager.getClass()
                .getDeclaredMethod("get" + upperEventString)
                .invoke(eventListenerManager);

            Method eventFieldMethod;

            try {
                try {
                    eventFieldMethod = eventMethod.getClass()
                        .getDeclaredMethod("get" + upperEventFieldString);

                } catch (NoSuchMethodException e) {
                    eventFieldMethod = eventMethod.getClass()
                        .getSuperclass()
                        .getDeclaredMethod("get" + upperEventFieldString);
                }

            } catch (NoSuchMethodException e) {
                try {
                    eventFieldMethod = eventMethod.getClass()
                        .getDeclaredMethod("is" + upperEventFieldString);

                } catch (NoSuchMethodException ee) {
                    eventFieldMethod = eventMethod.getClass()
                        .getSuperclass()
                        .getDeclaredMethod("is" + upperEventFieldString);
                }
            }

            return eventFieldMethod.invoke(eventMethod);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateField(String guildEventName, String guildEventFieldName)
        throws IllegalStateException {

        Object oldValue = retrieveField(guildEventName, guildEventFieldName);

        if (oldValue.getClass().isAssignableFrom(Boolean.class))
            updateField(guildEventName, guildEventFieldName, !((boolean) oldValue));

        else
            updateField(guildEventName, guildEventFieldName, oldValue);
    }

    public void updateField(String guildEventName, String guildEventFieldName, Object newValue)
        throws IllegalStateException {

        if (!findServerModeration(collection, guild).equals(document))
            throw new IllegalStateException("Document has been modified since last retrieval");

        GuildEventListenerManager eventListenerManager = document.getGuildEventListenerManager();
        boolean isValueBoolean = newValue.getClass().isAssignableFrom(Boolean.class);
        String upperEventString = Character.toUpperCase(guildEventName.charAt(0)) + guildEventName.substring(1);
        String upperEventFieldString = Character.toUpperCase(guildEventFieldName.charAt(0)) + guildEventFieldName.substring(1);

        try {
            Object eventMethod = eventListenerManager.getClass()
                .getDeclaredMethod("get" + upperEventString)
                .invoke(eventListenerManager);

            Method eventFieldMethod = null;

            try {
                eventFieldMethod = eventMethod.getClass()
                    .getDeclaredMethod(
                        "set" + upperEventFieldString,
                        isValueBoolean ? boolean.class : String.class
                    );

            } catch (NoSuchMethodException e) {
                eventFieldMethod = eventMethod.getClass()
                    .getSuperclass()
                    .getDeclaredMethod(
                        "set" + upperEventFieldString,
                        isValueBoolean ? boolean.class : String.class
                    );
            }

            eventFieldMethod.invoke(eventMethod, newValue);
            replaceServerModeration(collection, document, new ReplaceOptions().upsert(true));

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
