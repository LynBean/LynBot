package io.github.lynbean.lynbot;

import io.github.lynbean.lynbot.core.BotCore;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;

public class Bot {
    private static Bot botInstance;
    private final BotCore botCore;

    public Bot() {
        botInstance = this;
        this.botCore = new BotCore();
    }

    public static void main(String[] args) throws Exception {
        try {
            new Bot();
            Bot.botInstance.botCore.start();
        } catch (InvalidTokenException e) {
            System.out.println(e.getMessage());
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bot getInstance() {
        return Bot.botInstance;
    }

    public BotCore getBotCore() {
        return botCore;
    }
}
