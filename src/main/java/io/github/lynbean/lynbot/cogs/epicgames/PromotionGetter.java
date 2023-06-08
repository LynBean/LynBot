package io.github.lynbean.lynbot.cogs.epicgames;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lynbean.lynbot.cogs.epicgames.entities.Game;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PromotionGetter {
    private static final String FREE_GAMES_PROMOTION_URL = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions";
    private static final Gson GSON = new Gson();
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    private final @Getter List<Game> games;

    public PromotionGetter() {
        this.games = new ArrayList<>();
    }

    public void update() throws IOException {
        JsonElement data = retrieveData();
        List<Game> latest = parseData(data);

        games.addAll(latest);
    }

    private JsonElement retrieveData() throws IOException {
        Request request = new Request.Builder()
            .url(FREE_GAMES_PROMOTION_URL)
            .build();

        Response response = HTTP_CLIENT
            .newCall(request)
            .execute();

        return GSON.fromJson(response.body().string(), JsonElement.class);
    }

    private List<Game> parseData(JsonElement data) {
        JsonArray gameDatas = data
            .getAsJsonObject()
            .getAsJsonObject("data")
            .getAsJsonObject("Catalog")
            .getAsJsonObject("searchStore")
            .getAsJsonArray("elements");

        Map<JsonObject, JsonObject> games = gameDatas.asList()
            .stream()
            .filter(element -> !element.isJsonNull())
            .map(JsonElement::getAsJsonObject)
            .collect(
                Collectors.toMap(
                    element -> element,
                    element -> element.get("promotions")
                        .getAsJsonObject()
                )
            );

        return games.entrySet()
            .stream()
            .map(
                (entry) -> {
                    JsonObject game = entry.getKey();
                    JsonObject details = entry.getValue();

                    String title = game.getAsJsonPrimitive("title").getAsString();
                    String description = game.getAsJsonPrimitive("description").getAsString();
                    String price = game.getAsJsonObject("price")
                        .getAsJsonObject("totalPrice")
                        .getAsJsonObject("fmtPrice")
                        .getAsJsonPrimitive("originalPrice")
                        .getAsString();

                    String imageUrl = game.getAsJsonArray("keyImages")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonPrimitive("url")
                        .getAsString();

                    String seller = game.getAsJsonObject("seller")
                        .getAsJsonPrimitive("name")
                        .getAsString();

                    JsonArray promotions = details.getAsJsonArray("promotionalOffers")
                        .isEmpty()
                            ? details.getAsJsonArray("upcomingPromotionalOffers")
                            : details.getAsJsonArray("promotionalOffers");

                    JsonObject promotion = promotions
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("promotionalOffers")
                        .get(0)
                        .getAsJsonObject();

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                    LocalDateTime startDate = Optional.ofNullable(promotion.getAsJsonPrimitive("startDate"))
                        .map(JsonElement::getAsString)
                        .map(date -> LocalDateTime.parse(date, dateFormatter))
                        .orElse(null);

                    LocalDateTime endDate = Optional.ofNullable(promotion.getAsJsonPrimitive("endDate"))
                        .map(JsonElement::getAsString)
                        .map(date -> LocalDateTime.parse(date, dateFormatter))
                        .orElse(null);

                    return Game.builder()
                        .title(title)
                        .description(description)
                        .pricing(price)
                        .imageUrl(imageUrl)
                        .seller(seller)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build();
                }
            )
            .collect(Collectors.toList());
    }
}
