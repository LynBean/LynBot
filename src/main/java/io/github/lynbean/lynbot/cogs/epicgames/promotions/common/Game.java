package io.github.lynbean.lynbot.cogs.epicgames.promotions.common;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Game {
    private @Builder.Default String url = "https://store.epicgames.com/en-US/free-games";
    private Boolean onGoing;
    private String description;
    private String imageUrl;
    private String pricing;
    private String promoDate;
    private String seller;
    private String title;
}
