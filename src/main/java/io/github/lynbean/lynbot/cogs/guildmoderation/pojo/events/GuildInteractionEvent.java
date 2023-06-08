package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildInteractionEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildInteractionEvent() {
        super();
    }

    @BsonProperty(value = "slash_command_interaction_event")
    protected boolean slashCommandInteractionEvent;

    @BsonProperty(value = "user_context_interaction_event")
    protected boolean userContextInteractionEvent;

    @BsonProperty(value = "message_context_interaction_event")
    protected boolean messageContextInteractionEvent;

    @BsonProperty(value = "button_interaction_event")
    protected boolean buttonInteractionEvent;

    @BsonProperty(value = "command_auto_complete_interaction_event")
    protected boolean commandAutoCompleteInteractionEvent;

    @BsonProperty(value = "modal_interaction_event")
    protected boolean modalInteractionEvent;

    @BsonProperty(value = "string_select_interaction_event")
    protected boolean stringSelectInteractionEvent;

    @BsonProperty(value = "entity_select_interaction_event")
    protected boolean entitySelectInteractionEvent;
}
