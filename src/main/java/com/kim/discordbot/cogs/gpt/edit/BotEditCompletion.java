package com.kim.discordbot.cogs.gpt.edit;

import com.kim.discordbot.cogs.gpt.common.BotCompletionBuilder;
import com.kim.discordbot.core.database.ConfigManager;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditRequest.EditRequestBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper=true)
public class BotEditCompletion extends BotCompletionBuilder {
    private @Builder.Default String model = ConfigManager.get("edit-model");
    private @Builder.Default int n = Integer.parseInt(ConfigManager.get("edit-n"));
    private @Builder.Default Double temperature = Double.parseDouble(ConfigManager.get("edit-temperature"));
    private @Builder.Default Double topP = Double.parseDouble(ConfigManager.get("chat-top-p"));

    private String instruction;
    private @Builder.Default List<String> response = new ArrayList<>();

    @Override
    public Boolean isValidArgs() {
        return !(
            getService() == null ||
            getPrompt() == null ||
            this.instruction == null
        );
    }

    private EditRequestBuilder requestBuilder() {
        if (!isValidArgs())
            throw new IllegalArgumentException("Invalid arguments");

        return EditRequest.builder()
            .model(model)
            .input(this.getPrompt())
            .instruction(instruction)
            .n(n)
            .temperature(temperature)
            .topP(topP);
    }

    @Override
    public void buildRequest() {
        EditRequest request = requestBuilder()
            .build();

        this.setThread(new Thread(() -> {
            this.getService()
                .createEdit(request)
                .getChoices()
                .stream()
                .forEach(
                    (choice) -> this.response.add(
                        choice.getText()
                    )
                );

            this.getLatch().countDown();
        }));
    }

    @Override
    public void runRequest() {
        if (this.getThread() == null)
            buildRequest();

        this.getThread().start();
    }
}
