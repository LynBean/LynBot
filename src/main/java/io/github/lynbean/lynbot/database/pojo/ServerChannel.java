package io.github.lynbean.lynbot.database.pojo;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;

@Accessors(chain = true)
@BsonDiscriminator(value="ServerChannel", key="_cls")
@Data
public class ServerChannel {
    @BsonCreator
    public ServerChannel() {}

    public ServerChannel(GuildChannel channel) {
        this.asMention = channel.getAsMention();
        this.flags = channel.getFlags().stream().map(ChannelFlag::name).toList();
        this.guildId = channel.getGuild().getId();
        this.jumpUrl = channel.getJumpUrl();
        this.memberIds = getChannelMembers(channel);
        this.name = channel.getName();
        this.ownerId = null;
        this.threadChannelIds = null;
        this.threadMemberIds = null;
        this.timeCreated = channel.getTimeCreated().toLocalDateTime();
        this.topic = null;
        this.type = channel.getType().toString();

        if (channel.getClass().isAssignableFrom(TextChannel.class)) {
            this.topic = ((TextChannel) channel).getTopic();
            this.threadChannelIds = getThreadChannelIds(channel);
        } else if (channel.getClass().isAssignableFrom(NewsChannel.class)) {
            this.topic = ((NewsChannel) channel).getTopic();
        } else if (channel.getClass().isAssignableFrom(ForumChannel.class)) {
            this.topic = ((ForumChannel) channel).getTopic();
            this.threadChannelIds = getThreadChannelIds(channel);
        } else if (channel.getClass().isAssignableFrom(ThreadChannel.class)) {
            this.ownerId = ((ThreadChannel) channel).getOwner() == null ? null : ((ThreadChannel) channel).getOwner().getId();
            this.threadMemberIds = ((ThreadChannel) channel).getThreadMembers().stream().map(ThreadMember::getId).toList();
        }
    }

    private List<String> getChannelMembers(GuildChannel channel) {
        return ((IMemberContainer) channel).getMembers().stream().map(Member::getId).toList();
    }

    private List<String> getThreadChannelIds(GuildChannel channel) {
        return ((IThreadContainer) channel).getThreadChannels().stream().map(ThreadChannel::getId).toList();
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "as_mention")
    private String asMention;

    @BsonProperty(value = "flags")
    private List<String> flags;

    @BsonProperty(value = "guild_id")
    private String guildId;

    @BsonProperty(value = "jump_url")
    private String jumpUrl;

    @BsonProperty(value = "member_ids")
    private List<String> memberIds;

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "owner_id")
    private String ownerId;

    @BsonProperty(value = "thread_channel_ids")
    private List<String> threadChannelIds;

    @BsonProperty(value = "thread_member_ids")
    private List<String> threadMemberIds;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;

    @BsonProperty(value = "topic")
    private String topic;

    @BsonProperty(value = "type")
    private String type;
}
