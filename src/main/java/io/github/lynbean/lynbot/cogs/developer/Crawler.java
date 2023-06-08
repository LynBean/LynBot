package io.github.lynbean.lynbot.cogs.developer;

import static io.github.lynbean.lynbot.Bot.getMongoManager;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mongodb.client.model.ReplaceOptions;

import static io.github.lynbean.lynbot.core.thread.ThreadController.commandExecutor;
import io.github.lynbean.lynbot.database.pojo.ServerChannel;
import io.github.lynbean.lynbot.database.pojo.ServerEmoji;
import io.github.lynbean.lynbot.database.pojo.ServerMessage;
import io.github.lynbean.lynbot.database.pojo.ServerSticker;
import io.github.lynbean.lynbot.database.pojo.ServerWrapper;
import io.github.lynbean.lynbot.database.pojo.UserWrapper;
import lombok.Data;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

public class Crawler {
    Map<String, Progress> progression = new HashMap<>();

    private JDA jda;
    private @Setter Guild guild;
    private @Setter GuildChannel channel;

    public Crawler(JDA jda) {
        this.jda = jda;
    }

    private List<Guild> getGuilds() {
        if (guild != null) {
            return List.of(guild);
        }
        return jda.getGuilds();
    }

    private List<GuildChannel> getChannels() {
        if (channel != null) {
            return List.of(channel);
        }
        return getGuilds()
            .stream()
            .flatMap(guild -> guild.getChannels().stream())
            .collect(Collectors.toList());
    }

    private List<User> getUsers() {
        return jda.getUsers();
    }

    private List<RichCustomEmoji> getEmojis() {
        return jda.getEmojis();
    }

    private List<GuildSticker> getStickers() {
        return getGuilds()
            .stream()
            .flatMap(guild -> guild.getStickers().stream())
            .collect(Collectors.toList());
    }

    public String crawlGuilds() {
        List<Guild> guilds = getGuilds();
        String id = createProgress(guilds.size());

        commandExecutor.execute(
            () -> {
                guilds.stream()
                    .forEach(
                        guild -> {
                            getMongoManager().replaceServer(
                                new ServerWrapper(guild),
                                new ReplaceOptions().upsert(true)
                            );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public String crawlChannels() {
        List<GuildChannel> channels = getChannels();
        String id = createProgress(channels.size());

        commandExecutor.execute(
            () -> {
                channels.stream()
                    .forEach(
                        channel -> {
                            getMongoManager().replaceServerChannel(
                                new ServerChannel(channel),
                                new ReplaceOptions().upsert(true)
                            );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public String crawlMessages() {
        List<GuildChannel> channels = getChannels();
        String id = createProgress(channels.size());

        commandExecutor.execute(
            () -> {
                channels.stream()
                    .filter(channel -> channel.getType().isMessage())
                    .forEach(
                        channel -> {
                            ((MessageChannel) channel)
                                .getIterableHistory()
                                .forEachAsync(
                                    message -> {
                                        getMongoManager().insertServerMessage(new ServerMessage(message), true);
                                        return true;
                                    },
                                    Throwable::printStackTrace
                                );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public String crawlUsers() {
        List<User> users = getUsers();
        String id = createProgress(users.size());

        commandExecutor.execute(
            () -> {
                users.stream()
                    .forEach(
                        user -> {
                            getMongoManager().replaceUser(
                                new UserWrapper(user),
                                new ReplaceOptions().upsert(true)
                            );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public String crawlEmojis() {
        List<RichCustomEmoji> emojis = getEmojis();
        String id = createProgress(emojis.size());

        commandExecutor.execute(
            () -> {
                emojis.stream()
                    .forEach(
                        emoji -> {
                            getMongoManager().replaceServerEmoji(
                                new ServerEmoji(emoji),
                                new ReplaceOptions().upsert(true)
                            );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public String crawlStickers() {
        List<GuildSticker> stickers = getStickers();
        String id = createProgress(stickers.size());

        commandExecutor.execute(
            () -> {
                stickers.stream()
                    .forEach(
                        sticker -> {
                            getMongoManager().replaceServerSticker(
                                new ServerSticker(sticker),
                                new ReplaceOptions().upsert(true)
                            );
                            addProgress(id);
                        }
                    );
                deleteProgress(id);
            }
        );
        return id;
    }

    public Progress getProgress(String id) {
        return progression.getOrDefault(id, null);
    }

    private String createProgress(int total) {
        String id = String.valueOf(System.currentTimeMillis());
        progression.put(id, new Progress(total));
        return id;
    }

    private void deleteProgress(String id) {
        progression.remove(id);
    }

    private void addProgress(String id) {
        progression.get(id).setCurrent(progression.get(id).getCurrent() + 1);
    }

    @Data
    public static class Progress {
        private int total;
        private int current;

        public Progress(int total) {
            this(total, 0);
        }

        public Progress(int total, int current) {
            this.total = total;
            this.current = current;
        }

        public int getProgress() {
            return (int) (((double) current / (double) total) * 100);
        }
    }
}
