package com.kim.discordbot.core;

import com.kim.discordbot.core.commands.SlashCommand;
import com.kim.discordbot.core.commands.SlashCommandContext;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandRegistry {
    private final CommandManager manager = new CommandManager();

    public void process(SlashCommandInteractionEvent event) {
        SlashCommand command = manager.getSlashCommands().get(event.getName());

        if (command != null)
            command.execute(new SlashCommandContext(event));
    }

    public CommandManager getCommandManager() {
        return manager;
    }

    public void registerSlashCommand(@Nonnull Class<? extends SlashCommand> clazz) {
        manager.registerSlashCommand(clazz);
    }
}
