package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildEmojiEvent extends GuildGenericEvent {
    public GuildEmojiEvent() {
        super();
    }

    @BsonProperty(value = "added_event")
    protected boolean addedEvent;

    @BsonProperty(value = "removed_event")
    protected boolean removedEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_roles_event")
    protected boolean updateRolesEvent;
}
