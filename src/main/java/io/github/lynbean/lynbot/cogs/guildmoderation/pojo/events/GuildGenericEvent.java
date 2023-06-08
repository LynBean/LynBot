package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public abstract class GuildGenericEvent {
    @BsonCreator
    public GuildGenericEvent() {
        List.of(this.getClass().getDeclaredFields())
            .stream()
            .filter(field -> field.getType().equals(boolean.class))
            .forEach(field -> {
                try {
                    field.set(this, false);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @BsonProperty(value = "log_channel_id")
    protected String logChannelId;
}
