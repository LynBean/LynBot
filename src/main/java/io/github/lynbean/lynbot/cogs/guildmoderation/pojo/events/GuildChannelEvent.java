package io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class GuildChannelEvent extends GuildGenericEvent {
    @BsonCreator
    public GuildChannelEvent() {
        super();
    }

    @BsonProperty(value = "create_event")
    protected boolean createEvent;

    @BsonProperty(value = "delete_event")
    protected boolean deleteEvent;

    @BsonProperty(value = "update_bitrate_event")
    protected boolean updateBitrateEvent;

    @BsonProperty(value = "update_name_event")
    protected boolean updateNameEvent;

    @BsonProperty(value = "update_flags_event")
    protected boolean updateFlagsEvent;

    @BsonProperty(value = "update_nsfw_event")
    protected boolean updateNSFWEvent;

    @BsonProperty(value = "update_parent_event")
    protected boolean updateParentEvent;

    @BsonProperty(value = "update_position_event")
    protected boolean updatePositionEvent;

    @BsonProperty(value = "update_region_event")
    protected boolean updateRegionEvent;

    @BsonProperty(value = "update_slowmode_event")
    protected boolean updateSlowmodeEvent;

    @BsonProperty(value = "update_default_thread_slowmode_event")
    protected boolean updateDefaultThreadSlowmodeEvent;

    @BsonProperty(value = "update_default_reaction_event")
    protected boolean updateDefaultReactionEvent;

    @BsonProperty(value = "update_default_layout_event")
    protected boolean updateDefaultLayoutEvent;

    @BsonProperty(value = "update_topic_event")
    protected boolean updateTopicEvent;

    @BsonProperty(value = "update_type_event")
    protected boolean updateTypeEvent;

    @BsonProperty(value = "update_user_limit_event")
    protected boolean updateUserLimitEvent;

    @BsonProperty(value = "update_archived_event")
    protected boolean updateArchivedEvent;

    @BsonProperty(value = "update_archive_timestamp_event")
    protected boolean updateArchiveTimestampEvent;

    @BsonProperty(value = "update_auto_archive_duration_event")
    protected boolean updateAutoArchiveDurationEvent;

    @BsonProperty(value = "update_locked_event")
    protected boolean updateLockedEvent;

    @BsonProperty(value = "update_invitable_event")
    protected boolean updateInvitableEvent;

    @BsonProperty(value = "update_applied_tags_event")
    protected boolean updateAppliedTagsEvent;
}
