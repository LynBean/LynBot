package io.github.lynbean.lynbot.cogs.epicgames.promotions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lynbean.lynbot.cogs.epicgames.promotions.common.Game;
import io.github.lynbean.lynbot.cogs.epicgames.promotions.common.GamePool;
import io.github.lynbean.lynbot.util.Util;
import okhttp3.Request;
import okhttp3.Response;

public class FreeGamesHandler {
    private static final String FREE_GAMES_PROMOTION_URL = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions";
    private static final GamePool gamePool = new GamePool();

    public static List<Game> getGamePool() throws IOException, ParseException {
        if (gamePool.lastUpdate == null)
            renewPool();
        else if (gamePool.lastUpdate.compareTo(LocalDateTime.now()) > 60)
            renewPool();

        return gamePool.getGames();
    }

    private static void renewPool() throws IOException, ParseException {
        Request request = new Request.Builder()
            .url(FREE_GAMES_PROMOTION_URL)
            .build();

        Response response = Util.getHttpClient()
            .newCall(request)
            .execute();

        if (!response.isSuccessful())
            return;

        gamePool.clear();
        gamePool.lastUpdate = LocalDateTime.now();

        Gson gson = Util.getGson();

        JsonObject jsonObject = gson.fromJson(
            response.body().string(), JsonObject.class
        );


        JsonArray jsonOfGames = jsonObject
            .getAsJsonObject("data")
            .getAsJsonObject("Catalog")
            .getAsJsonObject("searchStore")
            .getAsJsonArray("elements");

        for (JsonElement game : jsonOfGames) {
            JsonObject gameObject = game.getAsJsonObject();
            JsonElement gamePromotionDetailElement = gameObject.get("promotions");

            if (gamePromotionDetailElement.isJsonNull()) continue;

            JsonObject gamePromotionDetail = gamePromotionDetailElement
                .getAsJsonObject();

            Boolean onGoing = true;

            JsonArray gamePromo = gamePromotionDetail
                .getAsJsonArray("promotionalOffers");

            if (gamePromo.isEmpty()) {
                onGoing = false;
                gamePromo = gamePromotionDetail
                    .getAsJsonArray("upcomingPromotionalOffers");
            }

            String gameTitle = gameObject
                .getAsJsonPrimitive("title")
                .getAsString();

            String gameDescription = gameObject
                .getAsJsonPrimitive("description")
                .getAsString();

            String gamePrice = gameObject
                .getAsJsonObject("price")
                .getAsJsonObject("totalPrice")
                .getAsJsonObject("fmtPrice")
                .getAsJsonPrimitive("originalPrice")
                .getAsString();

            String gameImageUrl = gameObject
                .getAsJsonArray("keyImages")
                .get(0)
                .getAsJsonObject()
                .getAsJsonPrimitive("url")
                .getAsString();

            String gameSeller = gameObject
                .getAsJsonObject("seller")
                .getAsJsonPrimitive("name")
                .getAsString();

            JsonObject gamePromoDateObject = gamePromo
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("promotionalOffers")
                .get(0)
                .getAsJsonObject();

            String gamePromoDate;

            if (onGoing) {
                String promoEndDate = gamePromoDateObject
                    .getAsJsonPrimitive("endDate")
                    .getAsString();

                DateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                Date oldDate = oldDateFormat.parse(promoEndDate.replace("Z", "-0000"));
                DateFormat newDateFormat = new SimpleDateFormat("MMM dd");
                String newDate = newDateFormat.format(oldDate);

                gamePromoDate = String.format(
                    "Free Now - %s", newDate
                );
            } else {
                String promoStartDate = gamePromoDateObject
                    .getAsJsonPrimitive("startDate")
                    .getAsString();

                String promoEndDate = gamePromoDateObject
                    .getAsJsonPrimitive("endDate")
                    .getAsString();

                DateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                Date oldStartDate = oldDateFormat.parse(promoStartDate.replace("Z", "-0000"));
                Date oldEndDate = oldDateFormat.parse(promoEndDate.replace("Z", "-0000"));
                DateFormat newDateFormat = new SimpleDateFormat("MMM dd");
                String newStartDate = newDateFormat.format(oldStartDate);
                String newEndDate = newDateFormat.format(oldEndDate);

                gamePromoDate = String.format(
                    "Free %s - %s", newStartDate, newEndDate
                );
            }

            gamePool.add(
                Game.builder()
                    .description(gameDescription)
                    .imageUrl(gameImageUrl)
                    .onGoing(onGoing)
                    .pricing(gamePrice)
                    .promoDate(gamePromoDate)
                    .seller(gameSeller)
                    .title(gameTitle)
                    .build()
            );
        }
    }
}
