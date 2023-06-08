package io.github.lynbean.lynbot.cogs.epicgames.entities;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import com.mongodb.lang.Nullable;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Game {
    @Builder.Default
    private @Nonnull String url = "https://store.epicgames.com/en-US/free-games";

    private @Nonnull LocalDateTime endDate;
    private @Nullable LocalDateTime startDate;
    private @Nonnull String description;
    private @Nonnull String imageUrl;
    private @Nonnull String pricing;
    private @Nonnull String seller;
    private @Nonnull String title;
}
