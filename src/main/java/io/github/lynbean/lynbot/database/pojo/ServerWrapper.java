package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerWrapper", key="_cls")
@Data
public class ServerWrapper {
    @BsonCreator
    public ServerWrapper() {}

    public ServerWrapper(Guild guild) {
        this.id = guild.getId();
        this.afkChannelId = guild.getAfkChannel() == null ? null : guild.getAfkChannel().getId();
        this.bannerId = guild.getBannerId();
        this.bannerUrl = guild.getBannerUrl();
        this.channelIds = guild.getChannels().stream().map(GuildChannel::getId).toList();
        this.communityUpdatesChannelId = guild.getCommunityUpdatesChannel() == null ? null : guild.getCommunityUpdatesChannel().getId();
        this.defaultChannelId = guild.getDefaultChannel() == null ? null : guild.getDefaultChannel().getId();
        this.description = guild.getDescription();
        this.emojiIds = guild.getEmojis().stream().map(RichCustomEmoji::getId).toList();
        this.forumChannelIds = guild.getForumChannels().stream().map(ForumChannel::getId).toList();
        this.iconId = guild.getIconId();
        this.iconUrl = guild.getIconUrl();
        this.locale = guild.getLocale().toString();
        this.memberIds = guild.getMembers().stream().map(Member::getId).toList();
        this.name = guild.getName();
        this.newsChannelIds = guild.getNewsChannels().stream().map(NewsChannel::getId).toList();
        this.ownerId = guild.getOwnerId();
        this.rulesChannelId = guild.getRulesChannel() == null ? null : guild.getRulesChannel().getId();
        this.splashId = guild.getSplashId();
        this.splashUrl = guild.getSplashUrl();
        this.stageChannelIds = guild.getStageChannels().stream().map(StageChannel::getId).toList();
        this.stickerIds = guild.getStickers().stream().map(GuildSticker::getId).toList();
        this.systemChannelId = guild.getSystemChannel() == null ? null : guild.getSystemChannel().getId();
        this.textChannelIds = guild.getTextChannelCache().stream().map(TextChannel::getId).toList();
        this.threadChannelIds = guild.getThreadChannelCache().stream().map(ThreadChannel::getId).toList();
        this.timeCreated = guild.getTimeCreated().toLocalDateTime();
        this.vanityCode = guild.getVanityCode();
        this.vanityUrl = guild.getVanityUrl();
        this.voiceChannelIds = guild.getVoiceChannelCache().stream().map(VoiceChannel::getId).toList();
    }

    @BsonId()
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "afk_channel_id")
    private String afkChannelId;

    @BsonProperty(value = "banner_id")
    private String bannerId;

    @BsonProperty(value = "banner_url")
    private String bannerUrl;

    @BsonProperty(value = "channel_ids")
    private List<String> channelIds;

    @BsonProperty(value = "community_updates_channel_id")
    private String communityUpdatesChannelId;

    @BsonProperty(value = "default_channel_id")
    private String defaultChannelId;

    @BsonProperty(value = "description")
    private String description;

    @BsonProperty(value = "emoji_ids")
    private List<String> emojiIds;

    @BsonProperty(value = "forum_channel_ids")
    private List<String> forumChannelIds;

    @BsonProperty(value = "icon_id")
    private String iconId;

    @BsonProperty(value = "icon_url")
    private String iconUrl;

    @BsonProperty(value = "locale")
    private String locale;

    @BsonProperty(value = "member_ids")
    private List<String> memberIds;

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "news_channel_ids")
    private List<String> newsChannelIds;

    @BsonProperty(value = "owner_id")
    private String ownerId;

    @BsonProperty(value = "rules_channel_id")
    private String rulesChannelId;

    @BsonProperty(value = "splash_id")
    private String splashId;

    @BsonProperty(value = "splash_url")
    private String splashUrl;

    @BsonProperty(value = "stage_channel_ids")
    private List<String> stageChannelIds;

    @BsonProperty(value = "sticker_ids")
    private List<String> stickerIds;

    @BsonProperty(value = "system_channel_id")
    private String systemChannelId;

    @BsonProperty(value = "text_channel_ids")
    private List<String> textChannelIds;

    @BsonProperty(value = "thread_channel_ids")
    private List<String> threadChannelIds;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "vanity_code")
    private String vanityCode;

    @BsonProperty(value = "vanity_url")
    private String vanityUrl;

    @BsonProperty(value = "voice_channel_ids")
    private List<String> voiceChannelIds;
}
