package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildScheduledEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildScheduledEvent() {
        super();
    }

    @BsonProperty(value = "update_description_event")
    protected boolean updateDescriptionEvent;

    @BsonProperty(value = "update_end_time_event")
    protected boolean updateEndTimeEvent;

    @BsonProperty(value = "update_location_event")
    protected boolean updateLocationEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_start_time_event")
    protected boolean updateStartTimeEvent;

    @BsonProperty(value = "update_status_event")
    protected boolean updateStatusEvent;

    @BsonProperty(value = "create_event")
    protected boolean createEvent;

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "user_add_event")
    protected boolean userAddEvent;

    @BsonProperty(value = "user_remove_event")
    protected boolean userRemoveEvent;
}
