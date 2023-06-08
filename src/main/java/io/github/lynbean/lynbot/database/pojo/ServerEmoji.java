package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerEmoji", key="_cls")
@Data
public class ServerEmoji {
    @BsonCreator
    public ServerEmoji() {}

    public ServerEmoji(RichCustomEmoji emoji) {
        this.id = emoji.getId();
        this.asMention = emoji.getAsMention();
        this.asReactionCode = emoji.getAsReactionCode();
        this.asFormatted = emoji.getAsMention();
        this.guildId = emoji.getGuild().getId();
        this.imageUrl = emoji.getImageUrl();
        this.name = emoji.getName();
        this.ownerId = emoji.getOwner() == null ? null : emoji.getOwner().getId();
        this.timeCreated = emoji.getTimeCreated().toLocalDateTime();
        this.type = emoji.getType().name();
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "as_mention")
    private String asMention;

    @BsonProperty(value = "as_reaction_code")
    private String asReactionCode;

    @BsonProperty(value = "as_formatted")
    private String asFormatted;

    @BsonProperty(value = "guild_id")
    private String guildId;

    @BsonProperty(value = "image_url")
    private String imageUrl;

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "owner_id")
    private String ownerId;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "type")
    private String type;
}
