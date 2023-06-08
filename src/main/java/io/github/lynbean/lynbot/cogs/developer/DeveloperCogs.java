package io.github.lynbean.lynbot.cogs.developer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.developer.Crawler.Progress;
import io.github.lynbean.lynbot.cogs.developer.listeners.DeveloperListener;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Cog
public class DeveloperCogs {
    private static Logger LOG = LoggerFactory.getLogger(DeveloperCogs.class);

    @Subscribe
    public void register(CommandRegistry registry) {
        if (Bot.getConfig().getString("development.guild_id").isBlank()) {
            LOG.warn("LynBot is running outside of development mode.");
            return;
        }

        registry.registerEventListeners(new DeveloperListener());
        registry.registerGuildSlashCommand(Development.class, Bot.getConfig().getString("development.guild_id"));
    }

    @SlashCommandMeta(
        name = "dev",
        description = "Development commands",
        isAdminOnly = true
    )
    public static class Development extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "crawl-messages",
            description = "Crawl through all messages into MongoDB",
            options = {
                @SlashCommandMeta.Option(
                    name = "guild-id",
                    description = "The guild to crawl through",
                    type = OptionType.STRING,
                    required = false
                ),
                @SlashCommandMeta.Option(
                    name = "channel-id",
                    description = "The channel to crawl through",
                    type = OptionType.STRING,
                    required = false
                )
            }
        )
        public static class CrawlMessages extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String guildId = event.getOption("guild-id") != null
                    ? event.getOption("guild-id").getAsString()
                    : null;
                String channelId = event.getOption("channel-id") != null
                    ? event.getOption("channel-id").getAsString()
                    : null;

                Crawler crawler = new Crawler(event.getJDA());

                if (guildId != null)
                    crawler.setGuild(event.getJDA().getGuildById(guildId));
                if (channelId != null)
                    crawler.setChannel(event.getJDA().getTextChannelById(channelId));

                String id = crawler.crawlMessages();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling messages... (from %d channels) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling messages!")
                            .build()
                    )
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "crawl-guilds",
            description = "Crawl guilds data into MongoDB"
        )
        public static class CrawlGuilds extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                Crawler crawler = new Crawler(event.getJDA());
                String id = crawler.crawlGuilds();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling guilds... (%d guilds) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling guilds!")
                            .build()
                    )
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "crawl-channels",
            description = "Crawl channels data into MongoDB",
            options = {
                @SlashCommandMeta.Option(
                    name = "guild-id",
                    description = "The guild to crawl through",
                    type = OptionType.STRING,
                    required = false
                )
            }
        )
        public static class CrawlChannels extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String guildId = event.getOption("guild-id") != null
                    ? event.getOption("guild-id").getAsString()
                    : null;

                Crawler crawler = new Crawler(event.getJDA());

                if (guildId != null)
                    crawler.setGuild(event.getJDA().getGuildById(guildId));

                String id = crawler.crawlChannels();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling channels... (%d channels) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling channels!")
                            .build()
                    )
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "crawl-users",
            description = "Crawl users data into MongoDB",
            options = {
                @SlashCommandMeta.Option(
                    name = "guild-id",
                    description = "The guild to crawl through",
                    type = OptionType.STRING,
                    required = false
                )
            }
        )
        public static class CrawlUsers extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String guildId = event.getOption("guild-id") != null
                    ? event.getOption("guild-id").getAsString()
                    : null;

                Crawler crawler = new Crawler(event.getJDA());

                if (guildId != null)
                    crawler.setGuild(event.getJDA().getGuildById(guildId));

                String id = crawler.crawlEmojis();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling users... (from %d guilds) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling users!")
                            .build()
                    )
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "crawl-emojis",
            description = "Crawl emojis data into MongoDB",
            options = {
                @SlashCommandMeta.Option(
                    name = "guild-id",
                    description = "The guild to crawl through",
                    type = OptionType.STRING,
                    required = false
                )
            }
        )
        public static class CrawlEmojis extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String guildId = event.getOption("guild-id") != null
                    ? event.getOption("guild-id").getAsString()
                    : null;

                Crawler crawler = new Crawler(event.getJDA());

                if (guildId != null)
                    crawler.setGuild(event.getJDA().getGuildById(guildId));

                String id = crawler.crawlEmojis();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling emojis... (from %d guilds) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling emojis!")
                            .build()
                    )
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "crawl-stickers",
            description = "Crawl stickers data into MongoDB",
            options = {
                @SlashCommandMeta.Option(
                    name = "guild-id",
                    description = "The guild to crawl through",
                    type = OptionType.STRING,
                    required = false
                )
            }
        )
        public static class CrawlStickers extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String guildId = event.getOption("guild-id") != null
                    ? event.getOption("guild-id").getAsString()
                    : null;

                Crawler crawler = new Crawler(event.getJDA());

                if (guildId != null)
                    crawler.setGuild(event.getJDA().getGuildById(guildId));

                String id = crawler.crawlEmojis();
                int progression = 0;

                while (crawler.getProgress(id) != null) {
                    Progress progress = crawler.getProgress(id);
                    if (progress == null) {
                        break;
                    }

                    progression = progress.getProgress();
                    event.getHook()
                        .editOriginalEmbeds(
                            new EmbedBuilder()
                                .setDescription("Crawling stickers... (from %d guilds) (%d%%)\n%s".formatted(
                                    progress.getTotal(), progression, getProgressionBar(progression)
                                    )
                                )
                                .build()
                        )
                        .queue();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }

                event.getHook()
                    .editOriginalEmbeds(
                        new EmbedBuilder()
                            .setDescription("Done crawling stickers!")
                            .build()
                    )
                    .queue();
            }
        }

        private static String getProgressionBar(double percentage) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                if (percentage >= (i + 1) * 10) {
                    sb.append("█");
                } else {
                    sb.append("░");
                }
            }
            return sb.toString();
        }
    }
}
