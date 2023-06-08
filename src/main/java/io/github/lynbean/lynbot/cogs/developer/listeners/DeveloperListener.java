package io.github.lynbean.lynbot.cogs.developer.listeners;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.ReplaceOptions;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.database.pojo.ServerMessage;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.GenericForumTagEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateModeratedEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.forum.update.GenericForumTagUpdateEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchiveTimestampEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAutoArchiveDurationEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateBitrateEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateDefaultLayoutEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateDefaultReactionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateDefaultThreadSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateFlagsEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateInvitableEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateLockedEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateRegionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateTopicEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateTypeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateUserLimitEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent;
import net.dv8tion.jda.api.events.emoji.update.GenericEmojiUpdateEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildTimeoutEvent;
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateFlagsEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventCreateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserAddEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.GenericScheduledEventUpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateEndTimeEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateLocationEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStartTimeEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStatusEvent;
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateAfkChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateAfkTimeoutEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBannerEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateCommunityUpdatesChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateExplicitContentLevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateFeaturesEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateLocaleEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateMFALevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateMaxMembersEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateMaxPresencesEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNSFWLevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNotificationLevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateOwnerEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateRulesChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateSplashEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateSystemChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateVanityCodeEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateVerificationLevelEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceRequestToSpeakEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSuppressEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceVideoEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericPrivilegeUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateHoistedEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateIconEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateMentionableEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.events.self.GenericSelfUpdateEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateAvatarEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateMFAEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateNameEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateVerifiedEvent;
import net.dv8tion.jda.api.events.session.GenericSessionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionInvalidateEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent;
import net.dv8tion.jda.api.events.stage.update.GenericStageInstanceUpdateEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent;
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent;
import net.dv8tion.jda.api.events.sticker.update.GenericGuildStickerUpdateEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateAvailableEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateNameEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateTagsEvent;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.events.thread.member.GenericThreadMemberEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateFlagsEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DeveloperListener extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(DeveloperListener.class);

    @Override
    public void onGenericEvent(@Nonnull GenericEvent event) {
        LOG.trace("onGenericEvent({})", event.getClass().getSimpleName());
    }

    @Override
    public void onGenericUpdate(@Nonnull UpdateEvent<?, ?> event) {}

    @Override
    public void onRawGateway(@Nonnull RawGatewayEvent event) {}

    @Override
    public void onGatewayPing(@Nonnull GatewayPingEvent event) {}

    //Session Events

    @Override
    public void onReady(@Nonnull ReadyEvent event) {}

    @Override
    public void onSessionInvalidate(@Nonnull SessionInvalidateEvent event) {}

    @Override
    public void onSessionDisconnect(@Nonnull SessionDisconnectEvent event) {}

    @Override
    public void onSessionResume(@Nonnull SessionResumeEvent event) {}

    @Override
    public void onSessionRecreate(@Nonnull SessionRecreateEvent event) {}

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {}

    //Status Events

    @Override
    public void onStatusChange(@Nonnull StatusChangeEvent event) {}

    @Override
    public void onException(@Nonnull ExceptionEvent event) {}

    //Interaction Events

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {}

    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {}

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {}

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {}

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {}

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {}

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {}

    @Override
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {}

    //User Events

    @Override
    public void onUserUpdateName(@Nonnull UserUpdateNameEvent event) {}

    @Override
    public void onUserUpdateDiscriminator(@Nonnull UserUpdateDiscriminatorEvent event) {}

    @Override
    public void onUserUpdateAvatar(@Nonnull UserUpdateAvatarEvent event) {}

    @Override
    public void onUserUpdateOnlineStatus(@Nonnull UserUpdateOnlineStatusEvent event) {}

    @Override
    public void onUserUpdateActivityOrder(@Nonnull UserUpdateActivityOrderEvent event) {}

    @Override
    public void onUserUpdateFlags(@Nonnull UserUpdateFlagsEvent event) {}

    @Override
    public void onUserTyping(@Nonnull UserTypingEvent event) {}

    @Override
    public void onUserActivityStart(@Nonnull UserActivityStartEvent event) {}

    @Override
    public void onUserActivityEnd(@Nonnull UserActivityEndEvent event) {}

    @Override
    public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {}

    //Self Events. Fires only in relation to the currently logged in account.

    @Override
    public void onSelfUpdateAvatar(@Nonnull SelfUpdateAvatarEvent event) {}

    @Override
    public void onSelfUpdateMFA(@Nonnull SelfUpdateMFAEvent event) {}

    @Override
    public void onSelfUpdateName(@Nonnull SelfUpdateNameEvent event) {}

    @Override
    public void onSelfUpdateVerified(@Nonnull SelfUpdateVerifiedEvent event) {}

    //Message Events

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        ServerMessage pojoMessage = new ServerMessage(event.getMessage());
        Bot.getMongoManager().insertServerMessage(pojoMessage);
    }

    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        ServerMessage pojoMessage = Bot.getMongoManager().findServerMessage(event.getMessage());
        pojoMessage.updateMessage(event.getMessage());
        Bot.getMongoManager().replaceServerMessage(pojoMessage, new ReplaceOptions().upsert(true));
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {}

    @Override
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {}

    @Override
    public void onMessageEmbed(@Nonnull MessageEmbedEvent event) {}

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {}

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {}

    @Override
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {}

    @Override
    public void onMessageReactionRemoveEmoji(@Nonnull MessageReactionRemoveEmojiEvent event) {}

    //PermissionOverride Events

    @Override
    public void onPermissionOverrideDelete(@Nonnull PermissionOverrideDeleteEvent event) {}

    @Override
    public void onPermissionOverrideUpdate(@Nonnull PermissionOverrideUpdateEvent event) {}

    @Override
    public void onPermissionOverrideCreate(@Nonnull PermissionOverrideCreateEvent event) {}

    //StageInstance Event

    @Override
    public void onStageInstanceDelete(@Nonnull StageInstanceDeleteEvent event) {}

    @Override
    public void onStageInstanceUpdateTopic(@Nonnull StageInstanceUpdateTopicEvent event) {}

    @Override
    public void onStageInstanceUpdatePrivacyLevel(@Nonnull StageInstanceUpdatePrivacyLevelEvent event) {}

    @Override
    public void onStageInstanceCreate(@Nonnull StageInstanceCreateEvent event) {}

    //Channel Events

    @Override
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {}

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {}

    //Channel Update Events

    @Override
    public void onChannelUpdateBitrate(@Nonnull ChannelUpdateBitrateEvent event) {}

    @Override
    public void onChannelUpdateName(@Nonnull ChannelUpdateNameEvent event) {}

    @Override
    public void onChannelUpdateFlags(@Nonnull ChannelUpdateFlagsEvent event) {}

    @Override
    public void onChannelUpdateNSFW(@Nonnull ChannelUpdateNSFWEvent event) {}

    @Override
    public void onChannelUpdateParent(@Nonnull ChannelUpdateParentEvent event) {}

    @Override
    public void onChannelUpdatePosition(@Nonnull ChannelUpdatePositionEvent event) {}

    @Override
    public void onChannelUpdateRegion(@Nonnull ChannelUpdateRegionEvent event) {}

    @Override
    public void onChannelUpdateSlowmode(@Nonnull ChannelUpdateSlowmodeEvent event) {}

    @Override
    public void onChannelUpdateDefaultThreadSlowmode(@Nonnull ChannelUpdateDefaultThreadSlowmodeEvent event) {}

    @Override
    public void onChannelUpdateDefaultReaction(@Nonnull ChannelUpdateDefaultReactionEvent event) {}

    @Override
    public void onChannelUpdateDefaultLayout(@Nonnull ChannelUpdateDefaultLayoutEvent event) {}

    @Override
    public void onChannelUpdateTopic(@Nonnull ChannelUpdateTopicEvent event) {}

    @Override
    public void onChannelUpdateType(@Nonnull ChannelUpdateTypeEvent event) {}

    @Override
    public void onChannelUpdateUserLimit(@Nonnull ChannelUpdateUserLimitEvent event) {}

    @Override
    public void onChannelUpdateArchived(@Nonnull ChannelUpdateArchivedEvent event) {}

    @Override
    public void onChannelUpdateArchiveTimestamp(@Nonnull ChannelUpdateArchiveTimestampEvent event) {}

    @Override
    public void onChannelUpdateAutoArchiveDuration(@Nonnull ChannelUpdateAutoArchiveDurationEvent event) {}

    @Override
    public void onChannelUpdateLocked(@Nonnull ChannelUpdateLockedEvent event) {}

    @Override
    public void onChannelUpdateInvitable(@Nonnull ChannelUpdateInvitableEvent event) {}

    @Override
    public void onChannelUpdateAppliedTags(@Nonnull ChannelUpdateAppliedTagsEvent event) {}

    //Forum Tag Events

    @Override
    public void onForumTagAdd(@Nonnull ForumTagAddEvent event) {}

    @Override
    public void onForumTagRemove(@Nonnull ForumTagRemoveEvent event) {}

    @Override
    public void onForumTagUpdateName(@Nonnull ForumTagUpdateNameEvent event) {}

    @Override
    public void onForumTagUpdateEmoji(@Nonnull ForumTagUpdateEmojiEvent event) {}

    @Override
    public void onForumTagUpdateModerated(@Nonnull ForumTagUpdateModeratedEvent event) {}

    //Thread Events

    @Override
    public void onThreadRevealed(@Nonnull ThreadRevealedEvent event) {}

    @Override
    public void onThreadHidden(@Nonnull ThreadHiddenEvent event) {}

    //Thread Member Events

    @Override
    public void onThreadMemberJoin(@Nonnull ThreadMemberJoinEvent event) {}

    @Override
    public void onThreadMemberLeave(@Nonnull ThreadMemberLeaveEvent event) {}

    //Guild Events

    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {}

    @Override
    public void onGuildTimeout(@Nonnull GuildTimeoutEvent event) {}

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {}

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {}

    @Override
    public void onGuildAvailable(@Nonnull GuildAvailableEvent event) {}

    @Override
    public void onGuildUnavailable(@Nonnull GuildUnavailableEvent event) {}

    @Override
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {}

    @Override
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {}

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {}

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {}

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {}

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {}

    //Guild Update Events

    @Override
    public void onGuildUpdateAfkChannel(@Nonnull GuildUpdateAfkChannelEvent event) {}

    @Override
    public void onGuildUpdateSystemChannel(@Nonnull GuildUpdateSystemChannelEvent event) {}

    @Override
    public void onGuildUpdateRulesChannel(@Nonnull GuildUpdateRulesChannelEvent event) {}

    @Override
    public void onGuildUpdateCommunityUpdatesChannel(@Nonnull GuildUpdateCommunityUpdatesChannelEvent event) {}

    @Override
    public void onGuildUpdateAfkTimeout(@Nonnull GuildUpdateAfkTimeoutEvent event) {}

    @Override
    public void onGuildUpdateExplicitContentLevel(@Nonnull GuildUpdateExplicitContentLevelEvent event) {}

    @Override
    public void onGuildUpdateIcon(@Nonnull GuildUpdateIconEvent event) {}

    @Override
    public void onGuildUpdateMFALevel(@Nonnull GuildUpdateMFALevelEvent event) {}

    @Override
    public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event){}

    @Override
    public void onGuildUpdateNotificationLevel(@Nonnull GuildUpdateNotificationLevelEvent event) {}

    @Override
    public void onGuildUpdateOwner(@Nonnull GuildUpdateOwnerEvent event) {}

    @Override
    public void onGuildUpdateSplash(@Nonnull GuildUpdateSplashEvent event) {}

    @Override
    public void onGuildUpdateVerificationLevel(@Nonnull GuildUpdateVerificationLevelEvent event) {}

    @Override
    public void onGuildUpdateLocale(@Nonnull GuildUpdateLocaleEvent event) {}

    @Override
    public void onGuildUpdateFeatures(@Nonnull GuildUpdateFeaturesEvent event) {}

    @Override
    public void onGuildUpdateVanityCode(@Nonnull GuildUpdateVanityCodeEvent event) {}

    @Override
    public void onGuildUpdateBanner(@Nonnull GuildUpdateBannerEvent event) {}

    @Override
    public void onGuildUpdateDescription(@Nonnull GuildUpdateDescriptionEvent event) {}

    @Override
    public void onGuildUpdateBoostTier(@Nonnull GuildUpdateBoostTierEvent event) {}

    @Override
    public void onGuildUpdateBoostCount(@Nonnull GuildUpdateBoostCountEvent event) {}

    @Override
    public void onGuildUpdateMaxMembers(@Nonnull GuildUpdateMaxMembersEvent event) {}

    @Override
    public void onGuildUpdateMaxPresences(@Nonnull GuildUpdateMaxPresencesEvent event) {}

    @Override
    public void onGuildUpdateNSFWLevel(@Nonnull GuildUpdateNSFWLevelEvent event) {}

    //Scheduled Event Events

    @Override
    public void onScheduledEventUpdateDescription(@Nonnull ScheduledEventUpdateDescriptionEvent event) {}

    @Override
    public void onScheduledEventUpdateEndTime(@Nonnull ScheduledEventUpdateEndTimeEvent event) {}

    @Override
    public void onScheduledEventUpdateLocation(@Nonnull ScheduledEventUpdateLocationEvent event) {}

    @Override
    public void onScheduledEventUpdateName(@Nonnull ScheduledEventUpdateNameEvent event) {}

    @Override
    public void onScheduledEventUpdateStartTime(@Nonnull ScheduledEventUpdateStartTimeEvent event) {}

    @Override
    public void onScheduledEventUpdateStatus(@Nonnull ScheduledEventUpdateStatusEvent event) {}


    @Override
    public void onScheduledEventCreate(@Nonnull ScheduledEventCreateEvent event) {}

    @Override
    public void onScheduledEventDelete(@Nonnull ScheduledEventDeleteEvent event) {}

    @Override
    public void onScheduledEventUserAdd(@Nonnull ScheduledEventUserAddEvent event) {}

    @Override
    public void onScheduledEventUserRemove(@Nonnull ScheduledEventUserRemoveEvent event) {}

    //Guild Invite Events

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {}

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {}

    //Guild Member Events

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {}

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {}

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {}

    //Guild Member Update Events

    @Override
    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {}

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {}

    @Override
    public void onGuildMemberUpdateAvatar(@Nonnull GuildMemberUpdateAvatarEvent event) {}

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {}

    @Override
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {}

    @Override
    public void onGuildMemberUpdateFlags(@Nonnull GuildMemberUpdateFlagsEvent event) {}

    @Override
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {}

    //Guild Voice Events

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {}

    @Override
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {}

    @Override
    public void onGuildVoiceDeafen(@Nonnull GuildVoiceDeafenEvent event) {}

    @Override
    public void onGuildVoiceGuildMute(@Nonnull GuildVoiceGuildMuteEvent event) {}

    @Override
    public void onGuildVoiceGuildDeafen(@Nonnull GuildVoiceGuildDeafenEvent event) {}

    @Override
    public void onGuildVoiceSelfMute(@Nonnull GuildVoiceSelfMuteEvent event) {}

    @Override
    public void onGuildVoiceSelfDeafen(@Nonnull GuildVoiceSelfDeafenEvent event) {}

    @Override
    public void onGuildVoiceSuppress(@Nonnull GuildVoiceSuppressEvent event) {}

    @Override
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {}

    @Override
    public void onGuildVoiceVideo(@Nonnull GuildVoiceVideoEvent event) {}

    @Override
    public void onGuildVoiceRequestToSpeak(@Nonnull GuildVoiceRequestToSpeakEvent event) {}

    //Role events

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {}

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {}

    //Role Update Events

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {}

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {}

    @Override
    public void onRoleUpdateIcon(@Nonnull RoleUpdateIconEvent event) {}

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {}

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {}

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {}

    @Override
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {}

    //Emoji Events

    @Override
    public void onEmojiAdded(@Nonnull EmojiAddedEvent event) {}

    @Override
    public void onEmojiRemoved(@Nonnull EmojiRemovedEvent event) {}

    //Emoji Update Events

    @Override
    public void onEmojiUpdateName(@Nonnull EmojiUpdateNameEvent event) {}

    @Override
    public void onEmojiUpdateRoles(@Nonnull EmojiUpdateRolesEvent event) {}

    // Application command permission update events

    @Override
    public void onGenericPrivilegeUpdate(@Nonnull GenericPrivilegeUpdateEvent event) {}

    @Override
    public void onApplicationCommandUpdatePrivileges(@Nonnull ApplicationCommandUpdatePrivilegesEvent event) {}

    @Override
    public void onApplicationUpdatePrivileges(@Nonnull ApplicationUpdatePrivilegesEvent event) {}

    //Sticker Events

    @Override
    public void onGuildStickerAdded(@Nonnull GuildStickerAddedEvent event) {}

    @Override
    public void onGuildStickerRemoved(@Nonnull GuildStickerRemovedEvent event) {}

    //Sticker Update Events

    @Override
    public void onGuildStickerUpdateName(@Nonnull GuildStickerUpdateNameEvent event) {}

    @Override
    public void onGuildStickerUpdateTags(@Nonnull GuildStickerUpdateTagsEvent event) {}

    @Override
    public void onGuildStickerUpdateDescription(@Nonnull GuildStickerUpdateDescriptionEvent event) {}

    @Override
    public void onGuildStickerUpdateAvailable(@Nonnull GuildStickerUpdateAvailableEvent event) {}

    // Debug Events

    @Override
    public void onHttpRequest(@Nonnull HttpRequestEvent event) {}

    //Generic Events

    @Override
    public void onGenericSessionEvent(@Nonnull GenericSessionEvent event) {}

    @Override
    public void onGenericInteractionCreate(@Nonnull GenericInteractionCreateEvent event) {}

    @Override
    public void onGenericAutoCompleteInteraction(@Nonnull GenericAutoCompleteInteractionEvent event) {}

    @Override
    public void onGenericComponentInteractionCreate(@Nonnull GenericComponentInteractionCreateEvent event) {}

    @Override
    public void onGenericCommandInteraction(@Nonnull GenericCommandInteractionEvent event) {}

    @Override
    public void onGenericContextInteraction(@Nonnull GenericContextInteractionEvent<?> event) {}

    @Override
    public void onGenericSelectMenuInteraction(@Nonnull GenericSelectMenuInteractionEvent event) {}

    @Override
    public void onGenericMessage(@Nonnull GenericMessageEvent event) {}

    @Override
    public void onGenericMessageReaction(@Nonnull GenericMessageReactionEvent event) {}

    @Override
    public void onGenericUser(@Nonnull GenericUserEvent event) {}

    @Override
    public void onGenericUserPresence(@Nonnull GenericUserPresenceEvent event) {}

    @Override
    public void onGenericSelfUpdate(@Nonnull GenericSelfUpdateEvent event) {}

    @Override
    public void onGenericStageInstance(@Nonnull GenericStageInstanceEvent event) {}

    @Override
    public void onGenericStageInstanceUpdate(@Nonnull GenericStageInstanceUpdateEvent event) {}

    @Override
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {}

    @Override
    public void onGenericChannelUpdate(@Nonnull GenericChannelUpdateEvent<?> event) {}

    @Override
    public void onGenericThread(@Nonnull GenericThreadEvent event) {}

    @Override
    public void onGenericThreadMember(@Nonnull GenericThreadMemberEvent event) {}

    @Override
    public void onGenericGuild(@Nonnull GenericGuildEvent event) {}

    @Override
    public void onGenericGuildUpdate(@Nonnull GenericGuildUpdateEvent event) {}

    @Override
    public void onGenericGuildInvite(@Nonnull GenericGuildInviteEvent event) {}

    @Override
    public void onGenericGuildMember(@Nonnull GenericGuildMemberEvent event) {}

    @Override
    public void onGenericGuildMemberUpdate(@Nonnull GenericGuildMemberUpdateEvent event) {}

    @Override
    public void onGenericGuildVoice(@Nonnull GenericGuildVoiceEvent event) {}

    @Override
    public void onGenericRole(@Nonnull GenericRoleEvent event) {}

    @Override
    public void onGenericRoleUpdate(@Nonnull GenericRoleUpdateEvent event) {}

    @Override
    public void onGenericEmoji(@Nonnull GenericEmojiEvent event) {}

    @Override
    public void onGenericEmojiUpdate(@Nonnull GenericEmojiUpdateEvent event) {}

    @Override
    public void onGenericGuildSticker(@Nonnull GenericGuildStickerEvent event) {}

    @Override
    public void onGenericGuildStickerUpdate(@Nonnull GenericGuildStickerUpdateEvent event) {}

    @Override
    public void onGenericPermissionOverride(@Nonnull GenericPermissionOverrideEvent event) {}

    @Override
    public void onGenericScheduledEventUpdate(@Nonnull GenericScheduledEventUpdateEvent event) {}

    @Override
    public void onGenericForumTag(@Nonnull GenericForumTagEvent event) {}

    @Override
    public void onGenericForumTagUpdate(@Nonnull GenericForumTagUpdateEvent event) {}
}
