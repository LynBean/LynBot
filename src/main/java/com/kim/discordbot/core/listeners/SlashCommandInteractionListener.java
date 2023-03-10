package com.kim.discordbot.core.listeners;

import com.kim.discordbot.core.commands.CommandProcessor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class SlashCommandInteractionListener implements EventListener {
    private final CommandProcessor processor;

    public SlashCommandInteractionListener(CommandProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof SlashCommandInteractionEvent)
            executeSlashCommand((SlashCommandInteractionEvent) event);
    }

    private void executeSlashCommand(SlashCommandInteractionEvent event) {
        processor.processSlashCommand(event);
    }
}
