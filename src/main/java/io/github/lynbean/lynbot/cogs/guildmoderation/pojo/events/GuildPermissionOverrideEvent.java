package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildPermissionOverrideEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildPermissionOverrideEvent() {
        super();
    }

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "update_event")
    protected boolean updateEvent;

    @BsonProperty(value = "create_event")
    protected boolean createEvent;
}
