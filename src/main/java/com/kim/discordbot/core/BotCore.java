package com.kim.discordbot.core;

import com.google.common.eventbus.EventBus;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.processor.CommandProcessor;
import com.kim.discordbot.core.database.BotConfig;
import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.core.listeners.command.ContextCommandListener;
import com.kim.discordbot.core.listeners.command.SlashCommandInteractionListener;
import com.kim.discordbot.core.thread.ThreadController;
import com.kim.discordbot.util.BotLogger;
import com.kim.discordbot.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private static CountDownLatch latch;

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
        latch = new CountDownLatch(1);
        ThreadController.eventExecutor.execute(
            () -> {
                preLoadProcedures();
                latch.countDown();
            }
        );
        latch.await();

        // Fire up the shards and wait for all the shards to be ready
        shardManager = shardManagerBuilder.build();
        latch = new CountDownLatch(shardManager.getShardsTotal());
        latch.await();

        // Post load procedures
        latch = new CountDownLatch(1);
        ThreadController.eventExecutor.execute(
            () -> {
                postLoadProcedures();
                latch.countDown();
            }
        );
        latch.await();
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
            .addEventListeners(shardReadyListener)
            .enableCache(getCacheFlags())
            .setChunkingFilter(ChunkingFilter.NONE)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setShardsTotal(getShardCount());
    }

    private void preLoadProcedures() {
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
                log.info("Registering commands for cog: {}", cog.getName());
                eventBus.register(cog.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Error while registering commands for cog: {}\n{}", cog.getName(), e);
            }
        };

        ThreadController.eventExecutor.execute(
            () -> eventBus.post(CommandProcessor.REGISTRY)
        );

        shardManagerBuilder.addEventListenerProviders(
            List.of(
                shardId -> new SlashCommandInteractionListener(commandProcessor, ThreadController.commandExecutor),
                shardId -> new ContextCommandListener(commandProcessor, ThreadController.commandExecutor)
            )
        );
    }

    private void postLoadProcedures() {
        log.info("Running post load procedures");

        // Register slash commands
        syncSlashCommands(
            CommandProcessor.REGISTRY.MANAGER.getSlashCommandDatas()
        );
    }

    private void syncSlashCommands(@Nonnull List<CommandData> commands) {
        String title = String.format("Registering a total of {} slash commands: ", commands.size());
        log.info(title + String.join(
            ", ",
            commands.stream()
                .map(CommandData::getName)
                .collect(Collectors.toList())
            ),
            commands.size()
        );
        for (JDA jda : shardManager.getShards()) {
            jda.updateCommands().addCommands(commands).queue();
            log.info("Registered {} slash commands for shard {}", commands.size(), jda.getShardInfo().getShardId());
        }
    }

    private static class ShardReadyListener extends ListenerAdapter {
        @Override
        public void onReady(@Nonnull ReadyEvent event) {
            log.info("Shard {} is ready", event.getJDA().getShardInfo().getShardId());
            latch.countDown();
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
