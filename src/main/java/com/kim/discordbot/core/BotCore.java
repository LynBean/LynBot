package com.kim.discordbot.core;

import com.google.common.eventbus.EventBus;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.CommandProcessor;
import com.kim.discordbot.core.database.BotConfig;
import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.core.listeners.SlashCommandInteractionListener;
import com.kim.discordbot.util.BotLogger;
import com.kim.discordbot.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;

public class BotCore {
    private final EventBus eventBus = new EventBus();
    private final static Logger log = BotLogger.getLogger(BotCore.class);

    private DefaultShardManagerBuilder shardManagerBuilder;
    private final static BotConfig botConfig = new BotConfig();
    private final static CommandProcessor commandProcessor = new CommandProcessor();
    private final static ShardReadyListener shardReadyListener = new ShardReadyListener();
    private ShardManager shardManager;
    private String commandsPackage = "com.kim.discordbot.cogs";

    private @Nonnull Collection<GatewayIntent> getGateways() {
        GatewayIntent[] gateways = {
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.DIRECT_MESSAGE_TYPING,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MESSAGE_TYPING,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_WEBHOOKS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.SCHEDULED_EVENTS,
        };

        return new ArrayList<GatewayIntent>(List.of(gateways));
    }

    private @Nonnull Collection<CacheFlag> getCacheFlags() {
        CacheFlag[] cacheflags = {
            CacheFlag.ACTIVITY,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.EMOJI,
            CacheFlag.FORUM_TAGS,
            CacheFlag.MEMBER_OVERRIDES,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.ROLE_TAGS,
            CacheFlag.SCHEDULED_EVENTS,
            CacheFlag.STICKER,
            CacheFlag.VOICE_STATE,
        };

        return new ArrayList<CacheFlag>(List.of(cacheflags));
    }

    public void start() throws InvalidTokenException, InterruptedException {
        // Pre load procedures
        BotThreadWorker preLoad = new BotThreadWorker(preLoadProcedures());
        preLoad.start();
        preLoad.getLatch().await();

        // Fire up the shards and wait for all the shards to be ready
        shardManager = shardManagerBuilder.build();
        shardReadyListener.setLatch(new CountDownLatch(shardManager.getShardsTotal()));
        shardReadyListener.getLatch().await();

        // Post load procedures
        new BotThreadWorker(postLoadProcedures())
            .start();
    }

    /**
     * Set the commands package to scan for commands
     */
    public BotCore setCommandsPackage(String commandsPackage) {
        this.commandsPackage = commandsPackage;
        return this;
    }

    /**
     * Returns the shard count from the bot config file.
     * If the result is null, it will return -1.
     * (Discord API will handle the sharding)
     * @return int
     */
    private static int getShardCount() {
        return Integer.parseInt(
            ConfigManager.get("shard-count", "-1")
        );
    }

    private DefaultShardManagerBuilder getShardManagerBuilder() {
        return DefaultShardManagerBuilder.create(
            ConfigManager.get("bot-token"),
            getGateways()
        )
            .setChunkingFilter(ChunkingFilter.NONE)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(getCacheFlags())
            .addEventListeners(shardReadyListener)
            .setShardsTotal(getShardCount());
    }

    private Runnable preLoadProcedures() {
        return new Runnable() {
            @Override
            public void run() {
                log.info("Running pre load procedures");

                // Register bot config properties into ConfigManager
                ConfigManager.registerProperties(botConfig.getProperties());

                shardManagerBuilder = getShardManagerBuilder();

                // Register commands package
                if (commandsPackage == null) {
                    log.warn("No commands package set, no commands will be loaded");
                    return;
                }

                Set<Class<?>> cogs = Util.lookForAnnotatedOn(commandsPackage, Cog.class);
                if (cogs.isEmpty()){
                    log.warn("No commands found in package: {}", commandsPackage);
                    return;
                }

                for (Class<?> cog : cogs) {
                    try {
                        eventBus.register(cog.getDeclaredConstructor().newInstance());
                    } catch (Exception e) {
                        log.error("Error while registering commands for cog: {}\n{}", cog.getName(), e);
                    }
                };
                new Thread(() -> eventBus.post(CommandProcessor.REGISTRY))
                    .start();

                shardManagerBuilder.addEventListenerProvider(
                    (shardId) -> new SlashCommandInteractionListener(commandProcessor)
                );
            }
        };
    }

    private Runnable postLoadProcedures() {
        return new Runnable() {
            @Override
            public void run() {
                log.info("Running post load procedures");

                // Register slash commands
                syncSlashCommands(
                    CommandProcessor.REGISTRY.MANAGER.getSlashCommandDatas()
                );
            }
        };
    }

    private void syncSlashCommands(@Nonnull List<CommandData> commands) {
        log.info("Syncing slash commands with Discord API");
        for (JDA jda : shardManager.getShards()) {
            jda.updateCommands().addCommands(commands).queue();
        }
    }

    private static class ShardReadyListener extends ListenerAdapter {
        private CountDownLatch latch;

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        @Override
        public void onReady(@Nonnull ReadyEvent event) {
            log.info("Shard {} is ready", event.getJDA().getShardInfo().getShardId());
            latch.countDown();
        }
    }

    public class BotThreadWorker extends Thread {
        private final Runnable runnable;
        private final CountDownLatch latch;

        public BotThreadWorker(Runnable runnable) {
            this(runnable, new CountDownLatch(1));
        }

        public BotThreadWorker(Runnable runnable, CountDownLatch latch) {
            this.runnable = runnable;
            this.latch = latch;
            this.setName(runnable.toString());
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        @Override
        public void run() {
            runnable.run();
            latch.countDown();
            log.info("Thread {} finished", Thread.currentThread().getName());
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
