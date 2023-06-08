package io.github.lynbean.lynbot.cogs.guildmoderation.pojo;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerModeration", key="_cls")
@Data
public class ServerModeration {
    @BsonCreator
    public ServerModeration() {
        this.guildEventListenerManager = new GuildEventListenerManager();
    }

    public ServerModeration(Guild guild) {
        this();
        this.id = guild.getId();
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "guild_event_listeners")
    private GuildEventListenerManager guildEventListenerManager;
}
