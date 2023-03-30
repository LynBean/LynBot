package io.github.lynbean.lynbot.cogs.gpt.common;

import com.theokanning.openai.service.OpenAiService;
import io.github.lynbean.lynbot.util.BotLogger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import lombok.Data;
import org.slf4j.Logger;

@Data
public abstract class BotCompletionBuilder {
    protected Logger log = BotLogger.getLogger(BotCompletionBuilder.class);
    protected CountDownLatch completionLatch;
    protected ExecutorService executor;
    protected OpenAiService service;
    protected Thread thread;
    protected Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public BotCompletionBuilder(OpenAiService service, ExecutorService executor) {
        this.service = service;
        this.executor = executor;
    }

    protected abstract void process();

    public void run() {
        completionLatch = new CountDownLatch(1);
        executor.execute(
            () -> {
                thread = Thread.currentThread();
                thread.setUncaughtExceptionHandler(
                    (t, e) -> {
                        uncaughtExceptionHandler.uncaughtException(t, e);
                        completionLatch.countDown();
                    }
                );

                process();
                completionLatch.countDown();
            }
        );
    }

    public Boolean isRunning() {
        return completionLatch.getCount() > 0 ? true : false;
    }

    public void stop() {
        thread.interrupt();
    }

    public void await() {
        if (completionLatch == null)
            return;

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
