package com.kim.discordbot.core.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandProcessor {
    public static final CommandRegistry REGISTRY = new CommandRegistry();

    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        REGISTRY.execute(event);
    }

    public void commandAutoComplete(CommandAutoCompleteInteractionEvent event) {
        REGISTRY.autoComplete(event);
    }
}
