package io.github.lynbean.lynbot.cogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.cogs.gpt.chat.BotChatCompletion;
import io.github.lynbean.lynbot.cogs.gpt.core.GPTApplication;
import io.github.lynbean.lynbot.cogs.gpt.edit.BotEditCompletion;
import io.github.lynbean.lynbot.cogs.gpt.image.BotImageCompletion;
import io.github.lynbean.lynbot.cogs.gpt.image.ImageComposer;
import io.github.lynbean.lynbot.cogs.util.CogsUtil;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.ContextCommand;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.ContextCommandMeta;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import io.github.lynbean.lynbot.core.database.ConfigManager;
import io.github.lynbean.lynbot.core.thread.ThreadController;
import io.github.lynbean.lynbot.util.BotLogger;
import io.github.lynbean.lynbot.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

@Cog
public class GPTCogs {
    private static final Logger log = BotLogger.getLogger(GPTCogs.class);
    private static final GPTApplication app = new GPTApplication();

    @Subscribe
    public void register(CommandRegistry registry) {
        if (ConfigManager.get("openai-key") == null) {
            log.warn("OpenAI key not found in config, skipping {} registration", GPTCogs.class.getSimpleName());
            return;
        }

        registry.registerConfigProperties(app.getConfig().getProperties());
        registry.registerSlashCommand(GPT.class);
        registry.registerContextCommand(ContextChatGPT.class);
    }

    @ContextCommandMeta(name = "chat")
    public static class ContextChatGPT extends ContextCommand {
        @Override
        protected void process(MessageReceivedEvent event, String rawContent) {
            Message message = event.getMessage();
            User user = event.getAuthor();

            List<String> content = new ArrayList<>();
            content.add(rawContent);

            if (message.getAttachments().size() > 0) {
                HashMap<String, byte[]> attachments = new HashMap<>();

                for (Attachment attachment : message.getAttachments()) {
                    try {
                        if (attachment.isImage()) {
                            byte[] bytes = Util.URLtoByteArray(attachment.getUrl());
                            attachments.put(
                                "image", bytes
                            );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                content.add(attachments.toString());
            }

            BotChatCompletion completion = new BotChatCompletion(
                app.getService(), ThreadController.commandExecutor, content.toString(), user.getId()
            );

            completion.setUncaughtExceptionHandler(
                (thread, throwable) -> {
                    message.reply(
                        String.format(
                            "%s %s", user.getAsMention(), throwable.getMessage()
                        )
                    )
                    .queue();
                }
            );

            completion.setOnError(
                (error) -> {
                    message.reply(
                        String.format(
                            "%s %s", user.getAsMention(), error.getMessage()
                        )
                    )
                    .queue();
                }
            );

            completion.setMaxNumOfCharsPerResponse(2000);
            completion.run();
            log.info(completion.toString());

            for (int i = 0; !completion.isDone(); i++) {
                Message responseMessage = message.reply(
                    "Thinking..."
                )
                    .complete();

                while (!completion.isDone()) {
                    try {Thread.sleep(1000);}
                    catch (InterruptedException ignored) {}
                    List<String> responses = completion.getResponses();

                    if (responses.size() < i + 1) continue;

                    String response = responses.get(i);
                    responseMessage.editMessage(response)
                        .queue();

                    if (responses.size() > i + 1) break;
                }

                if (!completion.isDone()) continue;

                // Has to make sure that the last response is sent
                List<String> responses = completion.getResponses();
                if (responses.size() < i + 1) break;
                String response = responses.get(i);

                responseMessage.editMessage(response)
                    .queue();
            }
        }
    }

    @SlashCommandMeta(
        name = "gpt",
        description = "The OpenAI GPT-3 API"
    )
    public static class GPT extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "chat",
            description = "The OpenAI GPT-3 API",
            options = {
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "content",
                    description = "Content.",
                    required = true
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "model",
                    description = "The model to use."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "role",
                    description = "The role of the AI."
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
            private String content;
            private String model;
            private String role;
            private Double frequencyPenalty;
            private Integer maxTokens;
            private Double presencePenalty;
            private Double temperature;
            private Double topP;
            private User user;

            @Override
            protected void process(SlashCommandInteractionEvent event) {
                OptionMapping contentOption = event.getOption("content");
                OptionMapping modelOption = event.getOption("model");
                OptionMapping roleOption = event.getOption("role");
                OptionMapping frequencyPenaltyOption = event.getOption("frequency-penalty");
                OptionMapping maxTokensOption = event.getOption("max-tokens");
                OptionMapping presencePenaltyOption = event.getOption("presence-penalty");
                OptionMapping temperatureOption = event.getOption("temperature");
                OptionMapping topPOption = event.getOption("top-p");

                content = contentOption.getAsString();
                if (frequencyPenaltyOption != null) frequencyPenalty = frequencyPenaltyOption.getAsDouble();
                if (maxTokensOption != null) maxTokens = maxTokensOption.getAsInt();
                if (modelOption != null) model = modelOption.getAsString();
                if (presencePenaltyOption != null) presencePenalty = presencePenaltyOption.getAsDouble();
                if (roleOption != null) role = roleOption.getAsString();
                if (temperatureOption != null) temperature = temperatureOption.getAsDouble();
                if (topPOption != null) topP = topPOption.getAsDouble();
                user = event.getUser();

                MessageEmbed startEmbed = new EmbedBuilder()
                    .setAuthor(user.getName() + " is asking for", null, user.getAvatarUrl())
                    .setTitle(content.length() >= 256 ? content.substring(0, 256) : content)
                    .setFooter(
                        "Powered by " +
                        (modelOption != null ? modelOption.getAsString() : ConfigManager.get("chat-model")),
                        "https://openai.com/content/images/2022/05/openai-avatar.png"
                    )
                    .build();

                event.getHook()
                    .editOriginalEmbeds(startEmbed)
                    .complete();

                BotChatCompletion completion = new BotChatCompletion(
                    app.getService(), ThreadController.commandExecutor, content, user.getId()
                );

                if (frequencyPenalty != null) completion.setFrequencyPenalty(frequencyPenalty);
                if (maxTokens != null) completion.setMaxTokens(maxTokens);
                if (model != null) completion.setModel(model);
                if (presencePenalty != null) completion.setPresencePenalty(presencePenalty);
                if (role != null) completion.setRole(role);
                if (temperature != null) completion.setTemperature(temperature);
                if (topP != null) completion.setTopP(topP);

                completion.setUncaughtExceptionHandler(
                    (thread, throwable) -> {
                        event.getHook()
                            .sendMessageEmbeds(
                                CogsUtil.exceptionEmbed(
                                    (Exception) throwable, event
                                )
                            )
                            .queue();
                    }
                );

                completion.setOnError(
                    (error) -> {
                        event.getHook()
                            .sendMessageEmbeds(
                                CogsUtil.exceptionEmbed(
                                    (Exception) error, event
                                )
                            )
                            .queue();
                    }
                );

                completion.run();
                log.info(completion.toString());

                for (int i = 0; !completion.isDone(); i++) {
                    Message message = event.getHook()
                        .sendMessageEmbeds(
                            new EmbedBuilder()
                                .setDescription("Thinking...")
                                .build()
                        )
                        .complete();

                    while (!completion.isDone()) {
                        try {Thread.sleep(1000);}
                        catch (InterruptedException ignored) {}
                        List<String> responses = completion.getResponses();

                        if (responses.size() < i + 1) continue;

                        String response = responses.get(i);
                        message.editMessageEmbeds(
                            new EmbedBuilder()
                                .setDescription(response)
                                .build()
                        )
                            .queue();

                        if (responses.size() > i + 1) break;
                    }

                    if (!completion.isDone()) continue;

                    // Has to make sure that the last response is sent
                    List<String> responses = completion.getResponses();
                    if (responses.size() < i + 1) break;
                    String response = responses.get(i);

                    message.editMessageEmbeds(
                        new EmbedBuilder()
                            .setDescription(response)
                            .build()
                    )
                        .queue();
                }
            }

            @Override
            protected OptionData optionDataEditor(OptionData data) {
                if (data.getName().equalsIgnoreCase("model") && data.getType().canSupportChoices()) {
                    List.of(
                        ConfigManager.get("chat-models")
                            .split(",")
                    )
                        .stream()
                        .map(String::trim)
                        .forEach(model -> data.addChoice(model, model));
                }

                if (data.getName().equalsIgnoreCase("role") && data.getType().canSupportChoices()) {
                    List.of(
                        ConfigManager.get("chat-roles")
                            .split(",")
                    )
                        .stream()
                        .map(String::trim)
                        .forEach(role -> data.addChoice(role, role));
                }

                return super.optionDataEditor(data);
            }
        }

        @SlashCommandMeta(
            name = "image",
            description = "The OpenAI GPT-3 API",
            options = {
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "prompt",
                    description = "A text description of the desired image(s). The maximum length is 1000 characters.",
                    required = true
                )
            }
        )
        public static class ImageGPT extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                OptionMapping promptOption = event.getOption("prompt");

                String prompt = promptOption.getAsString();
                String userID = event.getUser().getId();

                MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getUser().getName() + " is asking for", null, event.getUser().getAvatarUrl())
                    .setTitle(prompt.length() >= 256 ? prompt.substring(0, 256) : prompt)
                    .setDescription("Drawing...")
                    .setFooter(
                        "Powered by DALL·E",
                        "https://openai.com/content/images/2022/05/openai-avatar.png"
                    )
                    .build();

                final Message message = event.getHook()
                    .editOriginalEmbeds(embed)
                    .complete();

                BotImageCompletion image = new BotImageCompletion(
                    app.getService(), ThreadController.commandExecutor, prompt, userID
                );

                image.setN(4);
                image.setSize("1024x1024");

                final Throwable[] throwable = new Throwable[1];
                image.setUncaughtExceptionHandler(
                    (t, e) -> {
                        log.debug(e.getMessage());
                        throwable[0] = e;
                        message.editMessageEmbeds(
                            CogsUtil.exceptionEmbed((Exception) e, event)
                        )
                            .queue();
                    }
                );

                image.run();
                image.await();
                log.info(image.toString());

                // If an exception was thrown, return
                if (throwable[0] != null)
                    return;

                List<String> imageUrls = image.getImageUrls();

                ImageComposer composer = ImageComposer.builder()
                    .topLeftURL(imageUrls.get(0))
                    .topRightURL(imageUrls.get(1))
                    .bottomLeftURL(imageUrls.get(2))
                    .bottomRightURL(imageUrls.get(3))
                    .build();

                byte[] imageBytes;
                try {
                    imageBytes = composer.writeImageToBytes();
                } catch (IOException e) {
                    message.editMessageEmbeds(
                        CogsUtil.exceptionEmbed((Exception) e, event)
                    )
                        .queue();

                    return;
                }

                List<ItemComponent> itemComponents = List.of(
                    Button.link(imageUrls.get(0), "A"),
                    Button.link(imageUrls.get(1), "B"),
                    Button.link(imageUrls.get(2), "C"),
                    Button.link(imageUrls.get(3), "D")
                );

                try (FileUpload upload = FileUpload.fromData(imageBytes, imageBytes.toString() + ".png")) {
                    message.editMessageFormat("> %s is asking for %s", event.getUser().getAsMention(), prompt)
                        .setEmbeds(Collections.emptyList())
                        .setAttachments(upload)
                        .setActionRow(itemComponents)
                        .queue();
                } catch (Exception e) {
                    message.editMessageEmbeds(
                        CogsUtil.exceptionEmbed((Exception) e, event)
                    )
                        .queue();

                    return;
                }
            }
        }

        @SlashCommandMeta(
            name = "edit",
            description = "The OpenAI GPT-3 API",
            options = {
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "input",
                    description = "The input text to use as a starting point for the edit.",
                    required = true
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "instruction",
                    description = "The instruction that tells the model how to edit the prompt.",
                    required = true
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "model",
                    description = "ID of the model to use."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.INTEGER,
                    name = "n",
                    description = "How many edits to generate for the input and instruction.",
                    minIntValues = 1,
                    maxIntValues = 20
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
        public static class EditGPT extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                OptionMapping inputOption = event.getOption("input");
                OptionMapping instructionOption = event.getOption("instruction");
                OptionMapping nOption = event.getOption("n");
                OptionMapping temperatureOption = event.getOption("temperature");
                OptionMapping topPOption = event.getOption("top-p");

                String input = inputOption.getAsString();
                String instruction = instructionOption.getAsString();

                MessageEmbed embed = new EmbedBuilder()
                    .setAuthor(event.getUser().getName() + " is asking for", null, event.getUser().getAvatarUrl())
                    .setTitle(instruction.length() >= 256 ? instruction.substring(0, 256) : instruction)
                    .setDescription(input.length() >= 4096 ? input.substring(0, 4096) : input)
                    .setFooter(
                        "Powered by OpenAI",
                        "https://openai.com/content/images/2022/05/openai-avatar.png"
                    )
                    .build();

                event.getHook()
                    .editOriginalEmbeds(embed)
                    .complete();

                BotEditCompletion edit = new BotEditCompletion(
                    app.getService(), ThreadController.commandExecutor, input, instruction
                );

                if (nOption != null) edit.setN(nOption.getAsInt());
                if (temperatureOption != null) edit.setTemperature(temperatureOption.getAsDouble());
                if (topPOption != null) edit.setTopP(topPOption.getAsDouble());

                final Throwable[] throwable = new Throwable[1];
                edit.setUncaughtExceptionHandler(
                    (t, e) -> {
                        log.debug(e.getMessage());
                        throwable[0] = e;
                        event.getHook()
                            .sendMessageEmbeds(
                                CogsUtil.exceptionEmbed((Exception) e, event)
                            )
                            .queue();
                    }
                );

                edit.run();
                edit.await();
                log.info(edit.toString());

                // If an exception was thrown, return
                if (throwable[0] != null)
                    return;

                List<String> response = edit.getResponse();

                for (int i = 0; i < response.size(); i++) {
                    ListIterator<String> results = CogsUtil.trimMessage(response.get(i), 4096)
                        .listIterator();

                    while (results.hasNext()) {
                        event.getHook()
                            .sendMessageEmbeds(
                                new EmbedBuilder()
                                    .setDescription(results.next())
                                    .build()
                            )
                            .queue();
                    }
                }
            }

            @Override
            protected OptionData optionDataEditor(OptionData data) {
                if (data.getName().equalsIgnoreCase("model") && data.getType().canSupportChoices()) {
                    List.of(
                        ConfigManager.get("edit-models")
                            .split(",")
                    )
                        .stream()
                        .map(String::trim)
                        .forEach(model -> data.addChoice(model, model));
                }

                return super.optionDataEditor(data);
            }
        }
    }
}