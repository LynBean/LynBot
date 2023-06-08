package io.github.lynbean.lynbot.cogs.guildmoderation.pojo;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildApplicationCommandPermissionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildChannelEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEmojiEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildForumTagEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInteractionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInviteEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMemberEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMessageEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildPermissionOverrideEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildRoleEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildScheduledEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStageInstanceEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStickerEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildThreadEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildVoiceEvent;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class GuildEventListenerManager {
    @BsonCreator
    public GuildEventListenerManager() {
        this.guildApplicationCommandPermissionEvent = new GuildApplicationCommandPermissionEvent();
        this.guildChannelEvent = new GuildChannelEvent();
        this.guildEmojiEvent = new GuildEmojiEvent();
        this.guildEvent = new GuildEvent();
        this.guildForumTagEvent = new GuildForumTagEvent();
        this.guildInteractionEvent = new GuildInteractionEvent();
        this.guildInviteEvent = new GuildInviteEvent();
        this.guildMemberEvent = new GuildMemberEvent();
        this.guildMessageEvent = new GuildMessageEvent();
        this.guildPermissionOverrideEvent = new GuildPermissionOverrideEvent();
        this.guildRoleEvent = new GuildRoleEvent();
        this.guildScheduledEvent = new GuildScheduledEvent();
        this.guildStageInstanceEvent = new GuildStageInstanceEvent();
        this.guildStickerEvent = new GuildStickerEvent();
        this.guildThreadEvent = new GuildThreadEvent();
        this.guildVoiceEvent = new GuildVoiceEvent();
    }

    @BsonProperty(value = "guild_application_command_permission_event")
    private GuildApplicationCommandPermissionEvent guildApplicationCommandPermissionEvent;

    @BsonProperty(value = "guild_channel_event")
    private GuildChannelEvent guildChannelEvent;

    @BsonProperty(value = "guild_emoji_event")
    private GuildEmojiEvent guildEmojiEvent;

    @BsonProperty(value = "guild_event")
    private GuildEvent guildEvent;

    @BsonProperty(value = "guild_forum_tag_event")
    private GuildForumTagEvent guildForumTagEvent;

    @BsonProperty(value = "guild_interaction_event")
    private GuildInteractionEvent guildInteractionEvent;

    @BsonProperty(value = "guild_invite_event")
    private GuildInviteEvent guildInviteEvent;

    @BsonProperty(value = "guild_member_event")
    private GuildMemberEvent guildMemberEvent;

    @BsonProperty(value = "guild_message_event")
    private GuildMessageEvent guildMessageEvent;

    @BsonProperty(value = "guild_permission_override_event")
    private GuildPermissionOverrideEvent guildPermissionOverrideEvent;

    @BsonProperty(value = "guild_role_event")
    private GuildRoleEvent guildRoleEvent;

    @BsonProperty(value = "guild_scheduled_event")
    private GuildScheduledEvent guildScheduledEvent;

    @BsonProperty(value = "guild_stage_instance_event")
    private GuildStageInstanceEvent guildStageInstanceEvent;

    @BsonProperty(value = "guild_sticker_event")
    private GuildStickerEvent guildStickerEvent;

    @BsonProperty(value = "guild_thread_event")
    private GuildThreadEvent guildThreadEvent;

    @BsonProperty(value = "guild_voice_event")
    private GuildVoiceEvent guildVoiceEvent;
}
