package com.kim.discordbot.cogs;

import com.google.common.eventbus.Subscribe;
import com.kim.discordbot.core.CommandRegistry;
import com.kim.discordbot.core.commands.Cog;
import com.kim.discordbot.core.commands.meta.SlashCommandMeta;
import com.kim.discordbot.core.commands.SlashCommand;
import com.kim.discordbot.core.commands.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
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
        protected void process(SlashCommandContext context) {}

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
            protected void process(SlashCommandContext context) {
                JDA jda = context.getJDA();
                context.reply("Pong! " + jda.getGatewayPing() + "ms");
            }
        }

        @SlashCommandMeta(
            name = "source",
            description = "I'm open source!"
        )
        public static class Source extends SlashCommand {
            @Override
            protected void process(SlashCommandContext context) {
                EmbedBuilder embed = context.getBasicEmbedTemplate()
                    .setDescription(
                        "I'm open source!"
                    )
                    .setFooter("Made with ❤ by LynBean")
                    .setThumbnail(context.getSelfUser().getEffectiveAvatarUrl());

                context.getEvent()
                    .replyEmbeds(embed.build())
                    .addActionRow(
                        Button.link("https://github.com/LynBean/Yor", "GitHub.com")
                    )
                    .queue();
            }
        }
    }
}
