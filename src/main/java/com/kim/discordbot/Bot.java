package com.kim.discordbot;

import com.kim.discordbot.core.BotCore;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;

public class Bot {
    public static void main(String[] args) {
        BotCore bot = new BotCore();
        try {
            bot.setCommandsPackage("com.kim.discordbot.cogs")
                .start();

        } catch (InvalidTokenException e) {
            System.out.println(e.getMessage());
        }
    }
}
