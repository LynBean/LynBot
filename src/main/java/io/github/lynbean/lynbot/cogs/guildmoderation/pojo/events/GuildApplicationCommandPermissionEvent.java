package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildApplicationCommandPermissionEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildApplicationCommandPermissionEvent() {
        super();
    }

    @BsonProperty(value = "generic_privilege_update_event")
    protected boolean genericPrivilegeUpdateEvent;

    @BsonProperty(value = "application_command_update_privileges_event")
    protected boolean applicationCommandUpdatePrivilegesEvent;

    @BsonProperty(value = "application_update_privileges_event")
    protected boolean applicationUpdatePrivilegesEvent;
}