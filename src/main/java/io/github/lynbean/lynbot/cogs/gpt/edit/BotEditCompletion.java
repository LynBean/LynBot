package io.github.lynbean.lynbot.cogs.gpt.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.service.OpenAiService;

import io.github.lynbean.lynbot.cogs.gpt.common.BotCompletionBuilder;
import io.github.lynbean.lynbot.core.database.ConfigManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString
public class BotEditCompletion extends BotCompletionBuilder {
    private String input;
    private String model = ConfigManager.get("edit-model");
    private int n = Integer.parseInt(ConfigManager.get("edit-n"));
    private double temperature = Double.parseDouble(ConfigManager.get("edit-temperature"));
    private double topP = Double.parseDouble(ConfigManager.get("chat-top-p"));

    private String instruction;
    private List<String> response = new ArrayList<>();

    public BotEditCompletion(OpenAiService service, ExecutorService executor, String input, String instruction) {
        super(service, executor);
        this.input = input;
        this.instruction = instruction;
    }

    @Override
    protected void process() {
        EditRequest request = EditRequest.builder()
            .model(model)
            .input(input)
            .instruction(instruction)
            .n(n)
            .temperature(temperature)
            .topP(topP)
            .build();

        service.createEdit(request)
            .getChoices()
            .stream()
            .forEach(
                (choice) -> this.response.add(
                    choice.getText()
                )
            );

        completionLatch.countDown();
    }
}
