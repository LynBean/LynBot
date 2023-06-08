package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildEvent() {
        super();
    }

    @BsonProperty(value = "ban_event")
    protected boolean banEvent;

    @BsonProperty(value = "unban_event")
    protected boolean unbanEvent;

    @BsonProperty(value = "audit_log_entry_create_event")
    protected boolean auditLogEntryCreateEvent;

    @BsonProperty(value = "member_remove_event")
    protected boolean memberRemoveEvent;

    @BsonProperty(value = "update_afk_channel_event")
    protected boolean updateAfkChannelEvent;

    @BsonProperty(value = "update_system_channel_event")
    protected boolean updateSystemChannelEvent;

    @BsonProperty(value = "update_rules_channel_event")
    protected boolean updateRulesChannelEvent;

    @BsonProperty(value = "update_community_updates_channel_event")
    protected boolean updateCommunityUpdatesChannelEvent;

    @BsonProperty(value = "update_afk_timeout_event")
    protected boolean updateAfkTimeoutEvent;

    @BsonProperty(value = "update_explicit_content_level_event")
    protected boolean updateExplicitContentLevelEvent;

    @BsonProperty(value = "update_icon_event")
    protected boolean updateIconEvent;

    @BsonProperty(value = "update_mfa_level_event")
    protected boolean updateMFALevelEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_notification_level_event")
    protected boolean updateNotificationLevelEvent;

    @BsonProperty(value = "update_owner_event")
    protected boolean updateOwnerEvent;

    @BsonProperty(value = "update_splash_event")
    protected boolean updateSplashEvent;

    @BsonProperty(value = "update_verification_level_event")
    protected boolean updateVerificationLevelEvent;

    @BsonProperty(value = "update_locale_event")
    protected boolean updateLocaleEvent;

    @BsonProperty(value = "update_features_event")
    protected boolean updateFeaturesEvent;

    @BsonProperty(value = "update_vanity_code_event")
    protected boolean updateVanityCodeEvent;

    @BsonProperty(value = "update_banner_event")
    protected boolean updateBannerEvent;

    @BsonProperty(value = "update_description_event")
    protected boolean updateDescriptionEvent;

    @BsonProperty(value = "update_boost_tier_event")
    protected boolean updateBoostTierEvent;

    @BsonProperty(value = "update_boost_count_event")
    protected boolean updateBoostCountEvent;

    @BsonProperty(value = "update_max_members_event")
    protected boolean updateMaxMembersEvent;

    @BsonProperty(value = "update_max_presences_event")
    protected boolean updateMaxPresencesEvent;

    @BsonProperty(value = "update_nsfw_level_event")
    protected boolean updateNSFWLevelEvent;
}
