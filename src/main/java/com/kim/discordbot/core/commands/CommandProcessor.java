package com.kim.discordbot.core.commands;

import com.kim.discordbot.core.CommandRegistry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandProcessor {
    public static final CommandRegistry REGISTRY = new CommandRegistry();

    public void processSlashCommand(SlashCommandInteractionEvent event) {
        REGISTRY.process(event);
    }
}
