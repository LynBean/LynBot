package io.github.lynbean.lynbot.cogs.openai.chatbox.listener;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.lynbean.lynbot.cogs.openai.chat.Chat;
import io.github.lynbean.lynbot.cogs.openai.chatbox.ChatBox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatBoxListener extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ChatBoxListener.class);

    private static WebhookClient getWebhookClient(ThreadChannel threadChannel) {
        TextChannel textChannel = threadChannel.getParentMessageChannel().asTextChannel();
        List<Webhook> webhooks = textChannel.retrieveWebhooks()
            .complete()
            .stream()
            .filter(hook -> hook.getName().equalsIgnoreCase("ChatBox"))
            .collect(Collectors.toList());

        String webhookName = "ChatBox";

        if (webhooks.isEmpty()) {
            return WebhookClient.withUrl(textChannel.createWebhook(webhookName).complete().getUrl())
                .onThread(threadChannel.getIdLong());
        }

        return WebhookClient.withUrl(webhooks.get(0).getUrl())
            .onThread(threadChannel.getIdLong());
    }

    private static void editWebhookMessage(WebhookClient client, long messageId, MessageEmbed embed) {
        try {
            client.edit(
                messageId,
                WebhookEmbedBuilder.fromJDA(embed)
                    .build()
            )
                .get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static ReadonlyMessage sendWebhookMessage(WebhookClient client, MessageEmbed embed, String avatarUrl, String username) {
        try {
            return client.send(
                new WebhookMessageBuilder()
                    .addEmbeds(
                        WebhookEmbedBuilder.fromJDA(embed)
                            .build()
                    )
                    .setAvatarUrl(avatarUrl)
                    .setUsername(username)
                    .build()
            )
                .get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (
            event.getAuthor().isBot() ||
            event.getAuthor().isSystem() ||
            !event.getChannelType().isThread() ||
            !event.getChannel().getName().startsWith("ChatBox")
        ) {
            return;
        }

        User selfUser = event.getJDA().getSelfUser();
        ThreadChannel channel = event.getChannel().asThreadChannel();
        WebhookClient webhook = getWebhookClient(channel);
        ChatBox chatBox = new ChatBox(channel);

        chatBox.execute(
            (messages, preset) -> {
                Chat chat = new Chat(messages);

                // // DEBUG purposes
                // messages.stream()
                //     .map(ChatMessage::toString)
                //     .forEach(message -> System.out.println("\n" + message + "\n"));

                AtomicReference<ReadonlyMessage> message = new AtomicReference<ReadonlyMessage>();
                AtomicInteger pageCounter = new AtomicInteger(0);
                AtomicInteger rateCounter = new AtomicInteger(0);
                EmbedBuilder embed = new EmbedBuilder();

                chat.complete(
                    chunk -> {
                        chunk.getChoices()
                            .stream()
                            .forEachOrdered( // Mostly it will only have one choice, but just in case
                                choice -> {
                                    String result = choice.getMessage().getContent();

                                    if (result == null || result.isEmpty()) {
                                        return;
                                    }

                                    // Discord embed description limit
                                    if (embed.getDescriptionBuilder().length() + result.length() >= 2048) {
                                        // Apply the latest changes
                                        editWebhookMessage(
                                            webhook,
                                            message.get().getId(),
                                            embed.build()
                                        );
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
                                            editWebhookMessage(
                                                webhook,
                                                message.get().getId(),
                                                embed.build()
                                            );
                                        }

                                    } else {
                                        pageCounter.set(pageCounter.get() + 1);
                                        embed.setFooter("Page %d".formatted(pageCounter.get()));
                                        message.set(
                                            sendWebhookMessage(
                                                webhook,
                                                embed.build(),
                                                preset.getCharacterIconUrl() == null
                                                    ? selfUser.getEffectiveAvatarUrl()
                                                    : preset.getCharacterIconUrl(),
                                                preset.getCharacterName() == null
                                                    ? selfUser.getName()
                                                    : preset.getCharacterName()
                                            )
                                        );
                                    }
                                }
                            );
                    },
                    error -> {
                        channel.sendMessage(ChatBox.getIgnoreAnnotatedMessage(error.getMessage()))
                            .queue();
                    },
                    () -> {
                        // Apply the latest changes before exiting
                        if (message.get() != null) {
                            editWebhookMessage(
                                webhook,
                                message.get().getId(),
                                embed.build()
                            );

                        } else {
                            pageCounter.set(pageCounter.get() + 1);
                            embed.setFooter("Page %d".formatted(pageCounter.get()));
                            sendWebhookMessage(
                                webhook,
                                embed.build(),
                                preset.getCharacterIconUrl() == null
                                    ? selfUser.getEffectiveAvatarUrl()
                                    : preset.getCharacterIconUrl(),
                                preset.getCharacterName() == null
                                    ? selfUser.getName()
                                    : preset.getCharacterName()
                            );
                        }
                    }
                );
            }
        );
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("openai.chatbox.delete_channel")) {
            if (
                !event.getMessage()
                    .getMentions()
                    .getUsers()
                    .stream()
                    .anyMatch(user -> user.getIdLong() == event.getUser().getIdLong())
            ) {
                event.reply("Only the person who created this ChatBox can delete it.")
                    .setEphemeral(true)
                    .queue();

                return;
            }

            event.getChannel().delete()
                .queue();
        }
    }
}
