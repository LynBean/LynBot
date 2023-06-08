package io.github.lynbean.lynbot.cogs.epicgames;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.cogs.epicgames.entities.Game;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Cog
public class EpicGamesCogs {
    @Subscribe
    public void register(CommandRegistry registry) {
        registry.registerGlobalSlashCommand(EpicGames.class);
    }

    @SlashCommandMeta(
        name = "egs",
        description = "Epic Games."
    )
    public static class EpicGames extends SlashCommand {
        @Override
        public void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "free-weekly",
            description = "Epic Games Free Weekly."
        )
        public static class FreeWeekly extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();

                PromotionGetter promotion = new PromotionGetter();

                try {
                    promotion.update();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                List<Game> games = promotion.getGames();

                if (games.isEmpty()) {
                    event.getHook()
                        .sendMessage("No free games this week!")
                        .queue();

                    return;
                }

                List<SelectOption> options = games.stream()
                    .map(game -> SelectOption.of(game.getTitle(), game.getTitle()))
                    .collect(Collectors.toList());

                SelectMenu menu = StringSelectMenu.create("epicgames.freeweekly.select_%s".formatted(event.getId()))
                    .setPlaceholder("Take your pick!")
                    .addOptions(options)
                    .build();

                event.getHook()
                    .editOriginalEmbeds(createEmbed(games.get(0)))
                    .setActionRow(menu)
                    .queue(
                        message -> getCore().getEventWaiter().waitForEvent(
                            StringSelectInteractionEvent.class,
                            e -> e.getComponentId().equals("epicgames.freeweekly.select_%s".formatted(event.getId())),
                            e -> {
                                e.deferEdit().queue();

                                String title = e.getSelectedOptions().get(0).getValue();
                                Game game = games.stream()
                                    .filter(g -> g.getTitle().equals(title))
                                    .findFirst()
                                    .orElseThrow();

                                e.getHook()
                                    .editOriginalEmbeds(createEmbed(game))
                                    .queue();
                            },
                            300,
                            TimeUnit.SECONDS,
                            () -> event.getHook()
                                .editOriginalComponents(
                                    message.getComponents()
                                        .stream()
                                        .map(LayoutComponent::asDisabled)
                                        .collect(Collectors.toList())
                                )
                                .queue(),
                            true
                        )
                    );
            }

            private static MessageEmbed createEmbed(Game game) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

                String startDate = Optional.ofNullable(game.getStartDate())
                    .map(date -> formatter.format(date))
                    .orElse("Now");

                String endDate = formatter.format(game.getEndDate());

                return new EmbedBuilder()
                    .setAuthor("Free %s - %s".formatted(startDate, endDate))
                    .setTitle(game.getTitle(), game.getUrl())
                    .setDescription(game.getDescription())
                    .setImage(game.getImageUrl())
                    .addField("💸 Original Price", game.getPricing(), true)
                    .addField("🤑 Seller", game.getSeller(), true)
                    .build();
            }
        }
    }
}
