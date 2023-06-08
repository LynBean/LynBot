package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildStageInstanceEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildStageInstanceEvent() {
        super();
    }

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "update_topic_event")
    protected boolean updateTopicEvent;

    @BsonProperty(value = "update_privacy_level_event")
    protected boolean updatePrivacyLevelEvent;

    @BsonProperty(value = "create_event")
    protected boolean createEvent;
}
