package com.kim.discordbot.core;

import com.google.common.eventbus.EventBus;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.CommandProcessor;
import com.kim.discordbot.core.database.BotConfig;
import com.kim.discordbot.core.listeners.SlashCommandInteractionListener;
import com.kim.discordbot.util.BotLogger;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
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
    private final Logger log = BotLogger.getLogger(BotCore.class);
    private final BotConfig dbManager = new BotConfig();
    private ShardManager shardManager;
    private EventBus eventBus = new EventBus();
    private final CommandProcessor commandProcessor = new CommandProcessor();

    private final String botToken = dbManager.getProperties().getProperty("bot-token");
    private String commandsPackage;

    public void start() throws InvalidTokenException {
        Set<Class<?>> cogs = lookForAnnotatedOn(commandsPackage, Cog.class);

        if (cogs.isEmpty())
            log.warn("No commands found in package: {}", commandsPackage);

        for (Class<?> cog : cogs) {
            try {
                eventBus.register(cog.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Error while registering commands for cog: {}", cog.getName(), e);
            }
        }

        new Thread(
            () -> eventBus.post(CommandProcessor.REGISTRY)
        )
            .start();

        shardManager = DefaultShardManagerBuilder.create(botToken, getGateways())
            .setChunkingFilter(ChunkingFilter.NONE)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(getCacheFlags())
            .addEventListenerProvider(
                (shardId) -> new SlashCommandInteractionListener(commandProcessor)
            )
            .build();

        syncSlashCommands(
            CommandProcessor.REGISTRY.getCommandManager().getSlashCommandsList()
        );
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

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

    public BotCore setCommandsPackage(String commandsPackage) {
        this.commandsPackage = commandsPackage;
        return this;
    }

    private Set<Class<?>> lookForAnnotatedOn(String packageName, Class<? extends Annotation> annotation) {
        ClassGraph classGraph = new ClassGraph()
            .acceptPackages(packageName)
            .enableAnnotationInfo();

        try (ScanResult scanResult = classGraph.scan(3)) {
            return scanResult.getAllClasses()
                .stream()
                .filter(classInfo -> classInfo.hasAnnotation(annotation.getName())).map(ClassInfo::loadClass)
                .collect(Collectors.toSet());
        }
    }

    public void syncSlashCommands(@Nonnull List<CommandData> commands) {
        log.info("Syncing slash commands with Discord API");
        for (JDA jda : shardManager.getShards()) {
            jda.updateCommands().addCommands(commands).queue();
        }
    }
}
