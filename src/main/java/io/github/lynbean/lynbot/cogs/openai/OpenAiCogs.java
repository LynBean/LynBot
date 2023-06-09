package io.github.lynbean.lynbot.cogs.openai;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.openai.chat.Chat;
import io.github.lynbean.lynbot.cogs.openai.chatbox.ChatBox;
import io.github.lynbean.lynbot.cogs.openai.chatbox.ChatBoxDatabaseManager;
import io.github.lynbean.lynbot.cogs.openai.chatbox.listener.ChatBoxListener;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

@Cog
public class OpenAiCogs {
    private static final Logger LOG = LoggerFactory.getLogger(OpenAiCogs.class);

    @Subscribe
    public void register(CommandRegistry registry) {
        if (Bot.getConfig().getString("openai.key").isBlank()) {
            LOG.warn("OpenAI key is not provided. This cog will be disabled.");
            return;
        }

        registry.registerGlobalSlashCommand(OpenAi.class);
        registry.registerEventListeners(new ChatBoxListener());
    }

    @SlashCommandMeta(
        name = "openai",
        description = "The OpenAI API"
    )
    public static class OpenAi extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        /**
         * ChatGPT - Slash
         */
        @SlashCommandMeta(
            name = "chat",
            description = "The OpenAI GPT-3 API",
            options = {
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "model",
                    description = "The model to use."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "frequency-penalty",
                    description = "Number between -2.0 and 2.0. Positive values penalize new tokens.",
                    minDoubleValues = -2.0,
                    maxDoubleValues = 2.0
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.INTEGER,
                    name = "max-tokens",
                    description = "The maximum number of tokens to generate in the completion."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "presence-penalty",
                    description = "Number between -2.0 and 2.0. Positive values penalize new tokens.",
                    minDoubleValues = -2.0,
                    maxDoubleValues = 2.0
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "temperature",
                    description = "Try 0.9 for more creative applications, and 0 for ones with a well-defined answer.",
                    minDoubleValues = 0.0,
                    maxDoubleValues = 2.0
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "top-p",
                    description = "An alternative to sampling with temperature, called nucleus sampling.",
                    minDoubleValues = 0.0,
                    maxDoubleValues = 1.0
                )
            }
        )
        public static class ChatGPT extends SlashCommand {
            @Override
            protected List<OptionData> editOptions(List<OptionData> options) {
                List<String> models = Bot.getConfig().getStringList("openai.chat.models");

                if (models.size() > 0) {
                    options.stream()
                        .filter(option -> option.getName().equals("model"))
                        .findFirst()
                        .ifPresent(
                            option -> option.addChoices(
                                models.stream()
                                    .map(model -> new Choice(model, model))
                                    .collect(Collectors.toList())
                            )
                        );

                } else {
                    options.removeIf(option -> option.getName().equals("model"));
                }

                return super.editOptions(options);
            }

            @Override
            protected void process(SlashCommandInteractionEvent event) {
                // Send a modal to ask for the content
                TextInput askForContent = TextInput.create(
                    "content",
                    "What do you want to say?",
                    TextInputStyle.PARAGRAPH
                )
                    .setRequired(true)
                    .build();

                AtomicReference<String> content = new AtomicReference<>();
                CompletableFuture<Void> future = new CompletableFuture<>();

                event.replyModal(
                    Modal.create(
                        "openai.chatgpt.modal_%s".formatted(event.getId()),
                        "I need Coffee!"
                    )
                        .addComponents(ActionRow.of(askForContent))
                        .build()
                )
                    .queue(
                        success -> this.getCore().getEventWaiter().waitForEvent(
                            ModalInteractionEvent.class,
                            e -> e.getUser().equals(event.getUser()) && e.getModalId().equals("openai.chatgpt.modal_%s".formatted(event.getId())),
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

                User user = event.getUser();
                String model = event.getOption("model") != null
                    ? event.getOption("model").getAsString()
                    : Bot.getConfig().getString("openai.chat.default_model");

                // Send a title message
                event.getHook()
                    .sendMessageEmbeds(
                        new EmbedBuilder()
                            .setAuthor("%s is asking for".formatted(user.getName()), null, user.getAvatarUrl())
                            .setTitle(content.get().length() >= 256 ? content.get().substring(0, 256) : content.get())
                            .setFooter(
                                "Powered by %s".formatted(model),
                                "https://static-cdn.jtvnw.net/jtv_user_pictures/7f9a20f3-04ad-453c-a3a8-7cf25452084c-profile_image-300x300.png"
                            )
                            .build()
                    )
                    .complete();

                AtomicReference<Message> message = new AtomicReference<>();
                AtomicInteger pageCounter = new AtomicInteger(0);
                AtomicInteger rateCounter = new AtomicInteger(0);
                EmbedBuilder embed = new EmbedBuilder();

                // Setup completion and start
                Chat.fromString(content.get())
                    .setFrequencyPenalty(
                        Optional.ofNullable(event.getOption("frequency-penalty"))
                            .map(option -> option.getAsDouble())
                            .orElse(null)
                    )
                    .setMaxTokens(
                        Optional.ofNullable(event.getOption("max-tokens"))
                            .map(option -> option.getAsInt())
                            .orElse(null)
                    )
                    .setModel(model)
                    .setPresencePenalty(
                        Optional.ofNullable(event.getOption("presence-penalty"))
                            .map(option -> option.getAsDouble())
                            .orElse(null)
                    )
                    .setTemperature(
                        Optional.ofNullable(event.getOption("temperature"))
                            .map(option -> option.getAsDouble())
                            .orElse(null)
                    )
                    .setTopP(
                        Optional.ofNullable(event.getOption("top-p"))
                            .map(option -> option.getAsDouble())
                            .orElse(null)
                    )
                    .complete(
                        chunk -> {
                            String result = chunk.getChoices()
                                .get(0) // Probably only one choice
                                .getMessage()
                                .getContent();

                            if (result == null || result.isEmpty()) {
                                return;
                            }

                            // Discord embed description limit
                            if (embed.getDescriptionBuilder().length() + result.length() >= 2048) {
                                message.get().editMessageEmbeds(embed.build()).queue(); // Apply the latest changes
                                message.set(null);
                            }

                            if (message.get() != null) {
                                embed.appendDescription(result);

                            } else {
                                embed.setDescription(result);
                            }

                            if (message.get() != null) {
                                // Edit the message once every few updates
                                // to avoid this thread being blocked for too long
                                // due to Discord API rate limit
                                rateCounter.set(rateCounter.get() + 1);
                                if (rateCounter.get() % 50 == 0) {
                                    message.get().editMessageEmbeds(embed.build()).queue();
                                }

                            } else {
                                pageCounter.set(pageCounter.get() + 1);
                                embed.setFooter("Page %d".formatted(pageCounter.get()));
                                message.set(
                                    event.getHook().sendMessageEmbeds(
                                        embed.build()
                                    )
                                        .complete()
                                );
                            }
                        },
                        cause -> {
                            // Send the error message
                            LOG.error("An error occurred while processing a request", cause);
                            event.getHook()
                                .sendMessage(cause.getMessage())
                                .setEphemeral(true)
                                .queue();
                        },
                        () -> {
                            // Apply the latest changes before exiting
                            if (message.get() != null) {
                                message.get().editMessageEmbeds(embed.build()).queue();

                            } else {
                                pageCounter.set(pageCounter.get() + 1);
                                embed.setFooter("Page %d".formatted(pageCounter.get()));
                                event.getHook().sendMessageEmbeds(embed.build()).queue();
                            }
                        }
                    );
            }
        }

        /**
         * ChatGPT - Create/Initialize ChatBox
         */
        @SlashCommandMeta(
            name = "chatbox",
            description = "A place where you can date with your favorite character.",
            options = {
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "character",
                    description = "The presets made by LynBean.",
                    required = false
                )
            }
        )
        public static class ChatGPTBox extends SlashCommand {
            @Override
            protected List<OptionData> editOptions(List<OptionData> options) {
                List<Choice> choices = ChatBoxDatabaseManager.getPresetChoices();

                if (choices.size() > 0) {
                    options.stream()
                        .filter(option -> option.getName().equals("character"))
                        .findFirst()
                        .ifPresent(option -> option.addChoices(choices));

                } else {
                    options.removeIf(option -> option.getName().equals("character"));
                }

                return super.editOptions(options);
            }

            @Override
            protected void process(SlashCommandInteractionEvent event) {
                if (event.getChannel().getType().isThread()) {
                    event.reply("This command cannot be used in a thread.")
                        .setEphemeral(true)
                        .queue();
                    return;
                }

                event.deferReply().queue();

                String presetId = Optional.ofNullable(event.getOption("character"))
                    .map(option -> option.getAsString())
                    .orElse(null);

                Message originalMessage = event.getHook().retrieveOriginal().complete();
                ChatBox chatBox;

                if (presetId == null) {
                    // Use default preset
                    chatBox = ChatBox.create(originalMessage, event.getUser());

                } else {
                    // Use preset
                    chatBox = ChatBox.create(originalMessage, event.getUser(), presetId);
                }

                MessageEditData message = new MessageEditBuilder()
                    .setEmbeds(
                        new EmbedBuilder()
                            .setAuthor(
                                Optional.ofNullable(chatBox.getPreset().getCharacterName())
                                    .orElse(event.getJDA().getSelfUser().getName()),
                                null,
                                Optional.ofNullable(chatBox.getPreset().getCharacterIconUrl())
                                    .orElse(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            )
                            .setTitle(chatBox.getPreset().getTitle())
                            .setDescription(chatBox.getPreset().getDescription())
                            .build()
                    )
                    .build();

                event.getHook()
                    .editOriginal(message)
                    .queue();
            }
        }
    }
}