package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildStickerEvent extends GuildGenericEvent {
    public GuildStickerEvent() {
        super();
    }

    @BsonProperty(value = "added_event")
    protected boolean addedEvent;

    @BsonProperty(value = "removed_event")
    protected boolean removedEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_tags_event")
    protected boolean updateTagsEvent;

    @BsonProperty(value = "update_description_event")
    protected boolean updateDescriptionEvent;

    @BsonProperty(value = "update_available_event")
    protected boolean updateAvailableEvent;
}
