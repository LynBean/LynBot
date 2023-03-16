package com.kim.discordbot.cogs;

import com.google.common.eventbus.Subscribe;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.CommandRegistry;
import com.kim.discordbot.core.commands.meta.SlashCommandMeta;
import com.kim.discordbot.core.commands.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.JDA;

@Cog
public class BotCogs {

    /**
     * This method is called when the bot is ready to register commands.
     */
    @Subscribe
    public void register(CommandRegistry registry) {
        registry.registerSlashCommand(Bot.class);
    }

    /**
     * This is the parent command for all commands within this class.
     */
    @SlashCommandMeta(
        name = "bot",
        description = "Hi there!"
    )
    public static class Bot extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        /**
         * This is a child command of the parent command "bot".
         * The name of the command is "ping".
         * The full command name is "bot ping".
         */
        @SlashCommandMeta(
            name = "ping",
            description = "Pong!"
        )
        public static class Ping extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                JDA jda = event.getJDA();
                event.getHook()
                    .editOriginal("Pong! " + jda.getGatewayPing() + "ms")
                    .queue();
            }
        }

        @SlashCommandMeta(
            name = "source",
            description = "I'm open source!"
        )
        public static class Source extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(event.getUser().getName())
                    .setDescription(
                        "I'm open source!"
                    )
                    .setFooter("Made with ❤ by LynBean")
                    .setThumbnail(event.getUser().getEffectiveAvatarUrl());

                event.getHook()
                    .editOriginalEmbeds(embed.build())
                    .setActionRow(
                        Button.link("https://github.com/LynBean/Yor", "GitHub.com")
                    )
                    .queue();
            }
        }
    }
}
