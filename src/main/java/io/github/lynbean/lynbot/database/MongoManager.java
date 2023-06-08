package io.github.lynbean.lynbot.database;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;

import io.github.lynbean.lynbot.database.pojo.ServerChannel;
import io.github.lynbean.lynbot.database.pojo.ServerEmoji;
import io.github.lynbean.lynbot.database.pojo.ServerMessage;
import io.github.lynbean.lynbot.database.pojo.ServerSticker;
import io.github.lynbean.lynbot.database.pojo.ServerWrapper;
import io.github.lynbean.lynbot.database.pojo.UserWrapper;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

@Accessors(chain = true)
public class MongoManager {
    private @Getter MongoDatabase db;
    private @Getter CodecProvider pojoCodecProvider;
    private @Getter CodecRegistry codecRegistry;
    private @Getter ConnectionString connectionString;
    private @Getter MongoClient client;
    private @Getter MongoClientSettings clientSettings;

    private @Getter List<String> codecPackageNames = new ArrayList<>();

    private static final String SERVER_CHANNEL_COLLECTION = "server_channels";
    private static final String SERVER_EMOJI_COLLECTION = "server_emojis";
    private static final String SERVER_MESSAGE_COLLECTION = "server_messages";
    private static final String SERVER_STICKER_COLLECTION = "server_stickers";
    private static final String SERVER_COLLECTION = "servers";
    private static final String USER_COLLECTION = "users";

    private MongoCollection<ServerChannel> serverChannelCollection = null;
    private MongoCollection<ServerEmoji> serverEmojiCollection = null;
    private MongoCollection<ServerMessage> serverMessageCollection = null;
    private MongoCollection<ServerSticker> serverStickerCollection = null;
    private MongoCollection<ServerWrapper> serverCollection = null;
    private MongoCollection<UserWrapper> userCollection = null;

    public MongoManager(String connectionString, String databaseName) {
        this.connectionString = new ConnectionString(connectionString);
        startMongo();
        this.db = client.getDatabase(databaseName);
    }

    public MongoManager setCodecPackages(String... packageNames) {
        this.codecPackageNames.clear();
        return addCodecPackages(packageNames);
    }

    public MongoManager addCodecPackages(String... packageNames) {
        Stream.of(packageNames).forEach(codecPackageNames::add);
        stopMongo();
        startMongo();
        return this;
    }

    private void startMongo() {
        List.of(getClass().getDeclaredFields())
            .stream()
            .filter(field -> field.getType().equals(MongoCollection.class))
            .forEach(field -> {
                try {
                    field.set(this, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

        this.pojoCodecProvider = PojoCodecProvider.builder()
            .automatic(true)
            .register(codecPackageNames.toArray(new String[0]))
            .build();

        this.codecRegistry = createCodecRegistry(pojoCodecProvider);
        this.clientSettings = createClientSettings(connectionString, codecRegistry);
        this.client = MongoClients.create(clientSettings);

        if (db != null) {
            this.db = client.getDatabase(db.getName());
        }
    }

    public void stopMongo() {
        client.close();
    }

    private CodecRegistry createCodecRegistry(CodecProvider pojoCodecProvider) {
        return fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    }

    private MongoClientSettings createClientSettings(ConnectionString connectionString, CodecRegistry codecRegistry) {
        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return db.getCollection(collectionName);
    }

    public <T> MongoCollection<T> getCollection(String collectionName, Class<T> clazz) {
        return db.getCollection(collectionName, clazz);
    }

    public void createCollection(String collectionName) {
        db.createCollection(collectionName);
    }

    public void dropCollection(String collectionName) {
        db.getCollection(collectionName).drop();
    }

    public void insertOne(String collectionName, Document object) {
        db.getCollection(collectionName).insertOne(object);
    }

    public void insertMany(String collectionName, List<Document> objects) {
        db.getCollection(collectionName).insertMany(objects);
    }

    public MongoCollection<ServerChannel> getServerChannelCollection() {
        if (serverChannelCollection == null) {
            serverChannelCollection = getCollection(SERVER_CHANNEL_COLLECTION, ServerChannel.class);
        }
        return serverChannelCollection;
    }

    public @Nullable ServerChannel findServerChannel(String channelId) {
        return getServerChannelCollection()
            .find(eq("_id", channelId))
            .first();
    }

    public @Nonnull ServerChannel findServerChannel(GuildChannel channel) {
        ServerChannel document = getServerChannelCollection()
            .find(eq("_id", channel.getId()))
            .first();

        return document == null
            ? new ServerChannel(channel)
            : document;
    }

    public void insertServerChannel(ServerChannel channel) {
        getServerChannelCollection()
            .insertOne(channel);
    }

    public void insertServerChannel(ServerChannel channel, InsertOneOptions options) {
        getServerChannelCollection()
            .insertOne(
                channel,
                options
            );
    }

    public void replaceServerChannel(ServerChannel channel) {
        getServerChannelCollection()
            .replaceOne(
                eq("_id", channel.getId()),
                channel
            );
    }

    public void replaceServerChannel(ServerChannel channel, ReplaceOptions options) {
        getServerChannelCollection()
            .replaceOne(
                eq("_id", channel.getId()),
                channel,
                options
            );
    }

    public MongoCollection<ServerEmoji> getServerEmojiCollection() {
        if (serverEmojiCollection == null) {
            serverEmojiCollection = getCollection(SERVER_EMOJI_COLLECTION, ServerEmoji.class);
        }
        return serverEmojiCollection;
    }

    public @Nullable ServerEmoji findServerEmoji(String emojiId) {
        return getServerEmojiCollection()
            .find(eq("_id", emojiId))
            .first();
    }

    public @Nonnull ServerEmoji findServerEmoji(RichCustomEmoji emoji) {
        ServerEmoji document = getServerEmojiCollection()
            .find(eq("_id", emoji.getId()))
            .first();

        return document == null
            ? new ServerEmoji(emoji)
            : document;
    }

    public void insertServerEmoji(ServerEmoji emoji) {
        getServerEmojiCollection()
            .insertOne(emoji);
    }

    public void insertServerEmoji(ServerEmoji emoji, InsertOneOptions options) {
        getServerEmojiCollection()
            .insertOne(
                emoji,
                options
            );
    }

    public void replaceServerEmoji(ServerEmoji emoji) {
        getServerEmojiCollection()
            .replaceOne(
                eq("_id", emoji.getId()),
                emoji
            );
    }

    public void replaceServerEmoji(ServerEmoji emoji, ReplaceOptions options) {
        getServerEmojiCollection()
            .replaceOne(
                eq("_id", emoji.getId()),
                emoji,
                options
            );
    }

    public MongoCollection<ServerMessage> getServerMessageCollection() {
        if (serverMessageCollection == null) {
            serverMessageCollection = getCollection(SERVER_MESSAGE_COLLECTION, ServerMessage.class);
        }
        return serverMessageCollection;
    }

    public @Nullable ServerMessage findServerMessage(String messageId) {
        return getServerMessageCollection()
            .find(eq("_id", messageId))
            .first();
    }

    public @Nonnull ServerMessage findServerMessage(Message message) {
        ServerMessage document = getServerMessageCollection()
            .find(eq("_id", message.getId()))
            .first();

        return document == null
            ? new ServerMessage(message)
            : document;
    }

    public @Nullable UserWrapper getMessageAuthor(String messageId) {
        ServerMessage message = findServerMessage(messageId);

        if (message == null)
            return null;

        return findUser(message.getAuthorId());
    }

    public void insertServerMessage(ServerMessage message) {
        getServerMessageCollection()
            .insertOne(message);
    }

    public void insertServerMessage(ServerMessage message, boolean skipExists) {
        if (skipExists && findServerMessage(message.getId()) != null)
            return;

        getServerMessageCollection()
            .insertOne(message);
    }

    public void insertServerMessage(ServerMessage message, InsertOneOptions options) {
        getServerMessageCollection()
            .insertOne(
                message,
                options
            );
    }

    public void replaceServerMessage(ServerMessage message) {
        getServerMessageCollection()
            .replaceOne(
                eq("_id", message.getId()),
                message
            );
    }

    public void replaceServerMessage(ServerMessage message, ReplaceOptions options) {
        getServerMessageCollection()
            .replaceOne(
                eq("_id", message.getId()),
                message,
                options
            );
    }

    public MongoCollection<ServerSticker> getServerStickerCollection() {
        if (serverStickerCollection == null) {
            serverStickerCollection = getCollection(SERVER_STICKER_COLLECTION, ServerSticker.class);
        }
        return serverStickerCollection;
    }

    public @Nullable ServerSticker findServerSticker(String stickerId) {
        return getServerStickerCollection()
            .find(eq("_id", stickerId))
            .first();
    }

    public @Nonnull ServerSticker findServerSticker(GuildSticker sticker) {
        ServerSticker document = getServerStickerCollection()
            .find(eq("_id", sticker.getId()))
            .first();

        return document == null
            ? new ServerSticker(sticker)
            : document;
    }

    public void insertServerSticker(ServerSticker sticker) {
        getServerStickerCollection()
            .insertOne(sticker);
    }

    public void insertServerSticker(ServerSticker sticker, InsertOneOptions options) {
        getServerStickerCollection()
            .insertOne(
                sticker,
                options
            );
    }

    public void replaceServerSticker(ServerSticker sticker) {
        getServerStickerCollection()
            .replaceOne(
                eq("_id", sticker.getId()),
                sticker
            );
    }

    public void replaceServerSticker(ServerSticker sticker, ReplaceOptions options) {
        getServerStickerCollection()
            .replaceOne(
                eq("_id", sticker.getId()),
                sticker,
                options
            );
    }

    public MongoCollection<ServerWrapper> getServerCollection() {
        if (serverCollection == null) {
            serverCollection = getCollection(SERVER_COLLECTION, ServerWrapper.class);
        }
        return serverCollection;
    }

    public @Nullable ServerWrapper findServer(String guildId) {
        return getServerCollection()
            .find(eq("_id", guildId))
            .first();
    }

    public @Nonnull ServerWrapper findServer(Guild guild) {
        ServerWrapper document = getServerCollection()
            .find(eq("_id", guild.getId()))
            .first();

        return document == null
            ? new ServerWrapper(guild)
            : document;
    }

    public void insertServer(ServerWrapper guild) {
        getServerCollection()
            .insertOne(guild);
    }

    public void insertServer(ServerWrapper guild, InsertOneOptions options) {
        getServerCollection()
            .insertOne(
                guild,
                options
            );
    }

    public void replaceServer(ServerWrapper guild) {
        getServerCollection()
            .replaceOne(
                eq("_id", guild.getId()),
                guild
            );
    }

    public void replaceServer(ServerWrapper guild, ReplaceOptions options) {
        getServerCollection()
            .replaceOne(
                eq("_id", guild.getId()),
                guild,
                options
            );
    }

    public MongoCollection<UserWrapper> getUserCollection() {
        if (userCollection == null) {
            userCollection = getCollection(USER_COLLECTION, UserWrapper.class);
        }
        return userCollection;
    }

    public @Nullable UserWrapper findUser(String userId) {
        return getUserCollection()
            .find(eq("_id", userId))
            .first();
    }

    public @Nonnull UserWrapper findUser(net.dv8tion.jda.api.entities.User user) {
        UserWrapper document = getUserCollection()
            .find(eq("_id", user.getId()))
            .first();

        return document == null
            ? new UserWrapper(user)
            : document;
    }

    public void insertUser(UserWrapper user) {
        getUserCollection()
            .insertOne(user);
    }

    public void insertUser(UserWrapper user, InsertOneOptions options) {
        getUserCollection()
            .insertOne(
                user,
                options
            );
    }

    public void replaceUser(UserWrapper user) {
        getUserCollection()
            .replaceOne(
                eq("_id", user.getId()),
                user
            );
    }

    public void replaceUser(UserWrapper user, ReplaceOptions options) {
        getUserCollection()
            .replaceOne(
                eq("_id", user.getId()),
                user,
                options
            );
    }

    public void close() {
        client.close();
    }
}
