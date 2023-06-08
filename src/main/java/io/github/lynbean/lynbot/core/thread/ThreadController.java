package io.github.lynbean.lynbot.core.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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

    public static void executeCommand(Runnable runnable) {
        commandExecutor.execute(() -> runnable.run());
    }
}
