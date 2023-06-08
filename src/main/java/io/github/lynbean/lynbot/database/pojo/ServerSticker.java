package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerSticker", key="_cls")
@Data
public class ServerSticker {
    @BsonCreator
    public ServerSticker() {}

    public ServerSticker(GuildSticker sticker) {
        this.id = sticker.getId();
        this.description = sticker.getDescription();
        this.guildId = sticker.getGuildId();
        this.iconUrl = sticker.getIconUrl();
        this.name = sticker.getName();
        this.ownerId = sticker.getOwner() == null ? null : sticker.getOwner().getId();
        this.tags = sticker.getTags().stream().toList();
        this.timeCreated = sticker.getTimeCreated().toLocalDateTime();
        this.type = sticker.getType().name();
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "description")
    private String description;

    @BsonProperty(value = "guild_id")
    private String guildId;

    @BsonProperty(value = "icon_url")
    private String iconUrl;

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "owner_id")
    private String ownerId;

    @BsonProperty(value = "tags")
    private List<String> tags;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "type")
    private String type;
}
