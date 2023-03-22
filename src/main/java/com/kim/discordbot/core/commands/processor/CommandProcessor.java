package com.kim.discordbot.core.commands.processor;

import com.kim.discordbot.core.commands.CommandRegistry;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandProcessor {
    public static final CommandRegistry REGISTRY = new CommandRegistry();

    public void execute(MessageReceivedEvent event, String messageWithoutPrefix) {
        REGISTRY.execute(event, messageWithoutPrefix);
    }

    public void execute(SlashCommandInteractionEvent event) {
        REGISTRY.execute(event);
    }

    public void commandAutoComplete(CommandAutoCompleteInteractionEvent event) {
        REGISTRY.autoComplete(event);
    }
}
