package com.kim.discordbot.core.commands;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.JDA;

public class SlashCommandContext {
    private final SlashCommandInteractionEvent event;

    public SlashCommandContext(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public String getName() {
        return event.getName();
    }

    public String getFullCommandName() {
        return event.getFullCommandName();
    }

    public String getSubcommandName() {
        return event.getSubcommandName();
    }

    public String getSubcommandGroup() {
        return event.getSubcommandGroup();
    }

    public OptionMapping getOption(@Nonnull String name) {
        return event.getOption(name);
    }

    public User getUser() {
        return event.getUser();
    }

    public Member getMember() {
        return event.getMember();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public User getSelfUser() {
        return event.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        if (event.isFromGuild())
            return getGuild().getSelfMember();

        return null;
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public EmbedBuilder getBasicEmbedTemplate() {
        return new EmbedBuilder()
            .setAuthor(getUser().getName(), null, getUser().getAvatarUrl());
    }

    public void deferReply() {
        event.deferReply().queue();
    }

    public void reply(@Nonnull String message) {
        event.reply(message).queue();
    }

    public void reply(EmbedBuilder embed) {
        event.replyEmbeds(embed.build()).queue();
    }

    public void reply(@Nonnull String message, EmbedBuilder embed) {
        event.reply(message).setEmbeds(embed.build()).queue();
    }

    public void reply(@Nonnull String message, boolean ephemeral) {
        event.reply(message).setEphemeral(ephemeral).queue();
    }

    public void reply(EmbedBuilder embed, boolean ephemeral) {
        event.replyEmbeds(embed.build()).setEphemeral(ephemeral).queue();
    }

    public void reply(@Nonnull String message, EmbedBuilder embed, boolean ephemeral) {
        event.reply(message).setEmbeds(embed.build()).setEphemeral(ephemeral).queue();
    }

    public void send(@Nonnull String message) {
        event.getChannel().sendMessage(message).queue();
    }

    public void send(EmbedBuilder embed) {
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    public void send(@Nonnull String message, EmbedBuilder embed) {
        event.getChannel().sendMessage(message).setEmbeds(embed.build()).queue();
    }

    public void edit(@Nonnull String message) {
        event.getHook().editOriginal(message).queue();
    }

    public void edit(EmbedBuilder embed) {
        event.getHook().editOriginalEmbeds(embed.build()).queue();
    }

    public void edit(@Nonnull String message, EmbedBuilder embed) {
        event.getHook().editOriginal(message).setEmbeds(embed.build()).queue();
    }
}
