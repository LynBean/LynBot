package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildInviteEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildInviteEvent() {
        super();
    }

    @BsonProperty(value = "create_event")
    protected boolean createEvent;

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;
}
