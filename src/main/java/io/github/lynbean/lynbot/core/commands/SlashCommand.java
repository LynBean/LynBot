package io.github.lynbean.lynbot.core.commands;

import io.github.lynbean.lynbot.core.commands.meta.SlashCommandMeta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public abstract class SlashCommand {
    private Boolean deferReply = true;
    private final @Nonnull String name;
    private final @Nonnull String description;
    private final @Nonnull List<OptionData> options = new ArrayList<>();
    private final Map<String, SlashCommand> subCommands = new HashMap<>();
    private final List<SubcommandData> subCommandDatas = new ArrayList<>();

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

                if (data.getType() == OptionType.INTEGER)
                    data.setMinValue(option.minIntValues())
                        .setMaxValue(option.maxIntValues());

                if (data.getType() == OptionType.NUMBER)
                    data.setMinValue(option.minDoubleValues())
                        .setMaxValue(option.maxDoubleValues());

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

                data = optionDataEditor(data);
                options.add(data);
            }
        }
    }

    /**
     * Handles the slash command event.
     * @param event
     */
    protected abstract void process(SlashCommandInteractionEvent event);

    /**
     * Override this method to edit the option data before it is added to the command.
     * This method is created for convenience and is not required.
     * At most cases, you can use the {@link SlashCommandMeta} annotation to add options.
     * @param data The option data to edit.
     * @return The edited option data.
     */
    protected OptionData optionDataEditor(OptionData data) {
        return data;
    };

    /**
     * Handles the auto complete event.
     * @param event
     */
    protected void autoComplete(CommandAutoCompleteInteractionEvent event) {};

    /**
     * Whether the command should be deferred before processing.
     */
    public void setDeferReply(Boolean deferReply) {
        this.deferReply = deferReply;
    }

    /**
     * Executes the command.
     * @param event
     */
    public void execute(SlashCommandInteractionEvent event) {
        if (deferReply)
            event.deferReply().queue();

        SlashCommand subCommand = subCommands.get(event.getSubcommandName());

        if (subCommand != null) {
            subCommand.process(event);
            return;
        }

        process(event);
    }

    /**
     * Registers a sub command.
     */
    public void addSubCommand(SlashCommand command) {
        SubcommandData data = new SubcommandData(command.getName(), command.getDescription());

        if (!command.getOptions().isEmpty())
            data.addOptions(command.getOptions());

        subCommands.put(command.getName(), command);
        subCommandDatas.add(data);
    }

    /**
     * Returns an unmodifiable map of all registered sub commands.
     */
    public Map<String, SlashCommand> getSubCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    /**
     * Returns an unmodifiable list of all registered sub commands.
     */
    public List<SubcommandData> getSubCommandDatas() {
        return Collections.unmodifiableList(subCommandDatas);
    }

    public @Nonnull String getName() {
        return name;
    }

    public @Nonnull String getDescription() {
        return description;
    }

    public @Nonnull List<OptionData> getOptions() {
        return options;
    }
}
