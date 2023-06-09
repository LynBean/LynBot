package io.github.lynbean.lynbot.cogs.google;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Result.Image;
import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

@Cog
public class GoogleCogs {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleCogs.class);

    @Subscribe
    public void register(CommandRegistry registry) {
        if (
            Bot.getConfig().getString("google.key") == null ||
            Bot.getConfig().getString("google.cx") == null
        ) {
            LOG.warn("Google key or cx not found, disabling google cog");
            return;
        }

        registry.registerGlobalSlashCommand(Google.class);
    }

    @SlashCommandMeta(
        name = "google",
        description = "Google something"
    )
    public static class Google extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "search",
            description = "Search something on google"
        )
        public static class CustomSearch extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                // Send a modal to ask for the content
                TextInput askForContent = TextInput.create(
                    "content",
                    "What do you want to search?",
                    TextInputStyle.PARAGRAPH
                )
                    .setRequired(true)
                    .build();

                AtomicReference<String> content = new AtomicReference<>();
                CompletableFuture<Void> future = new CompletableFuture<>();

                event.replyModal(
                    Modal.create(
                        "google.customsearch.modal_%s".formatted(event.getId()),
                        "I need Coffee!"
                    )
                        .addComponents(ActionRow.of(askForContent))
                        .build()
                )
                    .queue(
                        success -> this.getCore().getEventWaiter().waitForEvent(
                            ModalInteractionEvent.class,
                            e -> e.getUser().equals(event.getUser()) && e.getModalId().equals("google.customsearch.modal_%s".formatted(event.getId())),
                            e -> {
                                e.getInteraction().deferEdit().queue();
                                content.set(e.getValue("content").getAsString());
                                future.complete(null); // Mark the future as complete
                            },
                            1,
                            TimeUnit.HOURS,
                            () -> future.complete(null) // Mark the future as complete
                        )
                    );

                future.join(); // Wait for the content to be set before continuing

                if (content.get() == null) // It's a timeout
                    return;

                List<Result> results = GoogleChat.search(content.get());

                if (results == null) {
                    event.getHook()
                        .sendMessage("No results found")
                        .setEphemeral(true)
                        .queue();
                    return;
                }

                event.getHook()
                    .sendMessageEmbeds(
                        new EmbedBuilder()
                            .setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl())
                            .setTitle("Search results for %s".formatted(content.get()))
                            .build()
                    )
                    .complete();

                results.forEach(
                    result -> event.getHook()
                        .sendMessageEmbeds(
                            new EmbedBuilder()
                                .setTitle(result.getTitle(), result.getLink())
                                .setDescription(result.getSnippet())
                                .setImage(
                                    Optional.ofNullable(result.getImage())
                                        .map(Image::getContextLink)
                                        .orElse(null)
                                )
                                .build()
                        )
                        .queue()
                );
            }
        }
    }
}
