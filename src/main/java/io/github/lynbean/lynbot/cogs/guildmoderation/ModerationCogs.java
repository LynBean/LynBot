package io.github.lynbean.lynbot.cogs.guildmoderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.Subscribe;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.guildmoderation.listener.GuildEventListener;
import io.github.lynbean.lynbot.core.commands.Cog;
import io.github.lynbean.lynbot.core.commands.CommandRegistry;
import io.github.lynbean.lynbot.core.commands.SlashCommand;
import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

@Cog
public class ModerationCogs {
    @Subscribe
    public void register(CommandRegistry registry) {
        Bot.getMongoManager().addCodecPackages(
            "io.github.lynbean.lynbot.cogs.guildmoderation.pojo",
            "io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events"
        );
        registry.registerEventListeners(new GuildEventListener());
        registry.registerGlobalSlashCommand(Moderation.class);
    }

    @SlashCommandMeta(
        name = "mod",
        description = "Moderation commands",
        isAdminOnly = true,
        isGuildOnly = true
    )
    public static class Moderation extends SlashCommand {
        @Override
        protected void process(SlashCommandInteractionEvent event) {}

        @SlashCommandMeta(
            name = "event-logging-settings",
            description = "Server event logging settings",
            options = {
                @SlashCommandMeta.Option(
                    name = "event-type",
                    description = "The type of event to set the logging channel for",
                    type = OptionType.STRING,
                    required = true
                )
            }
        )
        public static class EventLoggingSettings extends SlashCommand {
            @Override
            protected void process(SlashCommandInteractionEvent event) {
                event.deferReply().queue();
                String eventType = event.getOption("event-type").getAsString();

                UserInterfaceController controller = new UserInterfaceController(event.getGuild(), event.getUser(), eventType);

                Message message = event.getHook()
                    .editOriginal(MessageEditData.fromCreateData(controller.createMenuMessage()))
                    .complete();

                this.getCore().getEventWaiter().waitForEvent(
                    GenericComponentInteractionCreateEvent.class,
                    e -> e.getMessage().getId().contentEquals(message.getId()),
                    e -> controller.handleMenuInteraction(e),
                    60,
                    TimeUnit.SECONDS,
                    controller.getTimeoutHandler(message),
                    true
                );
            }

            @Override
            protected List<OptionData> editOptions(List<OptionData> options) {
                List<Choice> choices = UserInterfaceController.getChoices();

                if (choices.size() > 0) {
                    options.stream()
                        .filter(option -> option.getName().equals("event-type"))
                        .findFirst()
                        .ifPresent(option -> option.addChoices(choices));

                } else {
                    options.removeIf(option -> option.getName().equals("event-type"));
                }

                return super.editOptions(options);
            }
        }
    }
}
