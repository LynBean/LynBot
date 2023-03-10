package com.kim.discordbot.core.commands;

import com.kim.discordbot.core.commands.meta.SlashCommandMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public abstract class SlashCommand {
    private final @Nonnull String name;
    private final @Nonnull String description;
    private final @Nonnull List<OptionData> options = new ArrayList<>();
    private final Map<String, SlashCommand> subCommands = new HashMap<>();

    public SlashCommand() {
        var clazz = getClass();

        // SlashCommand MetaData must be present
        if (!clazz.isAnnotationPresent(SlashCommandMeta.class))
            throw new IllegalArgumentException("Command annotation is missing");

        SlashCommandMeta meta = clazz.getAnnotation(SlashCommandMeta.class);
        this.name = meta.name();
        this.description = meta.description();

        if (meta.options().length > 0) {
            for (var option : meta.options()) {
                OptionData data = new OptionData(
                    option.type(), option.name(), option.description(), option.required()
                );

                if (data.getType() == OptionType.INTEGER || data.getType() == OptionType.NUMBER)
                    data.setMinValue(option.minValues())
                        .setMaxValue(option.maxValues());

                if (option.choices().length > 0 && data.getType().canSupportChoices()) {
                    for (var choice : option.choices()) {
                        if (data.getType() == OptionType.INTEGER)
                            data.addChoice(choice.description(), Integer.parseInt(choice.value()));
                        else if (data.getType() == OptionType.NUMBER)
                            data.addChoice(choice.description(), Double.parseDouble(choice.value()));
                        else
                            data.addChoice(choice.description(), choice.value());
                    }
                }

                options.add(data);
            }
        }
    }

    protected abstract void process(SlashCommandContext context);

    public void execute(SlashCommandContext context) {
        SlashCommand subCommand = getSubCommands().get(context.getSubcommandName());

        if (subCommand != null) {
            subCommand.process(context);
            return;
        }

        process(context);
    }

    public void addSubCommand(SlashCommand command) {
        subCommands.put(command.getName(), command);
    }

    public void addSubCommands(SlashCommand... commands) {
        Arrays.stream(commands).forEach(this::addSubCommand);
    }

    public @Nonnull String getName() {
        return name;
    }

    public @Nonnull String getDescription() {
        return description;
    }

    public Map<String, SlashCommand> getSubCommands() {
        return subCommands;
    }

    public @Nonnull List<SubcommandData> getSubCommandsData() {
        List<SubcommandData> subCommandsData = new ArrayList<>();

        for (var subCommand : subCommands.values()) {
            SubcommandData data = new SubcommandData(subCommand.getName(), subCommand.getDescription());

            if (!subCommand.getOptions().isEmpty())
                data.addOptions(subCommand.getOptions());

            subCommandsData.add(data);
        }

        return subCommandsData;
    }

    public @Nonnull List<OptionData> getOptions() {
        return options;
    }
}
