package com.kim.discordbot.core.commands;

import com.kim.discordbot.core.commands.manager.CommandManager;
import com.kim.discordbot.core.database.ConfigManager;
import com.kim.discordbot.util.BotLogger;
import java.util.Properties;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;

public class CommandRegistry {
    private static final Logger log = BotLogger.getLogger(CommandRegistry.class);
    public final CommandManager MANAGER = new CommandManager();

    public void execute(MessageReceivedEvent event, String contentWithoutPrefix) {
        String commandName = contentWithoutPrefix.split(" ")[0];
        ContextCommand command = MANAGER.getContextCommands().get(commandName);

        if (command != null) {
            log.info("<{} - {}> -> {}", event.getAuthor().getName(), event.getAuthor().getId(), commandName);
            command.execute(event, contentWithoutPrefix);
        }
    }

    public void execute(SlashCommandInteractionEvent event) {
        SlashCommand command = MANAGER.getSlashCommands().get(event.getName());

        if (command != null) {
            log.info("<{} - {}> -> {}", event.getUser().getName(), event.getUser().getId(), event.getCommandString());
            command.execute(event);
        }
    }

    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand command = MANAGER.getSlashCommands().get(event.getName());

        if (command != null)
            command.autoComplete(event);
    }

    /**
     * Registers the slash command to the command manager.
     * @param clazz
     */
    public void registerSlashCommand(@Nonnull Class<? extends SlashCommand> clazz) {
        MANAGER.registerSlashCommand(clazz);
    }
    /**
     * Registers the context command to the command manager.
     * @param clazz
     */
    public void registerContextCommand(@Nonnull Class<? extends ContextCommand> clazz) {
        MANAGER.registerContextCommand(clazz);
    }

    /**
     * Registers the properties to the config manager.
     * At most cases, this should be called before {@link #registerSlashCommand(Class)}
     * to ensure that the properties are loaded before the command is registered.
     * @param properties
     */
    public void registerConfigProperties(Properties properties) {
        ConfigManager.registerProperties(properties);
    }
}
