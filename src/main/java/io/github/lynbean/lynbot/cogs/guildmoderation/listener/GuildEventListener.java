package io.github.lynbean.lynbot.cogs.guildmoderation.listener;

import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.findServerModeration;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getAdminCollection;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildApplicationCommandPermissionEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildChannelEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildEmojiEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildForumTagEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildInteractionEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildInviteEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildMemberEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildMessageEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildPermissionOverrideEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildRoleEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildScheduledEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildStageInstanceEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildStickerEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildThreadEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildVoiceEvent;
import static io.github.lynbean.lynbot.cogs.guildmoderation.GuildDatabaseManager.getGuildEventListenerManager;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildApplicationCommandPermissionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildChannelEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEmojiEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildForumTagEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInviteEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMemberEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMessageEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildPermissionOverrideEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildRoleEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildScheduledEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStickerEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildThreadEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildVoiceEvent;
import io.github.lynbean.lynbot.database.pojo.MessageContent;
import io.github.lynbean.lynbot.database.pojo.ServerMessage;
import io.github.lynbean.lynbot.database.pojo.UserWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member.MemberFlag;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateModeratedEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
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
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateFlagsEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventCreateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserAddEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateEndTimeEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateLocationEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStartTimeEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStatusEvent;
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
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericPrivilegeUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateHoistedEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateIconEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateMentionableEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateAvailableEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateNameEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateTagsEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege.Type;

public class GuildEventListener extends ListenerAdapter {

    private static <T extends Event> void autoFinalizedAndSend(T event, Guild guild, User user, String description, String channelId) {
        autoFinalizedAndSend(event, guild, user, description, channelId, null, null);
    }

    private static <T extends Event> void autoFinalizedAndSend(T event, Guild guild, User user, String description, String channelId, String thumbnailUrl, String imageUrl) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(event.getClass().getSimpleName());

        if (guild != null)
            embed.setFooter(guild.getName(), guild.getIconUrl());
        if (user != null)
            embed.setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (description != null)
            embed.setDescription(description);
        if (thumbnailUrl != null)
            embed.setThumbnail(thumbnailUrl);
        if (imageUrl != null)
            embed.setImage(imageUrl);

        try {
            ((GuildMessageChannel) event.getJDA()
                .getGuildChannelById(channelId))
                .sendMessageEmbeds(embed.build())
                .queue();

        } catch (Exception e) {
            // ignored
        }
    }

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

    //Message Events
    private List<MessageContent> getMessageContent(String messageId) {
        ServerMessage document = Bot.getMongoManager().findServerMessage(messageId);

        if (document == null)
            return null;

        List<MessageContent> content = document.getContent();

        if (content.isEmpty())
            return null;

        content.sort(Comparator.comparing(MessageContent::getTimeCreated, Comparator.reverseOrder()));
        return content;
    }

    private String getMessageContentBeforeEdited(String messageId) {
        List<MessageContent> content = getMessageContent(messageId);
        if (content == null) return null;
        return content.size() >= 2
            ? content.get(1).getRaw()
            : content.get(0).getRaw();
    }

    private String getLastMessageContent(String messageId) {
        List<MessageContent> content = getMessageContent(messageId);
        if (content == null) return null;
        return content.size() >= 1
            ? content.get(0).getRaw()
            : null;
    }

    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isUpdateEvent()) return;

        String messageContentBeforeEdited = getMessageContentBeforeEdited(event.getMessageId());

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getAuthor(),
            "%s\n\n%s\n\n%s".formatted(
                event.getMessage().getJumpUrl(),
                messageContentBeforeEdited == null
                    ? "Old message content is not available."
                    : "**Before:**\n%s".formatted(messageContentBeforeEdited),
                "**After:**\n%s".formatted(event.getMessage().getContentRaw())
            ),
            guildMessageEvent.getLogChannelId()
        );
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isDeleteEvent()) return;

        UserWrapper user = Bot.getMongoManager().getMessageAuthor(event.getMessageId());
        String messageContent = getLastMessageContent(event.getMessageId());
        String description = messageContent == null
            ? "**The message content is not available.**"
            : "**Message:**\n%s".formatted(messageContent);

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            user == null ? null : event.getJDA().getUserById(user.getId()),
            description,
            guildMessageEvent.getLogChannelId()
        );
    }

    @Override
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {
        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isBulkDeleteEvent()) return;
        String logChannelId = guildMessageEvent.getLogChannelId();

        event.getMessageIds().forEach(
            messageId -> {
                UserWrapper user = Bot.getMongoManager().getMessageAuthor(messageId);
                String messageContent = getLastMessageContent(messageId);
                String description = messageContent == null
                    ? "**The message content is not available.**"
                    : "**Message:**\n%s".formatted(messageContent);

                autoFinalizedAndSend(
                    event,
                    event.getGuild(),
                    user == null ? null : event.getJDA().getUserById(user.getId()),
                    description,
                    logChannelId
                );
            }
        );
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot() || event.getMember().getUser().isSystem())
            return;

        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isReactionAddEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "%s\nReaction added: %s".formatted(
                event.getJumpUrl(),
                event.getReaction().getEmoji().getAsReactionCode()
            ),
            guildMessageEvent.getLogChannelId()
        );
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot() || event.getMember().getUser().isSystem())
            return;

        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isReactionRemoveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "%s\nReaction removed: %s".formatted(
                event.getJumpUrl(),
                event.getReaction().getEmoji().getAsReactionCode()
            ),
            guildMessageEvent.getLogChannelId()
        );
    }

    @Override
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {
        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isReactionRemoveAllEvent()) return;

        ServerMessage message = Bot.getMongoManager().findServerMessage(event.getMessageId());

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            message == null ? null : event.getJDA().getUserById(message.getAuthorId()),
            "%s\n**All reactions have been removed.**".formatted(event.getJumpUrl()),
            guildMessageEvent.getLogChannelId()
        );
    }

    @Override
    public void onMessageReactionRemoveEmoji(@Nonnull MessageReactionRemoveEmojiEvent event) {
        GuildMessageEvent guildMessageEvent = getGuildMessageEvent(event.getGuild());
        if (!guildMessageEvent.isReactionRemoveEmojiEvent()) return;

        ServerMessage message = Bot.getMongoManager().findServerMessage(event.getMessageId());

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            message == null ? null : event.getJDA().getUserById(message.getAuthorId()),
            "%s\n**Emoji removed: **%s".formatted(
                event.getJumpUrl(),
                event.getReaction().getEmoji().getAsReactionCode()
            ),
            guildMessageEvent.getLogChannelId()
        );
    }

    //PermissionOverride Events

    @Override
    public void onPermissionOverrideDelete(@Nonnull PermissionOverrideDeleteEvent event) {
        GuildPermissionOverrideEvent guildPermissionOverrideEvent = getGuildPermissionOverrideEvent(event.getGuild());
        if (!guildPermissionOverrideEvent.isDeleteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember() == null ? null : event.getMember().getUser(),
            "%s\n%s\n\n**__%s__**\n\n**Denied:**\n%s\n\n**Allowed:**\n%s".formatted(
                event.getPermissionOverride().getChannel().getAsMention(),
                event.getPermissionOverride().getRole() == null ? "" : event.getPermissionOverride().getRole().getAsMention(),
                event.isMemberOverride() ? "Member Overriden" : "Role Overriden",
                event.getPermissionOverride().getDenied().toString(),
                event.getPermissionOverride().getAllowed().toString()
            ),
            guildPermissionOverrideEvent.getLogChannelId()
        );
    }

    @Override
    public void onPermissionOverrideUpdate(@Nonnull PermissionOverrideUpdateEvent event) {
        GuildPermissionOverrideEvent guildPermissionOverrideEvent = getGuildPermissionOverrideEvent(event.getGuild());
        if (!guildPermissionOverrideEvent.isUpdateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember() == null ? null : event.getMember().getUser(),
            "%s\n%s\n\n**__%s__**\n\n**__Old__**\n**Denied:**\n%s\n\n**Allowed:**\n%s\n\n**__New__**\n**Denied:**\n%s\n\n**Allowed:**\n%s".formatted(
                event.getPermissionOverride().getChannel().getAsMention(),
                event.getPermissionOverride().getRole() == null ? "" : event.getPermissionOverride().getRole().getAsMention(),
                event.isMemberOverride() ? "Member Overriden" : "Role Overriden",
                event.getOldDeny().toString(),
                event.getOldAllow().toString(),
                event.getPermissionOverride().getDenied().toString(),
                event.getPermissionOverride().getAllowed().toString()
            ),
            guildPermissionOverrideEvent.getLogChannelId()
        );
    }

    @Override
    public void onPermissionOverrideCreate(@Nonnull PermissionOverrideCreateEvent event) {
        GuildPermissionOverrideEvent guildPermissionOverrideEvent = getGuildPermissionOverrideEvent(event.getGuild());
        if (!guildPermissionOverrideEvent.isCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember() == null ? null : event.getMember().getUser(),
            "%s\n%s\n\n**__%s__**\n\n**Denied:**\n%s\n\n**Allowed:**\n%s".formatted(
                event.getPermissionOverride().getChannel().getAsMention(),
                event.getPermissionOverride().getRole() == null ? "" : event.getPermissionOverride().getRole().getAsMention(),
                event.isMemberOverride() ? "Member Overriden" : "Role Overriden",
                event.getPermissionOverride().getDenied().toString(),
                event.getPermissionOverride().getAllowed().toString()
            ),
            guildPermissionOverrideEvent.getLogChannelId()
        );
    }

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
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__%s__**\n\n%s".formatted(
                event.getChannel().getType().name(),
                event.getChannel().getAsMention()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isDeleteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__%s__**\n\n%s".formatted(
                event.getChannel().getType().name(),
                event.getChannel().getAsMention()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    //Channel Update Events

    @Override
    public void onChannelUpdateBitrate(@Nonnull ChannelUpdateBitrateEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateBitrateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateName(@Nonnull ChannelUpdateNameEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateFlags(@Nonnull ChannelUpdateFlagsEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateFlagsEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateNSFW(@Nonnull ChannelUpdateNSFWEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateNSFWEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateParent(@Nonnull ChannelUpdateParentEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateParentEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Parent is not available." : event.getOldValue().getAsMention(),
                event.getNewValue() == null ? "New Parent is not available." : event.getNewValue().getAsMention()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdatePosition(@Nonnull ChannelUpdatePositionEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdatePositionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateRegion(@Nonnull ChannelUpdateRegionEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateRegionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateSlowmode(@Nonnull ChannelUpdateSlowmodeEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateSlowmodeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateDefaultThreadSlowmode(@Nonnull ChannelUpdateDefaultThreadSlowmodeEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateDefaultThreadSlowmodeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateDefaultReaction(@Nonnull ChannelUpdateDefaultReactionEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateDefaultReactionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Default Reaction is not available." : event.getOldValue().getAsReactionCode(),
                event.getNewValue() == null ? "New Default Reaction is not available." : event.getNewValue().getAsReactionCode()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateDefaultLayout(@Nonnull ChannelUpdateDefaultLayoutEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateDefaultLayoutEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateTopic(@Nonnull ChannelUpdateTopicEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateTopicEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue(),
                event.getNewValue()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateType(@Nonnull ChannelUpdateTypeEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateTypeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Channel Type is not available." : event.getOldValue().name(),
                event.getNewValue() == null ? "New Channel Type is not available." : event.getNewValue().name()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateUserLimit(@Nonnull ChannelUpdateUserLimitEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateUserLimitEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old User Limit is not available." : event.getOldValue().toString(),
                event.getNewValue() == null ? "New User Limit is not available." : event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateArchived(@Nonnull ChannelUpdateArchivedEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateArchivedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Archived is not available." : event.getOldValue().toString(),
                event.getNewValue() == null ? "New Archived is not available." : event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateArchiveTimestamp(@Nonnull ChannelUpdateArchiveTimestampEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateArchiveTimestampEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue().toString(),
                event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateAutoArchiveDuration(@Nonnull ChannelUpdateAutoArchiveDurationEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateAutoArchiveDurationEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s minutes\n\n**__New__**\n%s minutes".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Auto Archive Duration is not available." : event.getOldValue().getMinutes(),
                event.getNewValue() == null ? "New Auto Archive Duration is not available." : event.getNewValue().getMinutes()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateLocked(@Nonnull ChannelUpdateLockedEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateLockedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Locked is not available." : event.getOldValue().toString(),
                event.getNewValue() == null ? "New Locked is not available." : event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateInvitable(@Nonnull ChannelUpdateInvitableEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateInvitableEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Invitable is not available." : event.getOldValue().toString(),
                event.getNewValue() == null ? "New Invitable is not available." : event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    @Override
    public void onChannelUpdateAppliedTags(@Nonnull ChannelUpdateAppliedTagsEvent event) {
        GuildChannelEvent guildChannelEvent = getGuildChannelEvent(event.getGuild());
        if (!guildChannelEvent.isUpdateAppliedTagsEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue().isEmpty() ? "Old Applied Tags is not available." : event.getOldValue().toString(),
                event.getNewValue().isEmpty() ? "New Applied Tags is not available." : event.getNewValue().toString()
            ),
            guildChannelEvent.getLogChannelId()
        );
    }

    //Forum Tag Events

    @Override
    public void onForumTagAdd(@Nonnull ForumTagAddEvent event) {
        GuildForumTagEvent guildForumTagEvent = getGuildForumTagEvent(event.getChannel().getGuild());
        if (!guildForumTagEvent.isAddEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getChannel().getGuild(),
            null,
            "%s\n\n**__Tag__**\n%s\n\n**__Name__**\n%s\n\n**__Position__**\n%s\n\n" +
                "**__Can only be applied by moderators with the MANAGE_THREADS permission__**\n%s".formatted(
                    event.getChannel().getAsMention(),
                    event.getTag().getEmoji() == null ? "Tag Emoji is not available." : event.getTag().getEmoji().getAsReactionCode(),
                    event.getTag().getName(),
                    event.getTag().getPosition(),
                    event.getTag().isModerated()
            ),
            guildForumTagEvent.getLogChannelId()
        );
    }

    @Override
    public void onForumTagRemove(@Nonnull ForumTagRemoveEvent event) {
        GuildForumTagEvent guildForumTagEvent = getGuildForumTagEvent(event.getChannel().getGuild());
        if (!guildForumTagEvent.isRemoveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getChannel().getGuild(),
            null,
            "%s\n\n**__Tag__**\n%s\n\n**__Name__**\n%s\n\n**__Position__**\n%s\n\n" +
                "**__Can only be applied by moderators with the MANAGE_THREADS permission__**\n%s".formatted(
                    event.getChannel().getAsMention(),
                    event.getTag().getEmoji() == null ? "Tag Emoji is not available." : event.getTag().getEmoji().getAsReactionCode(),
                    event.getTag().getName(),
                    event.getTag().getPosition(),
                    event.getTag().isModerated()
            ),
            guildForumTagEvent.getLogChannelId()
        );
    }

    @Override
    public void onForumTagUpdateName(@Nonnull ForumTagUpdateNameEvent event) {
        GuildForumTagEvent guildForumTagEvent = getGuildForumTagEvent(event.getChannel().getGuild());
        if (!guildForumTagEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getChannel().getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Name is not available." : event.getOldValue(),
                event.getNewValue() == null ? "New Name is not available." : event.getNewValue()
            ),
            guildForumTagEvent.getLogChannelId()
        );
    }

    @Override
    public void onForumTagUpdateEmoji(@Nonnull ForumTagUpdateEmojiEvent event) {
        GuildForumTagEvent guildForumTagEvent = getGuildForumTagEvent(event.getChannel().getGuild());
        if (!guildForumTagEvent.isUpdateEmojiEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getChannel().getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() == null ? "Old Emoji is not available." : event.getOldValue().getAsReactionCode(),
                event.getNewValue() == null ? "New Emoji is not available." : event.getNewValue().getAsReactionCode()
            ),
            guildForumTagEvent.getLogChannelId()
        );
    }

    @Override
    public void onForumTagUpdateModerated(@Nonnull ForumTagUpdateModeratedEvent event) {
        GuildForumTagEvent guildForumTagEvent = getGuildForumTagEvent(event.getChannel().getGuild());
        if (!guildForumTagEvent.isUpdateModeratedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getChannel().getGuild(),
            null,
            "%s\n\n**__Old__**\n%s\n\n**__New__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getOldValue() ? "Old Moderated is true." : "Old Moderated is false.",
                event.getNewValue() ? "New Moderated is true." : "New Moderated is false."
            ),
            guildForumTagEvent.getLogChannelId()
        );
    }

    //Thread Events

    @Override
    public void onThreadRevealed(@Nonnull ThreadRevealedEvent event) {
        GuildThreadEvent guildThreadEvent = getGuildThreadEvent(event.getThread().getGuild());
        if (!guildThreadEvent.isRevealedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getThread().getGuild(),
            event.getThread().getOwner() == null ? null : event.getThread().getOwner().getUser(),
            event.getThread().getAsMention(),
            guildThreadEvent.getLogChannelId()
        );
    }

    @Override
    public void onThreadHidden(@Nonnull ThreadHiddenEvent event) {
        GuildThreadEvent guildThreadEvent = getGuildThreadEvent(event.getThread().getGuild());
        if (!guildThreadEvent.isHiddenEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getThread().getGuild(),
            event.getThread().getOwner() == null ? null : event.getThread().getOwner().getUser(),
            event.getThread().getAsMention(),
            guildThreadEvent.getLogChannelId()
        );
    }

    //Thread Member Events

    @Override
    public void onThreadMemberJoin(@Nonnull ThreadMemberJoinEvent event) {
        GuildThreadEvent guildThreadEvent = getGuildThreadEvent(event.getThread().getGuild());
        if (!guildThreadEvent.isMemberJoinEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getThread().getGuild(),
            event.getMember().getUser(),
            event.getThread().getAsMention(),
            guildThreadEvent.getLogChannelId()
        );
    }

    @Override
    public void onThreadMemberLeave(@Nonnull ThreadMemberLeaveEvent event) {
        GuildThreadEvent guildThreadEvent = getGuildThreadEvent(event.getThread().getGuild());
        if (!guildThreadEvent.isMemberLeaveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getThread().getGuild(),
            event.getMember().getUser(),
            event.getThread().getAsMention(),
            guildThreadEvent.getLogChannelId()
        );
    }

    //Guild Events

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isBanEvent()) return;

        String reason = event.getGuild().retrieveBan(event.getUser()).complete().getReason();

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "%s\n\n**ID**\n%s\n\n**__Reason__**\n%s".formatted(
                event.getUser().getAsMention(),
                event.getUser().getId(),
                reason == null ? "Reason is not available." : reason
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUnbanEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "%s\n\n**ID**\n%s".formatted(
                event.getUser().getAsMention(),
                event.getUser().getId()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isAuditLogEntryCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getEntry().getUser(),
            "**__Action Type__**\n%s\n\n**__Action Target__**\n%s\n\n**__Reason__**\n%s\n\n**__Changes__**\n%s\n\n**__Options__**\n%s".formatted(
                event.getEntry().getType().name(),
                event.getEntry().getTargetType().name(),
                event.getEntry().getReason(),
                event.getEntry().getChanges().isEmpty() ? "No Changes." : event.getEntry().getChanges().toString(),
                event.getEntry().getOptions().isEmpty() ? "No Options." : event.getEntry().getOptions().toString()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isMemberRemoveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "%s\n\n**ID**\n%s".formatted(
                event.getUser().getAsMention(),
                event.getUser().getId()
            ),
            guildEvent.getLogChannelId()
        );
    }

    //Guild Update Events

    @Override
    public void onGuildUpdateAfkChannel(@Nonnull GuildUpdateAfkChannelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateAfkChannelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Afk Channel__**\n%s\n\n**__New Afk Channel__**\n%s".formatted(
                event.getOldAfkChannel() == null ? "Old Afk Channel is not available." : event.getOldAfkChannel().getAsMention(),
                event.getNewAfkChannel() == null ? "New Afk Channel is not available." : event.getNewAfkChannel().getAsMention()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateSystemChannel(@Nonnull GuildUpdateSystemChannelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateSystemChannelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old System Channel__**\n%s\n\n**__New System Channel__**\n%s".formatted(
                event.getOldSystemChannel() == null ? "Old System Channel is not available." : event.getOldSystemChannel().getAsMention(),
                event.getNewSystemChannel() == null ? "New System Channel is not available." : event.getNewSystemChannel().getAsMention()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateRulesChannel(@Nonnull GuildUpdateRulesChannelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateRulesChannelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Rules Channel__**\n%s\n\n**__New Rules Channel__**\n%s".formatted(
                event.getOldRulesChannel() == null ? "Old Rules Channel is not available." : event.getOldRulesChannel().getAsMention(),
                event.getNewRulesChannel() == null ? "New Rules Channel is not available." : event.getNewRulesChannel().getAsMention()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateCommunityUpdatesChannel(@Nonnull GuildUpdateCommunityUpdatesChannelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateCommunityUpdatesChannelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Community Updates Channel__**\n%s\n\n**__New Community Updates Channel__**\n%s".formatted(
                event.getOldCommunityUpdatesChannel() == null ? "Old Community Updates Channel is not available." : event.getOldCommunityUpdatesChannel().getAsMention(),
                event.getNewCommunityUpdatesChannel() == null ? "New Community Updates Channel is not available." : event.getNewCommunityUpdatesChannel().getAsMention()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateAfkTimeout(@Nonnull GuildUpdateAfkTimeoutEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateAfkTimeoutEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Afk Timeout__**\n%s\n\n**__New Afk Timeout__**\n%s".formatted(
                event.getOldAfkTimeout().getSeconds() + " Seconds",
                event.getNewAfkTimeout().getSeconds() + " Seconds"
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateExplicitContentLevel(@Nonnull GuildUpdateExplicitContentLevelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateExplicitContentLevelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Explicit Content Level__**\n%s\n\n**__New Explicit Content Level__**\n%s".formatted(
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateIcon(@Nonnull GuildUpdateIconEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateIconEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Icon__**\n%s\n\n**__New Icon__**\n%s".formatted(
                event.getOldIconUrl() == null ? "Old Icon is not available." : event.getOldIconUrl(),
                event.getNewIconUrl() == null ? "New Icon is not available." : event.getNewIconUrl()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateMFALevel(@Nonnull GuildUpdateMFALevelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateMFALevelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old MFA Level__**\n%s\n\n**__New MFA Level__**\n%s".formatted(
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event){
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Name__**\n%s\n\n**__New Name__**\n%s".formatted(
                event.getOldName(),
                event.getNewName()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateNotificationLevel(@Nonnull GuildUpdateNotificationLevelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateNotificationLevelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Notification Level__**\n%s\n\n**__New Notification Level__**\n%s".formatted(
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateOwner(@Nonnull GuildUpdateOwnerEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateOwnerEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Owner__**\n%s\n\n**__New Owner__**\n%s".formatted(
                event.getOldOwner() == null ? "Old Owner is not available." : event.getOldOwner().getAsMention(),
                event.getNewOwner() == null ? "New Owner is not available." : event.getNewOwner().getAsMention()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateSplash(@Nonnull GuildUpdateSplashEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateSplashEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Splash__**\n%s\n\n**__New Splash__**\n%s".formatted(
                event.getOldSplashUrl() == null ? "Old Splash is not available." : event.getOldSplashUrl(),
                event.getNewSplashUrl() == null ? "New Splash is not available." : event.getNewSplashUrl()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateVerificationLevel(@Nonnull GuildUpdateVerificationLevelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateVerificationLevelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Verification Level__**\n%s\n\n**__New Verification Level__**\n%s".formatted(
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateLocale(@Nonnull GuildUpdateLocaleEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateLocaleEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Locale__**\n%s\n\n**__New Locale__**\n%s".formatted(
                event.getOldValue().name(),
                event.getNewValue().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateFeatures(@Nonnull GuildUpdateFeaturesEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateFeaturesEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Features__**\n%s\n\n**__New Features__**\n%s".formatted(
                event.getOldFeatures().isEmpty() ? "Old Features is not available." : event.getOldFeatures().toString(),
                event.getNewFeatures().isEmpty() ? "New Features is not available." : event.getNewFeatures().toString()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateVanityCode(@Nonnull GuildUpdateVanityCodeEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateVanityCodeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Vanity Code__**\n%s\n\n**__New Vanity Code__**\n%s".formatted(
                event.getOldVanityCode() == null ? "Old Vanity Code is not available." : event.getOldVanityCode(),
                event.getNewVanityCode() == null ? "New Vanity Code is not available." : event.getNewVanityCode()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateBanner(@Nonnull GuildUpdateBannerEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateBannerEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Banner__**\n%s\n\n**__New Banner__**\n%s".formatted(
                event.getOldBannerUrl() == null ? "Old Banner is not available." : event.getOldBannerUrl(),
                event.getNewBannerUrl() == null ? "New Banner is not available." : event.getNewBannerUrl()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateDescription(@Nonnull GuildUpdateDescriptionEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateDescriptionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Description__**\n%s\n\n**__New Description__**\n%s".formatted(
                event.getOldDescription() == null ? "Old Description is not available." : event.getOldDescription(),
                event.getNewDescription() == null ? "New Description is not available." : event.getNewDescription()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateBoostTier(@Nonnull GuildUpdateBoostTierEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateBoostTierEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Boost Tier__**\n%s\n\n**__New Boost Tier__**\n%s".formatted(
                event.getOldBoostTier().name(),
                event.getNewBoostTier().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateBoostCount(@Nonnull GuildUpdateBoostCountEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateBoostCountEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Boost Count__**\n%s\n\n**__New Boost Count__**\n%s".formatted(
                event.getOldBoostCount(),
                event.getNewBoostCount()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateMaxMembers(@Nonnull GuildUpdateMaxMembersEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateMaxMembersEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Max Members__**\n%s\n\n**__New Max Members__**\n%s".formatted(
                event.getOldMaxMembers(),
                event.getNewMaxMembers()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateMaxPresences(@Nonnull GuildUpdateMaxPresencesEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateMaxPresencesEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old Max Presences__**\n%s\n\n**__New Max Presences__**\n%s".formatted(
                event.getOldMaxPresences(),
                event.getNewMaxPresences()
            ),
            guildEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildUpdateNSFWLevel(@Nonnull GuildUpdateNSFWLevelEvent event) {
        GuildEvent guildEvent = getGuildEvent(event.getGuild());
        if (!guildEvent.isUpdateNSFWLevelEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Old NSFW Level__**\n%s\n\n**__New NSFW Level__**\n%s".formatted(
                event.getOldNSFWLevel().name(),
                event.getNewNSFWLevel().name()
            ),
            guildEvent.getLogChannelId()
        );
    }

    //Scheduled Event Events

    @Override
    public void onScheduledEventUpdateDescription(@Nonnull ScheduledEventUpdateDescriptionEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateDescriptionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old Description__**\n%s\n\n**__New Description__**\n%s".formatted(
                event.getOldDescription() == null ? "Old Description is not available." : event.getOldDescription(),
                event.getNewDescription() == null ? "New Description is not available." : event.getNewDescription()
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUpdateEndTime(@Nonnull ScheduledEventUpdateEndTimeEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateEndTimeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old End Time__**\n%s\n\n**__New End Time__**\n%s".formatted(
                event.getOldEndTime() == null
                    ? "Old End Time is not available."
                    : event.getOldEndTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getNewEndTime() == null
                    ? "New End Time is not available."
                    : event.getNewEndTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss"))
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUpdateLocation(@Nonnull ScheduledEventUpdateLocationEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateLocationEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old Location__**\n%s\n\n**__New Location__**\n%s".formatted(
                event.getOldLocation() == null ? "Old Location is not available." : event.getOldLocation(),
                event.getNewLocation() == null ? "New Location is not available." : event.getNewLocation()
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUpdateName(@Nonnull ScheduledEventUpdateNameEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old Name__**\n%s\n\n**__New Name__**\n%s".formatted(
                event.getOldName() == null ? "Old Name is not available." : event.getOldName(),
                event.getNewName() == null ? "New Name is not available." : event.getNewName()
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUpdateStartTime(@Nonnull ScheduledEventUpdateStartTimeEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateStartTimeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old Start Time__**\n%s\n\n**__New Start Time__**\n%s".formatted(
                event.getOldStartTime() == null
                    ? "Old Start Time is not available."
                    : event.getOldStartTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getNewStartTime() == null
                    ? "New Start Time is not available."
                    : event.getNewStartTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss"))
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUpdateStatus(@Nonnull ScheduledEventUpdateStatusEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUpdateStatusEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Old Status__**\n%s\n\n**__New Status__**\n%s".formatted(
                event.getOldStatus().name(),
                event.getNewStatus().name()
            ),
            guildScheduledEvent.getLogChannelId()
        );
    }


    @Override
    public void onScheduledEventCreate(@Nonnull ScheduledEventCreateEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Name__**\n%s\n\n**__Description__**\n%s\n\n**__Location__**\n%s\n\n**__Start Time__**\n%s\n\n**__End Time__**\n%s\n\n**__Status__**\n%s".formatted(
                event.getScheduledEvent().getName(),
                event.getScheduledEvent().getDescription(),
                event.getScheduledEvent().getLocation(),
                event.getScheduledEvent().getStartTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getScheduledEvent().getEndTime() == null
                    ? "End Time is not available."
                    : event.getScheduledEvent().getEndTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getScheduledEvent().getStatus().name()
            ),
            guildScheduledEvent.getLogChannelId(),
            null,
            event.getScheduledEvent().getImageUrl()
        );
    }

    @Override
    public void onScheduledEventDelete(@Nonnull ScheduledEventDeleteEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isDeleteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getScheduledEvent().getCreator(),
            "**__Name__**\n%s\n\n**__Description__**\n%s\n\n**__Location__**\n%s\n\n**__Start Time__**\n%s\n\n**__End Time__**\n%s\n\n**__Status__**\n%s".formatted(
                event.getScheduledEvent().getName(),
                event.getScheduledEvent().getDescription(),
                event.getScheduledEvent().getLocation(),
                event.getScheduledEvent().getStartTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getScheduledEvent().getEndTime() == null
                    ? "End Time is not available."
                    : event.getScheduledEvent().getEndTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getScheduledEvent().getStatus().name()
            ),
            guildScheduledEvent.getLogChannelId(),
            null,
            event.getScheduledEvent().getImageUrl()
        );
    }

    @Override
    public void onScheduledEventUserAdd(@Nonnull ScheduledEventUserAddEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUserAddEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            null,
            guildScheduledEvent.getLogChannelId()
        );
    }

    @Override
    public void onScheduledEventUserRemove(@Nonnull ScheduledEventUserRemoveEvent event) {
        GuildScheduledEvent guildScheduledEvent = getGuildScheduledEvent(event.getGuild());
        if (!guildScheduledEvent.isUserRemoveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            null,
            guildScheduledEvent.getLogChannelId()
        );
    }

    //Guild Invite Events

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {
        GuildInviteEvent guildInviteEvent = getGuildInviteEvent(event.getGuild());
        if (!guildInviteEvent.isCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getInvite().getInviter(),
            "**__Channel__**\n%s\n\n**__Code__**\n%s\n\n**__Max Age__**\n%s\n\n**__Max Uses__**\n%s\n\n**__Temporary__**\n%s\n\n**__URL__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getInvite().getCode(),
                event.getInvite().getMaxAge() == 0
                    ? "No Limit"
                    : event.getInvite().getMaxAge() + " seconds",
                event.getInvite().getMaxUses() == 0
                    ? "No Limit"
                    : event.getInvite().getMaxUses() + " uses",
                event.getInvite().isTemporary() ? "Yes" : "No",
                event.getInvite().getUrl()
            ),
            guildInviteEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        GuildInviteEvent guildInviteEvent = getGuildInviteEvent(event.getGuild());
        if (!guildInviteEvent.isDeleteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Code__**\n%s\n\n**__URL__**\n%s".formatted(
                event.getChannel().getAsMention(),
                event.getUrl()
            ),
            guildInviteEvent.getLogChannelId()
        );
    }

    //Guild Member Events

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isJoinEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            null,
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isRoleAddEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Roles__**\n%s".formatted(
                event.getRoles().stream()
                    .map(Role::getAsMention)
                    .collect(Collectors.joining("\n"))
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isRoleRemoveEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Roles__**\n%s".formatted(
                event.getRoles().stream()
                    .map(Role::getAsMention)
                    .collect(Collectors.joining("\n"))
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    //Guild Member Update Events

    @Override
    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            null,
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateNicknameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Nickname__**\n%s\n\n**__Old Nickname__**\n%s\n\n**__New Nickname__**\n%s".formatted(
                event.getOldNickname() == null ? "Old Nickname is not available" : event.getOldNickname(),
                event.getNewNickname() == null ? "New Nickname is not available" : event.getNewNickname()
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdateAvatar(@Nonnull GuildMemberUpdateAvatarEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateAvatarEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Avatar__**\n%s\n\n**__Old Avatar__**\n%s\n\n**__New Avatar__**\n%s".formatted(
                event.getOldAvatarUrl() == null ? "Old Avatar is not available" : "[Old Avatar](%s)".formatted(event.getOldAvatarUrl()),
                event.getNewAvatarUrl() == null ? "New Avatar is not available" : "[New Avatar](%s)".formatted(event.getNewAvatarUrl())
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateBoostTimeEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Boost Time__**\nOld: %s\nNew: %s".formatted(
                event.getOldTimeBoosted() == null
                    ? "None"
                    : event.getOldTimeBoosted().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getNewTimeBoosted() == null
                    ? "None"
                    : event.getNewTimeBoosted().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss"))
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdatePendingEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Pending__**\nOld: %s\nNew: %s".formatted(
                event.getOldPending() ? "Yes" : "No",
                event.getNewPending() ? "Yes" : "No"
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdateFlags(@Nonnull GuildMemberUpdateFlagsEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateFlagsEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Flags__**\nOld: %s\nNew: %s".formatted(
                event.getOldValue().isEmpty()
                    ? "None"
                    : event.getOldValue().stream()
                        .map(MemberFlag::name)
                        .collect(Collectors.joining("\n")),
                event.getNewValue().isEmpty()
                    ? "None"
                    : event.getNewValue().stream()
                        .map(MemberFlag::name)
                        .collect(Collectors.joining("\n"))
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {
        GuildMemberEvent guildMemberEvent = getGuildMemberEvent(event.getGuild());
        if (!guildMemberEvent.isUpdateTimeOutEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getUser(),
            "**__Time Out__**\nOld: %s\nNew: %s".formatted(
                event.getOldValue() == null
                    ? "None"
                    : event.getOldValue().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getNewValue() == null
                    ? "None"
                    : event.getNewValue().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss"))
            ),
            guildMemberEvent.getLogChannelId()
        );
    }

    //Guild Voice Events

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isUpdateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getEntity().getUser(),
            "**__Voice Updaate__**\n**__Left__**\n%s\n\n**__Joined__**\n%s".formatted(
                event.getChannelLeft() == null ? "None" : event.getChannelLeft().getAsMention(),
                event.getChannelJoined() == null ? "None" : event.getChannelJoined().getAsMention()
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isMuteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Mute__**\nOld: %s\nNew: %s".formatted(
                event.isMuted() ? "Is Muted" : "Not Muted",
                event.isMuted() ? "Is Muted" : "Not Muted"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceDeafen(@Nonnull GuildVoiceDeafenEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isDeafenEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Deafen__**\nOld: %s\nNew: %s".formatted(
                event.isDeafened() ? "Is Deafened" : "Not Deafened",
                event.isDeafened() ? "Is Deafened" : "Not Deafened"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceGuildMute(@Nonnull GuildVoiceGuildMuteEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isGuildMuteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Guild Mute__**\nOld: %s\nNew: %s".formatted(
                event.isGuildMuted() ? "Is Guild Muted" : "Not Guild Muted",
                event.isGuildMuted() ? "Is Guild Muted" : "Not Guild Muted"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceGuildDeafen(@Nonnull GuildVoiceGuildDeafenEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isGuildDeafenEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Guild Deafen__**\nOld: %s\nNew: %s".formatted(
                event.isGuildDeafened() ? "Is Guild Deafened" : "Not Guild Deafened",
                event.isGuildDeafened() ? "Is Guild Deafened" : "Not Guild Deafened"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceSelfMute(@Nonnull GuildVoiceSelfMuteEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isSelfMuteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Self Mute__**\nOld: %s\nNew: %s".formatted(
                event.isSelfMuted() ? "Is Self Muted" : "Not Self Muted",
                event.isSelfMuted() ? "Is Self Muted" : "Not Self Muted"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceSelfDeafen(@Nonnull GuildVoiceSelfDeafenEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isSelfDeafenEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Self Deafen__**\nOld: %s\nNew: %s".formatted(
                event.isSelfDeafened() ? "Is Self Deafened" : "Not Self Deafened",
                event.isSelfDeafened() ? "Is Self Deafened" : "Not Self Deafened"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceSuppress(@Nonnull GuildVoiceSuppressEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isSuppressEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Suppress__**\nOld: %s\nNew: %s".formatted(
                event.isSuppressed() ? "Is Suppressed" : "Not Suppressed",
                event.isSuppressed() ? "Is Suppressed" : "Not Suppressed"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isStreamEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Stream__**\nOld: %s\nNew: %s".formatted(
                event.isStream() ? "Is Stream" : "Not Stream",
                event.isStream() ? "Is Stream" : "Not Stream"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceVideo(@Nonnull GuildVoiceVideoEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isVideoEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Video__**\nOld: %s\nNew: %s".formatted(
                event.isSendingVideo() ? "Is Sending Video" : "Not Sending Video",
                event.isSendingVideo() ? "Is Sending Video" : "Not Sending Video"
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    @Override
    public void onGuildVoiceRequestToSpeak(@Nonnull GuildVoiceRequestToSpeakEvent event) {
        GuildVoiceEvent guildVoiceEvent = getGuildVoiceEvent(event.getGuild());
        if (!guildVoiceEvent.isRequestToSpeakEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getMember().getUser(),
            "**__Request To Speak__**\nBefore: %s\nCurrent: %s".formatted(
                event.getOldTime() == null
                    ? "Never Requested To Speak"
                    : event.getOldTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss")),
                event.getNewTime() == null
                    ? "Never Requested To Speak"
                    : event.getNewTime().format(DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss"))
            ),
            guildVoiceEvent.getLogChannelId()
        );
    }

    //Role events

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isCreateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            event.getRole().getGuild().getOwner() == null ? null : event.getGuild().getOwner().getUser(),
            "**__Role Create__**\nName: %s\nColor: %s\nPermissions: %s\nExplicit Permissions: %s\nPosition: %s\n" +
                "Hoisted: %s\nMentionable: %s\nManaged: %s\nPublic Role: %s\n" +
                "Bot Id: %s\nIntegration Id: %s\nSubscription Id: %s\nTags: %s%s%s%s%s".formatted(
                    event.getRole().getAsMention(),
                    event.getRole().getColor() == null ? "No Color" : event.getRole().getColor().toString(),
                    event.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    event.getRole().getPermissionsExplicit().stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    event.getRole().getPosition(),
                    event.getRole().isHoisted() ? "Hoisted" : "Not Hoisted",
                    event.getRole().isMentionable() ? "Mentionable" : "Not Mentionable",
                    event.getRole().isManaged() ? "Managed" : "Not Managed",
                    event.getRole().isPublicRole() ? "Public Role" : "Not Public Role",
                    event.getRole().getTags().getBotId() == null ? "No Bot Id" : event.getRole().getTags().getBotId(),
                    event.getRole().getTags().getIntegrationId() == null ? "No Integration Id" : event.getRole().getTags().getIntegrationId(),
                    event.getRole().getTags().getSubscriptionId() == null ? "No Subscription Id" : event.getRole().getTags().getSubscriptionId(),
                    event.getRole().getTags().isAvailableForPurchase() ? "Available For Purchase" : "Not Available For Purchase",
                    event.getRole().getTags().isBoost() ? "Boost" : "Not Boost",
                    event.getRole().getTags().isBot() ? "Bot" : "Not Bot",
                    event.getRole().getTags().isIntegration() ? "Integration" : "Not Integration",
                    event.getRole().getTags().isLinkedRole() ? "Linked Role" : "Not Linked Role"
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isDeleteEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Delete__**\nName: %s\nColor: %s\nPermissions: %s\nExplicit Permissions: %s\nPosition: %s\n" +
                "Hoisted: %s\nMentionable: %s\nManaged: %s\nPublic Role: %s\n" +
                "Bot Id: %s\nIntegration Id: %s\nSubscription Id: %s\nTags: %s%s%s%s%s".formatted(
                    event.getRole().getAsMention(),
                    event.getRole().getColor() == null ? "No Color" : event.getRole().getColor().toString(),
                    event.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    event.getRole().getPermissionsExplicit().stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    event.getRole().getPosition(),
                    event.getRole().isHoisted() ? "Hoisted" : "Not Hoisted",
                    event.getRole().isMentionable() ? "Mentionable" : "Not Mentionable",
                    event.getRole().isManaged() ? "Managed" : "Not Managed",
                    event.getRole().isPublicRole() ? "Public Role" : "Not Public Role",
                    event.getRole().getTags().getBotId() == null ? "No Bot Id" : event.getRole().getTags().getBotId(),
                    event.getRole().getTags().getIntegrationId() == null ? "No Integration Id" : event.getRole().getTags().getIntegrationId(),
                    event.getRole().getTags().getSubscriptionId() == null ? "No Subscription Id" : event.getRole().getTags().getSubscriptionId(),
                    event.getRole().getTags().isAvailableForPurchase() ? "Available For Purchase" : "Not Available For Purchase",
                    event.getRole().getTags().isBoost() ? "Boost" : "Not Boost",
                    event.getRole().getTags().isBot() ? "Bot" : "Not Bot",
                    event.getRole().getTags().isIntegration() ? "Integration" : "Not Integration",
                    event.getRole().getTags().isLinkedRole() ? "Linked Role" : "Not Linked Role"
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    //Role Update Events

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdateColorEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Color Update__**\nName: %s\nOld Color: %s\nNew Color: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldColor() == null ? "No Color" : event.getOldColor().toString(),
                event.getNewColor() == null ? "No Color" : event.getNewColor().toString()
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdateHoistedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Hoist Update__**\nName: %s\nOld Hoist: %s\nNew Hoist: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldValue() ? "Hoisted" : "Not Hoisted",
                event.getNewValue() ? "Hoisted" : "Not Hoisted"
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdateIcon(@Nonnull RoleUpdateIconEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdateIconEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Icon Update__**\nName: %s\nOld Icon: %s\nNew Icon: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldIcon() == null ? "No Icon" : event.getOldIcon().getIconUrl(),
                event.getNewIcon() == null ? "No Icon" : event.getNewIcon().getIconUrl()
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdateMentionableEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Mentionable Update__**\nName: %s\nOld Mentionable: %s\nNew Mentionable: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldValue() ? "Mentionable" : "Not Mentionable",
                event.getNewValue() ? "Mentionable" : "Not Mentionable"
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Name Update__**\nOld Name: %s\nNew Name: %s".formatted(
                event.getOldName(),
                event.getNewName()
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdatePermissionsEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Permission Update__**\nName: %s\nOld Permissions: %s\nNew Permissions: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")),
                event.getNewPermissions().stream().map(Permission::getName).collect(Collectors.joining(", "))
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    @Override
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {
        GuildRoleEvent guildRoleEvent = getGuildRoleEvent(event.getGuild());
        if (!guildRoleEvent.isUpdatePositionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Role Position Update__**\nName: %s\nOld Position: %s\nNew Position: %s".formatted(
                event.getRole().getAsMention(),
                event.getOldPosition(),
                event.getNewPosition()
            ),
            guildRoleEvent.getLogChannelId(),
            event.getRole().getIcon() == null ? null : event.getRole().getIcon().getIconUrl(),
            null
        );
    }

    //Emoji Events

    @Override
    public void onEmojiAdded(@Nonnull EmojiAddedEvent event) {
        GuildEmojiEvent guildEmojiEvent = getGuildEmojiEvent(event.getGuild());
        if (!guildEmojiEvent.isAddedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Emoji Added__**\nName: %s\nId: %s\nAnimated: %s\nRoles: %s".formatted(
                event.getEmoji().getAsMention(),
                event.getEmoji().getId(),
                event.getEmoji().isAnimated() ? "Animated" : "Not Animated",
                event.getEmoji().getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", "))
            ),
            guildEmojiEvent.getLogChannelId(),
            event.getEmoji().getImageUrl(),
            null
        );
    }

    @Override
    public void onEmojiRemoved(@Nonnull EmojiRemovedEvent event) {
        GuildEmojiEvent guildEmojiEvent = getGuildEmojiEvent(event.getGuild());
        if (!guildEmojiEvent.isRemovedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Emoji Removed__**\nName: %s\nId: %s\nAnimated: %s\nRoles: %s".formatted(
                event.getEmoji().getAsMention(),
                event.getEmoji().getId(),
                event.getEmoji().isAnimated() ? "Animated" : "Not Animated",
                event.getEmoji().getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", "))
            ),
            guildEmojiEvent.getLogChannelId(),
            event.getEmoji().getImageUrl(),
            null
        );
    }

    //Emoji Update Events

    @Override
    public void onEmojiUpdateName(@Nonnull EmojiUpdateNameEvent event) {
        GuildEmojiEvent guildEmojiEvent = getGuildEmojiEvent(event.getGuild());
        if (!guildEmojiEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Emoji Name Update__**\nOld Name: %s\nNew Name: %s".formatted(
                event.getOldName(),
                event.getNewName()
            ),
            guildEmojiEvent.getLogChannelId(),
            event.getEmoji().getImageUrl(),
            null
        );
    }

    @Override
    public void onEmojiUpdateRoles(@Nonnull EmojiUpdateRolesEvent event) {
        GuildEmojiEvent guildEmojiEvent = getGuildEmojiEvent(event.getGuild());
        if (!guildEmojiEvent.isUpdateRolesEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Emoji Roles Update__**\nName: %s\nOld Roles: %s\nNew Roles: %s".formatted(
                event.getEmoji().getAsMention(),
                event.getOldRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", ")),
                event.getNewRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", "))
            ),
            guildEmojiEvent.getLogChannelId(),
            event.getEmoji().getImageUrl(),
            null
        );
    }

    // Application command permission update events

    @Override
    public void onGenericPrivilegeUpdate(@Nonnull GenericPrivilegeUpdateEvent event) {
        GuildApplicationCommandPermissionEvent guildApplicationCommandPermissionEvent = getGuildApplicationCommandPermissionEvent(event.getGuild());
        if (!guildApplicationCommandPermissionEvent.isGenericPrivilegeUpdateEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Generic Privilege Update__**\nNew Privileges: %s\nTarget Type: %s".formatted(
                event.getPrivileges().stream().map(IntegrationPrivilege::getType).map(Type::name).collect(Collectors.joining(", ")),
                event.getTargetType().name()
            ),
            guildApplicationCommandPermissionEvent.getLogChannelId()
        );
    }

    @Override
    public void onApplicationCommandUpdatePrivileges(@Nonnull ApplicationCommandUpdatePrivilegesEvent event) {
        GuildApplicationCommandPermissionEvent guildApplicationCommandPermissionEvent = getGuildApplicationCommandPermissionEvent(event.getGuild());
        if (!guildApplicationCommandPermissionEvent.isApplicationCommandUpdatePrivilegesEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Application Command Privilege Update__**\nApplication Id: %s\nCommand Id: %s\nNew Privileges: %s\nTarget Type: %s".formatted(
                event.getApplicationId(),
                event.getCommandId(),
                event.getPrivileges().stream().map(IntegrationPrivilege::getType).map(Type::name).collect(Collectors.joining(", ")),
                event.getTargetType().name()
            ),
            guildApplicationCommandPermissionEvent.getLogChannelId()
        );
    }

    @Override
    public void onApplicationUpdatePrivileges(@Nonnull ApplicationUpdatePrivilegesEvent event) {
        GuildApplicationCommandPermissionEvent guildApplicationCommandPermissionEvent = getGuildApplicationCommandPermissionEvent(event.getGuild());
        if (!guildApplicationCommandPermissionEvent.isApplicationUpdatePrivilegesEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Application Privilege Update__**\nApplication Id: %s\nNew Privileges: %s\nTarget Type: %s".formatted(
                event.getApplicationId(),
                event.getPrivileges().stream().map(IntegrationPrivilege::getType).map(Type::name).collect(Collectors.joining(", ")),
                event.getTargetType().name()
            ),
            guildApplicationCommandPermissionEvent.getLogChannelId()
        );
    }

    //Sticker Events

    @Override
    public void onGuildStickerAdded(@Nonnull GuildStickerAddedEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isAddedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Added__**\nName: %s\nId: %s\nTags: %s\nDescription: %s\nAvailable: %s".formatted(
                event.getSticker().getName(),
                event.getSticker().getId(),
                event.getSticker().getTags().stream().collect(Collectors.joining(", ")),
                event.getSticker().getDescription(),
                event.getSticker().isAvailable() ? "Available" : "Not Available"
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }

    @Override
    public void onGuildStickerRemoved(@Nonnull GuildStickerRemovedEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isRemovedEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Removed__**\nName: %s\nId: %s\nTags: %s\nDescription: %s\nAvailable: %s".formatted(
                event.getSticker().getName(),
                event.getSticker().getId(),
                event.getSticker().getTags().stream().collect(Collectors.joining(", ")),
                event.getSticker().getDescription(),
                event.getSticker().isAvailable() ? "Available" : "Not Available"
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }

    //Sticker Update Events

    @Override
    public void onGuildStickerUpdateName(@Nonnull GuildStickerUpdateNameEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isUpdateNameEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Name Update__**\nOld Name: %s\nNew Name: %s".formatted(
                event.getOldValue(),
                event.getNewValue()
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }

    @Override
    public void onGuildStickerUpdateTags(@Nonnull GuildStickerUpdateTagsEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isUpdateTagsEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Tags Update__**\nOld Tags: %s\nNew Tags: %s".formatted(
                event.getOldValue().stream().collect(Collectors.joining(", ")),
                event.getNewValue().stream().collect(Collectors.joining(", "))
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }

    @Override
    public void onGuildStickerUpdateDescription(@Nonnull GuildStickerUpdateDescriptionEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isUpdateDescriptionEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Description Update__**\nOld Description: %s\nNew Description: %s".formatted(
                event.getOldValue(),
                event.getNewValue()
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }

    @Override
    public void onGuildStickerUpdateAvailable(@Nonnull GuildStickerUpdateAvailableEvent event) {
        GuildStickerEvent guildStickerEvent = getGuildStickerEvent(event.getGuild());
        if (!guildStickerEvent.isUpdateAvailableEvent()) return;

        autoFinalizedAndSend(
            event,
            event.getGuild(),
            null,
            "**__Sticker Availability Update__**\nOld Availability: %s\nNew Availability: %s".formatted(
                event.getOldValue() ? "Available" : "Not Available",
                event.getNewValue() ? "Available" : "Not Available"
            ),
            guildStickerEvent.getLogChannelId(),
            event.getSticker().getIconUrl(),
            null
        );
    }
}
