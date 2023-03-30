package io.github.lynbean.lynbot.cogs.gpt.image;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import io.github.lynbean.lynbot.cogs.gpt.common.BotCompletionBuilder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class BotImageCompletion extends BotCompletionBuilder {
    private String prompt;
    private String userId;
    private int n;
    private String size;

    private List<String> imageUrls = new ArrayList<>();

    public BotImageCompletion(OpenAiService service, ExecutorService executor, String prompt, String userId) {
        super(service, executor);
        this.prompt = prompt;
        this.userId = userId;
    }

    @Override
    protected void process() {
        CreateImageRequest request = CreateImageRequest.builder()
            .prompt(prompt)
            .n(n)
            .size(size)
            .user(userId)
            .build();

        service.createImage(request)
            .getData()
            .stream()
            .forEach(image -> imageUrls.add(image.getUrl()));

        completionLatch.countDown();
    }
}
