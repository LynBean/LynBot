package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildThreadEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildThreadEvent() {
        super();
    }

    @BsonProperty(value = "revealed_event")
    protected boolean revealedEvent;

    @BsonProperty(value = "hidden_event")
    protected boolean hiddenEvent;

    @BsonProperty(value = "member_join_event")
    protected boolean memberJoinEvent;

    @BsonProperty(value = "member_leave_event")
    protected boolean memberLeaveEvent;
}
