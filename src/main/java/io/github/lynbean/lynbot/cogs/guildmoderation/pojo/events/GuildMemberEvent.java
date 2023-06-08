package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildMemberEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildMemberEvent() {
        super();
    }

    @BsonProperty(value = "join_event")
    protected boolean joinEvent;

    @BsonProperty(value = "role_add_event")
    protected boolean roleAddEvent;

    @BsonProperty(value = "role_remove_event")
    protected boolean roleRemoveEvent;

    @BsonProperty(value = "update_event")
    protected boolean updateEvent;

    @BsonProperty(value = "update_nickname_event")
    protected boolean updateNicknameEvent;

    @BsonProperty(value = "update_avatar_event")
    protected boolean updateAvatarEvent;

    @BsonProperty(value = "update_boost_time_event")
    protected boolean updateBoostTimeEvent;

    @BsonProperty(value = "update_pending_event")
    protected boolean updatePendingEvent;

    @BsonProperty(value = "update_flags_event")
    protected boolean updateFlagsEvent;

    @BsonProperty(value = "update_time_out_event")
    protected boolean updateTimeOutEvent;
}
