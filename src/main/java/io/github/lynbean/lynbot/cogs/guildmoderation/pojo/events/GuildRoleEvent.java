package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildRoleEvent extends GuildGenericEvent {
    public GuildRoleEvent() {
        super();
    }

    @BsonProperty(value = "create_event")
    protected boolean createEvent;

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "update_color_event")
    protected boolean updateColorEvent;

    @BsonProperty(value = "update_hoisted_event")
    protected boolean updateHoistedEvent;

    @BsonProperty(value = "update_icon_event")
    protected boolean updateIconEvent;

    @BsonProperty(value = "update_mentionable_event")
    protected boolean updateMentionableEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_permissions_event")
    protected boolean updatePermissionsEvent;

    @BsonProperty(value = "update_position_event")
    protected boolean updatePositionEvent;
}
