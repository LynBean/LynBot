package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MessageFlag;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerMessage", key="_cls")
@Data
public class ServerMessage {
    @BsonCreator
    public ServerMessage() {}

    public ServerMessage(Message message) {
        this.authorId = message.getAuthor().getId();
        this.channelId = message.getChannel().getId();
        this.content = List.of(new MessageContent(message));
        this.flags = message.getFlags().stream().map(MessageFlag::name).toList();
        this.flagsRaw = message.getFlagsRaw();
        this.guildId = message.getGuild().getId();
        this.id = message.getId();
        this.jumpUrl = message.getJumpUrl();
        this.messageReferenceId = message.getMessageReference() == null ? null : message.getMessageReference().getMessageId();
        this.startedThreadId = message.getStartedThread() == null ? null : message.getStartedThread().getId();
        this.timeCreated = message.getTimeCreated().toLocalDateTime();
        this.timeEdited = message.getTimeEdited() == null ? null : message.getTimeEdited().toLocalDateTime();
        this.type = message.getType().name();
    }

    public ServerMessage updateMessage(Message message) {
        MessageContent newMessageContent = new MessageContent(message);

        if (!this.content.get(0).equals(newMessageContent)) {
            this.content.add(newMessageContent);
        }

        this.timeEdited = message.getTimeEdited() == null ? null : message.getTimeEdited().toLocalDateTime();
        return this;
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "author_id")
    private String authorId;

    @BsonProperty(value = "channel_id")
    private String channelId;

    @BsonProperty(value = "content")
    private List<MessageContent> content;

    @BsonProperty(value = "flags")
    private List<String> flags;

    @BsonProperty(value = "flags_raw")
    private Long flagsRaw;

    @BsonProperty(value = "guild_id")
    private String guildId;

    @BsonProperty(value = "jump_url")
    private String jumpUrl;

    @BsonProperty(value = "message_reference_id")
    private String messageReferenceId;

    @BsonProperty(value = "started_thread_id")
    private String startedThreadId;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "time_edited")
    private LocalDateTime timeEdited;

    @BsonProperty(value = "type")
    private String type;
}
