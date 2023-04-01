package io.github.lynbean.lynbot.cogs;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.cogs.epicgames.promotions.FreeGamesHandler;
import io.github.lynbean.lynbot.cogs.epicgames.promotions.common.Game;
import io.github.lynbean.lynbot.cogs.util.CogsUtil;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import io.github.lynbean.lynbot.core.thread.ThreadController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Cog
public class EpicGamesCogs {

    @Subscribe
    public void register(CommandRegistry registry) {
        registry.registerSlashCommand(EpicGames.class);
        registry.registerEventListeners(new EpicGamesListener());
    }

    public static class EpicGamesListener extends ListenerAdapter {
        @Override
        public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
            ThreadController.commandExecutor.execute(() -> {
                if (event.getSelectMenu().getId().equalsIgnoreCase("epicgames-freeweekly-gameindex")) {
                    event.deferEdit();
                    try {
                        EpicGames.FreeWeekly.reloadGamePool();
                    } catch (Exception e) {
                        event.getHook()
                            .editOriginalEmbeds(CogsUtil.exceptionEmbed(e, event))
                            .queue();

                        return;
                    }
                    event.editMessageEmbeds(
                        EpicGames.FreeWeekly.gameEmbed(
                            EpicGames.FreeWeekly.gamePool.get(Integer.parseInt(event.getValues().get(0)))
                        )
                    )
                        .queue();
                }
            });
        }
    }

    @SlashCommandMeta(
        name = "epicgames",
        description = "Epic Games."
    )
    public static class EpicGames extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "free-weekly",
            description = "Weekly free games from Epic Games Store."
        )
        public static class FreeWeekly extends SlashCommand {
            private static List<Game> gamePool;

            private static void reloadGamePool() throws IOException, ParseException {
                gamePool = FreeGamesHandler.getGamePool();
            }

            @Override
            protected void process(SlashCommandInteractionEvent event) {
                try {
                    reloadGamePool();
                } catch (Exception e) {
                    event.getHook()
                        .editOriginalEmbeds(CogsUtil.exceptionEmbed(e, event))
                        .queue();

                    return;
                }

                List<SelectOption> options = new ArrayList<>();

                for (int i = 0; i < gamePool.size(); i++) {
                    Game game = gamePool.get(i);
                    options.add(SelectOption.of(
                        game.getTitle(), String.valueOf(i)
                    ));
                }

                SelectMenu menu = StringSelectMenu.create("epicgames-freeweekly-gameindex")
                    .setPlaceholder("Take a look for this week free games?")
                    .addOptions(options)
                    .build();

                event.getHook()
                    .editOriginalEmbeds(gameEmbed(gamePool.get(0)))
                    .setActionRow(menu)
                    .queue();
            }

            private static MessageEmbed gameEmbed(Game game) {
                return new EmbedBuilder()
                    .setAuthor(game.getPromoDate())
                    .setTitle(game.getTitle(), game.getUrl())
                    .setDescription(game.getDescription())
                    .setImage(game.getImageUrl())
                    .addField("Original Price", game.getPricing(), true)
                    .addField("Seller", game.getSeller(), true)
                    .build();
            }
        }
    }
}
