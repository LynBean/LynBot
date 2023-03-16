package com.kim.discordbot.core.listeners;

import com.kim.discordbot.core.commands.CommandProcessor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandInteractionListener extends ListenerAdapter {
    private final CommandProcessor commandProcessor;

    public SlashCommandInteractionListener(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        commandProcessor.executeSlashCommand(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        commandProcessor.commandAutoComplete(event);
    }
}
