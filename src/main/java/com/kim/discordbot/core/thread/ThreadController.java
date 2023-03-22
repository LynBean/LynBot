package com.kim.discordbot.core.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ThreadController {
    public static final ExecutorService commandExecutor = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder()
            .setNameFormat("Bot CommandHandler-Worker %d")
            .build()
    );

    public static final ExecutorService eventExecutor = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder()
            .setNameFormat("Bot Event-Worker %d")
            .build()
    );
}
