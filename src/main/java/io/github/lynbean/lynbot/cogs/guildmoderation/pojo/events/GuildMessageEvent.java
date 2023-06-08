package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildMessageEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildMessageEvent() {
        super();
    }

    @BsonProperty(value = "update_event")
    protected boolean updateEvent;

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "bulk_delete_event")
    protected boolean bulkDeleteEvent;

    @BsonProperty(value = "reaction_add_event")
    protected boolean reactionAddEvent;

    @BsonProperty(value = "reaction_remove_event")
    protected boolean reactionRemoveEvent;

    @BsonProperty(value = "reaction_remove_all_event")
    protected boolean reactionRemoveAllEvent;

    @BsonProperty(value = "reaction_remove_emoji_event")
    protected boolean reactionRemoveEmojiEvent;
}
