package io.github.lynbean.lynbot.cogs.developer;

import java.util.List;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Cog
public class BotCogs {

    /**
     * This method is called when the bot is ready to register commands.
     */
    @Subscribe
    public void register(CommandRegistry registry) {
        registry.registerGlobalSlashCommand(Bot.class);
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
                event.deferReply().queue();
                String USER_NAME = "LynBean";
                String PROJECT_NAME = "LynBot";
                String DISCORD_ID = "466890099737755658";

                User lynbean = event.getJDA().retrieveUserById(DISCORD_ID).complete();

                MessageEmbed embed = new EmbedBuilder()
                    .setAuthor("Made by %s".formatted(lynbean.getAsTag()), null, lynbean.getEffectiveAvatarUrl())
                    .setTitle("⭐ I'm open source! ⭐")
                    .setDescription("If you are interested in my project, please give me a ⭐ on GitHub!")
                    .setThumbnail("https://avatars.githubusercontent.com/u/57824016?v=4")
                    .setFooter("Made with ❤ by %s".formatted(USER_NAME))
                    .setImage("https://opengraph.githubassets.com/%s/%s/%s".formatted(event.getId(), USER_NAME, PROJECT_NAME))
                    .build();

                List<ItemComponent> components = List.of(
                    Button.link("https://github.com/%s/%s".formatted(USER_NAME, PROJECT_NAME), "Take a look!")
                );

                event.getHook()
                    .sendMessageEmbeds(embed)
                    .addActionRow(components)
                    .queue();
            }
        }
    }
}
