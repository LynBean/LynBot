package io.github.lynbean.lynbot.core.listeners.command;

import io.github.lynbean.lynbot.core.commands.processor.CommandProcessor;
import io.github.lynbean.lynbot.util.Util;
import java.util.concurrent.ExecutorService;
import java.util.List;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ContextCommandListener extends ListenerAdapter {
    private final CommandProcessor commandProcessor;
    private final ExecutorService executorService;

    public ContextCommandListener(CommandProcessor commandProcessor, ExecutorService executorService) {
        this.commandProcessor = commandProcessor;
        this.executorService = executorService;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        List<String> prefixes = Util.getBotPrefixes();
        String message = event.getMessage().getContentRaw();
        String selfUserMention = event.getJDA().getSelfUser().getAsMention();

        for (int i = 0; i <= prefixes.size(); i++) {
            if (message.startsWith(selfUserMention)) {
                message = message.substring(selfUserMention.length())
                    .trim();
                break;
            }

            if (i == prefixes.size())
                return;

            if (!message.toLowerCase().startsWith(prefixes.get(i).toLowerCase()))
                continue;

            message = message.substring(prefixes.get(i).length())
                .trim();

            break;
        }

        final String messageWithoutPrefix = message;
        executorService.execute(
            () -> commandProcessor.execute(event, messageWithoutPrefix)
        );
    }
}
