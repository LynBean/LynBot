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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.User.UserFlag;

@Accessors(chain = true)
@BsonDiscriminator(value="UserWrapper", key="_cls")
@Data
public class UserWrapper {
    @BsonCreator
    public UserWrapper() {}

    public UserWrapper(User user) {
        this.asMention = user.getAsMention();
        this.asTag = user.getAsTag();
        this.avatarId = user.getAvatarId();
        this.avatarUrl = user.getAvatarUrl();
        this.defaultAvatarId = user.getDefaultAvatarId();
        this.defaultAvatarUrl = user.getDefaultAvatarUrl();
        this.discriminator = user.getDiscriminator();
        this.effectiveAvatarUrl = user.getEffectiveAvatarUrl();
        this.flags = user.getFlags().stream().map(UserFlag::name).toList();
        this.flagsRaw = user.getFlagsRaw();
        this.id = user.getId();
        this.mutualGuildIds = user.getMutualGuilds().stream().map(Guild::getId).toList();
        this.name = user.getName();
        this.timeCreated = user.getTimeCreated().toLocalDateTime();
    }

    public UserWrapper(Member member) {
        this(member.getUser());
    }

    @BsonId
    @BsonProperty(value = "_id")
    private String id;

    @BsonProperty(value = "as_mention")
    private String asMention;

    @BsonProperty(value = "as_tag")
    private String asTag;

    @BsonProperty(value = "avatar_id")
    private String avatarId;

    @BsonProperty(value = "avatar_url")
    private String avatarUrl;

    @BsonProperty(value = "default_avatar_id")
    private String defaultAvatarId;

    @BsonProperty(value = "default_avatar_url")
    private String defaultAvatarUrl;

    @BsonProperty(value = "discriminator")
    private String discriminator;

    @BsonProperty(value = "effective_avatar_url")
    private String effectiveAvatarUrl;

    @BsonProperty(value = "flags")
    private List<String> flags;

    @BsonProperty(value = "flags_raw")
    private int flagsRaw;

    @BsonProperty(value = "mutual_guild_ids")
    private List<String> mutualGuildIds;

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "time_created")
    private LocalDateTime timeCreated;
}
