package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Message.Attachment;

@Accessors(chain = true)
@BsonDiscriminator(value="MessageAttachment", key="_cls")
@Data
public class MessageAttachment {
    @BsonCreator
    public MessageAttachment() {}

    public MessageAttachment(Attachment attachment) {
        this.id = attachment.getId();
        this.contentType = attachment.getContentType();
        this.description = attachment.getDescription();
        this.fileName = attachment.getFileName();
        this.height = attachment.getHeight();
        this.proxyUrl = attachment.getProxyUrl();
        this.size = attachment.getSize();
        this.timeCreated = attachment.getTimeCreated().toLocalDateTime();
        this.url = attachment.getUrl();
        this.width = attachment.getWidth();
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "content_type")
    private String contentType;

    @BsonProperty(value = "description")
    private String description;

    @BsonProperty(value = "file_name")
    private String fileName;

    @BsonProperty(value = "height")
    private Integer height;

    @BsonProperty(value = "proxy_url")
    private String proxyUrl;

    @BsonProperty(value = "size")
    private Integer size;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "url")
    private String url;

    @BsonProperty(value = "width")
    private Integer width;
}
