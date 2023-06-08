package io.github.lynbean.lynbot.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.manager.CommandManager;
import io.github.lynbean.lynbot.core.commands.processor.CommandProcessor;
import io.github.lynbean.lynbot.core.listeners.command.ContextCommandListener;
import io.github.lynbean.lynbot.core.listeners.command.SlashCommandInteractionListener;
import io.github.lynbean.lynbot.core.listeners.waiter.EventWaiter;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Builder
public class BotCore {

    private @Getter final CommandManager manager = new CommandManager(this);
    private @Getter final CommandRegistry registry = new CommandRegistry(manager);
    private @Getter final CommandProcessor processor = new CommandProcessor(registry);
    private @Getter final static List<BotCore> instances = new ArrayList<BotCore>();

    private final EventBus eventBus = new EventBus();
    private final static Logger LOG = LoggerFactory.getLogger(BotCore.class);

    private @Builder.Default @Getter @Nonnull final List<String> subBotTokens = new ArrayList<>();
    private @Getter @Nonnull final String mainBotToken;

    private @Builder.Default @Getter @Nonnull final List<String> cogsPackage = new ArrayList<>();
    private @Builder.Default @Getter @Nonnull final List<String> prefixes = new ArrayList<>();
    private @Getter final int shardsTotal = -1;

    private @Getter ShardManager mainShardManager;
    private @Getter ShardManager[] subShardManagers;

    private @Getter final EventWaiter eventWaiter = new EventWaiter();

    private final @Builder.Default @Getter @Nonnull CacheFlag[] cacheFlags = new CacheFlag[0];
    private final @Builder.Default @Getter @Nonnull ChunkingFilter chunkingFilter = ChunkingFilter.NONE;
    private final @Builder.Default @Getter @Nonnull GatewayIntent[] gateways = new GatewayIntent[0];
    private final @Builder.Default @Getter @Nonnull MemberCachePolicy memberCachePolicy = MemberCachePolicy.NONE;

    public static BotCoreBuilder builder() {
        return new CoreBuilder();
    }

    private static class CoreBuilder extends BotCoreBuilder {
        @Override
        public BotCoreBuilder mainShardManager(ShardManager mainShardManager) {
            LOG.warn("mainShardManager is ignored in BotCoreBuilder");
            return this;
        }
        @Override
        public BotCoreBuilder subShardManagers(ShardManager[] subShardManagers) {
            LOG.warn("subShardManagers is ignored in BotCoreBuilder");
            return this;
        }
        @Override
        public BotCore build() {
            BotCore botCore = super.build();
            BotCore.instances.add(botCore);
            return botCore;
        }
    }

    public static Set<Class<?>> lookForAnnotatedOn(String packageName, Class<? extends Annotation> annotation) {
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

    private ShardManager shardManagerBuilder(String token, boolean isMainBot) {
        DefaultShardManagerBuilder smb = DefaultShardManagerBuilder.create(token, List.of(gateways));
        smb.setShardsTotal(shardsTotal);

        if (isMainBot) {
            smb.enableCache(List.of(cacheFlags))
                .setChunkingFilter(chunkingFilter)
                .setMemberCachePolicy(memberCachePolicy)
                .addEventListeners(eventWaiter)
                .addEventListenerProviders(
                    List.of(
                        shardId -> new SlashCommandInteractionListener(processor),
                        shardId -> new ContextCommandListener(processor, prefixes)
                    )
                );
        }

        return smb.build(false);
    }

    public void run() {
        LOG.info("\u001B[31mBuilding Shard Managers for " + (subBotTokens.size() + 1) + " bots...\u001B[0m");
        mainShardManager = shardManagerBuilder(mainBotToken, true);
        subShardManagers = new ShardManager[subBotTokens.size()];
        for (int i = 0; i < subBotTokens.size(); i++) {
            subShardManagers[i] = shardManagerBuilder(subBotTokens.get(i), false);
        }

        LOG.info("\u001B[31mShard Managers built. Starting shards...\u001B[0m");
        ArrayList<ShardManager> shardManagers = new ArrayList<>();
        shardManagers.add(mainShardManager);
        shardManagers.addAll(List.of(subShardManagers));
        shardManagers.forEach(ShardManager::login);

        shardManagers.forEach(shardManager -> {
            User botUser = shardManager.getShards().get(0).getSelfUser();
            LOG.info(
                "\u001B[33m" + botUser.getName() + "#" + botUser.getDiscriminator() + " - " + botUser.getId() + " is ready!" +
                " (" + String.valueOf(shardManager.getShards().size()) + " shards)" + " (" + String.valueOf(shardManager.getGuilds().size()) + " guilds)" +
                " (" + String.valueOf(shardManager.getUsers().size()) + " users)" + " (GatewayPing " + String.valueOf(shardManager.getAverageGatewayPing()) + "ms)" +
                "\u001B[0m"
            );
        });

        LOG.info("\u001B[31mShards started. Registering commands...\u001B[0m");
        Set<Class<?>>[] classPackages = new Set[cogsPackage.size()];
        for (int i = 0; i < cogsPackage.size(); i++) {
            classPackages[i] = lookForAnnotatedOn(cogsPackage.get(i), Cog.class);
        }
        for (Set<Class<?>> clazzPackage : classPackages) {
            if (clazzPackage.isEmpty()) {
                LOG.warn("\u001B[33mNo commands found in package " + clazzPackage.getClass().getName() + "\u001B[0m");
                continue;
            }
            for (Class<?> clazz : clazzPackage) {
                try {
                    eventBus.register(clazz.getDeclaredConstructor().newInstance());
                    LOG.info("\u001B[33mRegistering cog: " + clazz.getName() + "\u001B[0m");
                } catch (Exception e) {
                    LOG.error("\u001B[33mFailed to register cog: " + clazz.getName() + "\u001B[0m", e);
                }
            }
        }
        try {
            eventBus.post(registry);
        } catch (Exception e) {
            LOG.error("\u001B[33mAn error has occured during registering cogs\u001B[0m", e);
        }

        LOG.info("\u001B[31mCommands registered. Syncing commands to Discord...\u001B[0m");
        List<CommandData> globalSlashCommandDatas = manager.getGlobalSlashCommandDatas();
        Map<String, List<CommandData>> guildSlashCommandDatas = manager.getGuildSlashCommandData();
        for (JDA mainBotJda : mainShardManager.getShards()) {
            mainBotJda.updateCommands()
                .addCommands(globalSlashCommandDatas)
                .queue();

            LOG.info("\u001B[33mSynced {} global commands for shard {}\u001B[0m", globalSlashCommandDatas.size(), mainBotJda.getShardInfo().getShardId());

            guildSlashCommandDatas.forEach(
                (guildId, slashCommandDatas) -> {
                    mainBotJda.getGuildById(guildId)
                        .updateCommands()
                        .addCommands(slashCommandDatas)
                        .queue();

                    LOG.info("\u001B[33mSynced {} guild commands for shard {} in guild {}\u001B[0m", slashCommandDatas.size(), mainBotJda.getShardInfo().getShardId(), guildId);
                }
            );
        }

        LOG.info("\u001B[31mCommands synced. Ready to go!\u001B[0m");
    }

    public void shutdown() {
        LOG.info("\u001B[31mShutting down...\u001B[0m");
        mainShardManager.shutdown();
        List.of(subShardManagers).forEach(ShardManager::shutdown);
        LOG.info("\u001B[31mShutdown complete.\u001B[0m");
    }

    public void restart() throws Exception {
        LOG.info("\u001B[31mInitiating restart...\u001B[0m");
        shutdown();
        eventBus.unregister(registry);
        manager.unregisterAll();
        run();
    }
}