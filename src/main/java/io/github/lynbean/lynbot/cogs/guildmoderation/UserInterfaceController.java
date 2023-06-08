package io.github.lynbean.lynbot.cogs.guildmoderation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.codecs.pojo.annotations.BsonProperty;

import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildApplicationCommandPermissionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildChannelEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEmojiEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildForumTagEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildGenericEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInteractionEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildInviteEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMemberEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildMessageEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildPermissionOverrideEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildRoleEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildScheduledEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStageInstanceEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildStickerEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildThreadEvent;
import io.github.lynbean.lynbot.cogs.guildmoderation.pojo.events.GuildVoiceEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class UserInterfaceController {
    public static final String BUTTON_SET_LOG_CHANNEL = "guild-configs-set-log-channel";
    public static final String BUTTON_TOGGLE_ALL_EVENTS = "guild-configs-toggle-all-events";
    public static final String STRING_SELECT_MENU_ID = "guild-configs-select-menu";

    private GuildDatabaseManager guildDatabaseManager;
    private Guild guild;
    private User user;
    private String guildEventName;

    private boolean booleanSwitcher = true;

    public UserInterfaceController(Guild guild, User user, String guildEventName) {
        this.guildDatabaseManager = new GuildDatabaseManager(guild);
        this.guild = guild;
        this.user = user;
        this.guildEventName = guildEventName;
    }

    public static final List<Class<? extends GuildGenericEvent>> GUILD_EVENT_CLAZZ = List.of(
        GuildApplicationCommandPermissionEvent.class,
        GuildChannelEvent.class,
        GuildEmojiEvent.class,
        GuildEvent.class,
        GuildForumTagEvent.class,
        GuildInteractionEvent.class,
        GuildInviteEvent.class,
        GuildMemberEvent.class,
        GuildMessageEvent.class,
        GuildPermissionOverrideEvent.class,
        GuildRoleEvent.class,
        GuildScheduledEvent.class,
        GuildStageInstanceEvent.class,
        GuildStickerEvent.class,
        GuildThreadEvent.class,
        GuildVoiceEvent.class
    );

    public static final List<String> EXCLUDED_FIELDS = List.of(
        "logChannelId"
    );

    /**
     * Map of all the fields from all classes in {@link GUILD_EVENT_CLAZZ}
     * with their respective class name as the key.
     * Filtered by the {@link BsonProperty} annotation.
     */
    public static Map<String, List<String>> getFields() {
        return getRawFields()
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue()
                        .stream()
                        .filter(fieldName -> !EXCLUDED_FIELDS.contains(fieldName))
                        .collect(Collectors.toList())
                )
            );
    }

    public static Map<String, List<String>> getRawFields() {
        return GUILD_EVENT_CLAZZ.stream()
            .collect(
                Collectors.toMap(
                    Class::getSimpleName,
                    clazz -> Stream.concat(
                        Arrays.stream(clazz.getDeclaredFields()),
                        Arrays.stream(clazz.getSuperclass().getDeclaredFields())
                    )
                        .collect(Collectors.toList())
                        .stream()
                        .filter(field -> field.isAnnotationPresent(BsonProperty.class))
                        .map(Field::getName)
                        .collect(Collectors.toList())
                )
            );
    }

    public static Map<String, List<SelectOption>> getSelectOptions() {
        return getFields().entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    entry -> Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1),
                    entry -> entry.getValue()
                        .stream()
                        .map(fieldName -> SelectOption.of(fieldName, fieldName))
                        .collect(Collectors.toList())
                )
            );
    }

    public static List<Choice> getChoices() {
        return GUILD_EVENT_CLAZZ.stream()
            .map(Class::getSimpleName)
            .map(name -> new Choice(name, name))
            .collect(Collectors.toList());
    }

    public MessageCreateData createMenuMessage() {
        MessageEmbed embed = new EmbedBuilder()
            .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTitle(guildEventName)
            .setDescription(
                getRawFields().get(guildEventName)
                    .stream()
                    .map(
                        fieldName -> "%s: *%s*\n".formatted(
                            Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1),
                            String.valueOf(guildDatabaseManager.retrieveField(guildEventName, fieldName))
                        )
                    )
                    .collect(Collectors.joining("\n"))
            )
            .build();

        StringSelectMenu menu = StringSelectMenu.create(STRING_SELECT_MENU_ID)
            .addOptions(getSelectOptions().get(guildEventName))
            .setMaxValues(25)
            .setMinValues(1)
            .setPlaceholder("Select field(s) to toggle")
            .build();

        List<Button> buttons = List.of(
            Button.secondary(BUTTON_SET_LOG_CHANNEL, "Set log channel"),
            Button.secondary(BUTTON_TOGGLE_ALL_EVENTS, "Toggle all events to True/False")
        );

        return new MessageCreateBuilder()
            .addActionRow(buttons)
            .addActionRow(menu)
            .addEmbeds(embed)
            .build();
    }

    public <T extends GenericComponentInteractionCreateEvent> void handleMenuInteraction(T t) {
        t.deferEdit().queue();

        try {
            if (t.getComponentId().equals(BUTTON_SET_LOG_CHANNEL)) {
                guildDatabaseManager.updateField(
                    guildEventName,
                    "logChannelId",
                    t.getChannel().getId()
                );
            }
            if (t.getComponentId().equals(BUTTON_TOGGLE_ALL_EVENTS)) {
                getFields().get(guildEventName)
                    .stream()
                    .forEach(fieldName -> guildDatabaseManager.updateField(guildEventName, fieldName, booleanSwitcher));
                booleanSwitcher = !booleanSwitcher;
            }
            if (t.getComponentId().equals(STRING_SELECT_MENU_ID)) {
                ((StringSelectInteractionEvent) t).getSelectedOptions()
                    .stream()
                    .forEach(fieldName -> guildDatabaseManager.updateField(guildEventName, fieldName.getValue()));
            }

        } catch (IllegalStateException e) {
            t.getHook()
                .editOriginal("> %s".formatted(e.getMessage()))
                .queue();

            getTimeoutHandler(t.getMessage()).run();
            return;
        }

        t.getHook()
            .editOriginal(MessageEditBuilder.fromCreateData(createMenuMessage()).build())
            .queue();
    }

    public Runnable getTimeoutHandler(Message message) {
        return () -> {
            message.editMessageComponents(
                message.getComponents()
                    .stream()
                    .map(LayoutComponent::asDisabled)
                    .collect(Collectors.toList())
            )
                .queue();
        };
    }
}
