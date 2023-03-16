package com.kim.discordbot.core.commands;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandManager {
    private final Map<String, SlashCommand> slashCommands = new HashMap<>();
    private final List<CommandData> slashCommandDatas = new ArrayList<>();

    /**
     * Returns an unmodifiable map of all registered slash commands.
     */
    public Map<String, SlashCommand> getSlashCommands() {
        return Collections.unmodifiableMap(slashCommands);
    }

    /**
     * Returns an unmodifiable list of all registered slash commands.
     */
    public List<CommandData> getSlashCommandDatas() {
        return Collections.unmodifiableList(slashCommandDatas);
    }

    /**
     * Instantiates a new instance of the given class.
     */
    private static <@Nonnull T> T instantiate(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a slash command.
     */
    public <T extends SlashCommand> T registerSlashCommand(@Nonnull Class<T> clazz) {
        return registerSlashCommand(instantiate(clazz));
    }

    private <T extends SlashCommand> T registerSlashCommand(@Nonnull T command) {
        if (slashCommands.putIfAbsent(command.getName(), command) != null) {
            throw new IllegalArgumentException("Command with name " + command.getName() + " already exists");
        }

        registerSubSlashCommands(command);
        CommandData commandData;

        if (command.getSubCommands().isEmpty())
            commandData = Commands.slash(command.getName(), command.getDescription())
                .addOptions(command.getOptions());

        else
            commandData = Commands.slash(command.getName(), command.getDescription())
                .addSubcommands(command.getSubCommandDatas());

        slashCommands.put(command.getName(), command);
        slashCommandDatas.add(commandData);
        return command;
    }

    private static void registerSubSlashCommands(SlashCommand command) {
        for (var inner : command.getClass().getDeclaredClasses()) {
            if (!Modifier.isStatic(inner.getModifiers())) continue;
            if (!SlashCommand.class.isAssignableFrom(inner)) continue;
            if (inner.isLocalClass() || inner.isAnonymousClass()) continue;
            if (Modifier.isAbstract(inner.getModifiers())) continue;

            var subCommand = (SlashCommand)instantiate(inner);
            command.addSubCommand(subCommand);
        }
    }
}
