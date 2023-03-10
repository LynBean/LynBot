package com.kim.discordbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotLogger {
    private static Logger log;

    public static Logger getLogger(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
        return log;
    }
}
