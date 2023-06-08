package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Message;

@Accessors(chain = true)
@BsonDiscriminator(value="MessageContent", key="_cls")
@Data
public class MessageContent {
    @BsonCreator
    public MessageContent() {}

    public MessageContent(Message message) {
        this.attachments = message.getAttachments().stream().map(MessageAttachment::new).toList();
        this.raw = message.getContentRaw();
        this.stripped = message.getContentStripped();
        this.display = message.getContentDisplay();
        this.timeCreated = message.getTimeEdited() == null ? message.getTimeCreated().toLocalDateTime() : message.getTimeEdited().toLocalDateTime();
    }

    @BsonProperty(value = "attachments")
    private List<MessageAttachment> attachments;

    @BsonProperty(value = "raw")
    private String raw;

    @BsonProperty(value = "stripped")
    private String stripped;

    @BsonProperty(value = "display")
    private String display;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;
}
