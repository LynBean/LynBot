package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildVoiceEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildVoiceEvent() {
        super();
    }

    @BsonProperty(value = "update_event")
    protected boolean updateEvent;

    @BsonProperty(value = "mute_event")
    protected boolean muteEvent;

    @BsonProperty(value = "deafen_event")
    protected boolean deafenEvent;

    @BsonProperty(value = "guild_mute_event")
    protected boolean guildMuteEvent;

    @BsonProperty(value = "guild_deafen_event")
    protected boolean guildDeafenEvent;

    @BsonProperty(value = "self_mute_event")
    protected boolean selfMuteEvent;

    @BsonProperty(value = "self_deafen_event")
    protected boolean selfDeafenEvent;

    @BsonProperty(value = "suppress_event")
    protected boolean suppressEvent;

    @BsonProperty(value = "stream_event")
    protected boolean streamEvent;

    @BsonProperty(value = "video_event")
    protected boolean videoEvent;

    @BsonProperty(value = "request_to_speak_event")
    protected boolean requestToSpeakEvent;
}
