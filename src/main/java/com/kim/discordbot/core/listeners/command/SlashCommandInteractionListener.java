package com.kim.discordbot.core.listeners.command;

import com.kim.discordbot.core.commands.processor.CommandProcessor;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandInteractionListener extends ListenerAdapter {
    private final CommandProcessor commandProcessor;
    private final ExecutorService executorService;

    public SlashCommandInteractionListener(CommandProcessor commandProcessor, ExecutorService executorService) {
        this.commandProcessor = commandProcessor;
        this.executorService = executorService;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        executorService.execute(
            () -> commandProcessor.execute(event)
        );
    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        executorService.execute(
            () -> commandProcessor.commandAutoComplete(event)
        );
    }
}
