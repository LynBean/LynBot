package com.kim.discordbot.core.commands;

import com.kim.discordbot.core.database.ConfigManager;
import java.util.Properties;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandRegistry {
    public final CommandManager MANAGER = new CommandManager();

    public void execute(SlashCommandInteractionEvent event) {
        SlashCommand command = MANAGER.getSlashCommands().get(event.getName());

        if (command != null)
            command.execute(event);
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
     * Registers the properties to the config manager.
     * At most cases, this should be called before {@link #registerSlashCommand(Class)}
     * to ensure that the properties are loaded before the command is registered.
     * @param properties
     */
    public void registerConfigProperties(Properties properties) {
        ConfigManager.registerProperties(properties);
    }
}
