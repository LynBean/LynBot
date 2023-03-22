package com.kim.discordbot.cogs;

import com.google.common.eventbus.Subscribe;
import com.kim.discordbot.cogs.gpt.chat.BotChatCompletion;
import com.kim.discordbot.cogs.gpt.core.GPTApplication;
import com.kim.discordbot.cogs.gpt.image.BotImageCompletion;
import com.kim.discordbot.cogs.gpt.image.ImageComposer;
import com.kim.discordbot.cogs.gpt.util.GPTUtil;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.CommandRegistry;
import com.kim.discordbot.core.commands.ContextCommand;
import com.kim.discordbot.core.commands.meta.ContextCommandMeta;
import com.kim.discordbot.core.commands.meta.SlashCommandMeta;
import com.kim.discordbot.core.commands.SlashCommand;
import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.FileUpload;

@Cog
public class GPTCogs {
    private static final GPTApplication app = new GPTApplication();

    @Subscribe
    public void register(CommandRegistry registry) {
        registry.registerConfigProperties(app.getPropertiesConfig());
        registry.registerSlashCommand(GPT.class);
        registry.registerContextCommand(ContextChatGPT.class);
    }

    @ContextCommandMeta(name = "chat")
    public static class ContextChatGPT extends ContextCommand {
        @Override
        protected void process(MessageReceivedEvent event, String rawContent) {
            Message message = event.getMessage();
            String userID = event.getAuthor().getId();

            List<String> content = new ArrayList<>();
            content.add(rawContent);

            if (message.getAttachments().size() > 0) {
                HashMap<String, byte[]> attachments = new HashMap<>();

                for (Attachment attachment : message.getAttachments()) {
                    try {
                        if (attachment.isImage()) {
                            attachments.put(
                                "image", Util.URLtoByteArray(attachment.getUrl())
                            );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                content.add(attachments.toString());
            }

            List<String> response = GPT.chatGptProcess(content.toString(), userID);

            for (String resp : response) {
                List<String> trimmed = GPTUtil.trimMessage(resp, 2000);

                for (int i = 0; i < trimmed.size(); i++) {
                    if (i == 0) {
                        message.reply(
                            String.format("%s %s", message.getAuthor().getAsMention(), trimmed.get(i))
                        )
                            .queue();
                        continue;
                    }

                    message.getChannel()
                        .sendMessage(trimmed.get(i))
                        .queue();
                }
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
                    description = "Number between -2.0 and 2.0. Positive values penalize new tokens."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.INTEGER,
                    name = "max-tokens",
                    description = "The maximum number of tokens to generate in the completion."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "presence-penalty",
                    description = "Number between -2.0 and 2.0. Positive values penalize new tokens."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "temperature",
                    description = "Try 0.9 for more creative applications, and 0 for ones with a well-defined answer."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "top-p",
                    description = "An alternative to sampling with temperature, called nucleus sampling."
                )
            }
        )
        public static class ChatGPT extends SlashCommand {
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

                String content = contentOption.getAsString();
                String userID = event.getUser().getId();

                MessageEmbed startEmbed = new EmbedBuilder()
                    .setAuthor(event.getUser().getName() + " is asking for", null, event.getUser().getAvatarUrl())
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

                final List<String> response;

                try {
                    response = chatGptProcess(
                        content,
                        (modelOption != null ? modelOption.getAsString() : null),
                        (roleOption != null ? roleOption.getAsString() : null),
                        (frequencyPenaltyOption != null ? frequencyPenaltyOption.getAsString() : null),
                        (maxTokensOption != null ? maxTokensOption.getAsString() : null),
                        (presencePenaltyOption != null ? presencePenaltyOption.getAsString() : null),
                        (temperatureOption != null ? temperatureOption.getAsString() : null),
                        (topPOption != null ? topPOption.getAsString() : null),
                        userID
                    );
                } catch (RuntimeException e) {
                    event.getHook()
                        .sendMessageEmbeds(
                            new EmbedBuilder()
                                .setAuthor("Error", null, event.getUser().getAvatarUrl())
                                .setTitle(e.getClass().getName())
                                .setDescription(e.getMessage())
                                .build()
                        )
                        .queue();

                    return;
                }

                for (int i = 0; i < response.size(); i++) {
                    ListIterator<String> results = GPTUtil.trimMessage(response.get(i), 4096)
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

        private static List<String> chatGptProcess(String content, String userID) {
            return chatGptProcess(
                content, null, null, null,
                null, null, null, null, userID
            );
        }

        private static List<String> chatGptProcess(
            String content, String model, String role,
            String frequencyPenalty, String maxTokens, String presencePenalty,
            String temperature, String topP, String userID
        ) throws RuntimeException {
            BotChatCompletion chat = BotChatCompletion.builder()
                .build();

            chat.setService(app.getService());
            chat.setPrompt(content);

            if (model != null)
                chat.setModel(model);
            if (role != null)
                chat.setRole(role);
            if (frequencyPenalty != null)
                chat.setFrequencyPenalty(Double.parseDouble(frequencyPenalty));
            if (maxTokens != null)
                chat.setMaxTokens(Integer.parseInt(maxTokens));
            if (presencePenalty != null)
                chat.setPresencePenalty(Double.parseDouble(presencePenalty));
            if (temperature != null)
                chat.setTemperature(Double.parseDouble(temperature));
            if (topP != null)
                chat.setTopP(Double.parseDouble(topP));

            chat.setUser(userID);
            chat.buildRequest();

            chat.getThread().setUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException(e);
            });

            chat.runRequest();
            chat.await();

            return chat.getResponse();
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

                BotImageCompletion image = BotImageCompletion.builder()
                    .build();

                image.setService(app.getService());
                image.setPrompt(prompt);
                image.setUser(userID);
                image.setN(4);
                image.setSize("1024x1024");

                image.buildRequest();

                image.getThread().setUncaughtExceptionHandler((t, e) -> {
                    message.editMessageEmbeds(
                        GPTUtil.exceptionEmbed((Exception) e, event)
                    )
                        .queue();

                    return;
                });

                image.runRequest();
                image.await();
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
                        GPTUtil.exceptionEmbed((Exception) e, event)
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
                        GPTUtil.exceptionEmbed((Exception) e, event)
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
                    description = "The instruction that tells the model how to edit the prompt."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.STRING,
                    name = "n",
                    description = "How many edits to generate for the input and instruction."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "temperature",
                    description = "Try 0.9 for more creative applications, and 0 for ones with a well-defined answer."
                ),
                @SlashCommandMeta.Option(
                    type = OptionType.NUMBER,
                    name = "top-p",
                    description = "An alternative to sampling with temperature, called nucleus sampling."
                )
            }
        )
        public static class EditGPT extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.getHook()
                    .editOriginalEmbeds(
                        GPTUtil.exceptionEmbed(
                            new UnsupportedOperationException("This command is not implemented yet."),
                            event
                        )
                    )
                    .queue();
                // TODO: Implements this method
            }
        }
    }
}
