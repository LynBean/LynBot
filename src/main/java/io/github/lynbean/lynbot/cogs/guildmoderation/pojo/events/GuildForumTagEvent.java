package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildForumTagEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildForumTagEvent() {
        super();
    }

    @BsonProperty(value = "add_event")
    protected boolean addEvent;

    @BsonProperty(value = "remove_event")
    protected boolean removeEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_emoji_event")
    protected boolean updateEmojiEvent;

    @BsonProperty(value = "update_moderated_event")
    protected boolean updateModeratedEvent;
}
