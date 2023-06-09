package io.github.lynbean.lynbot.cogs.google;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.customsearch.v1.CustomSearchAPI;
import com.google.api.services.customsearch.v1.model.Result;

import io.github.lynbean.lynbot.Bot;
import io.github.lynbean.lynbot.cogs.google.http.GoogleHttpRequestInitializer;

public class GoogleChat {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleChat.class);

    private static CustomSearchAPI API = new CustomSearchAPI.Builder(
        new NetHttpTransport(),
        new GsonFactory(),
        new GoogleHttpRequestInitializer()
    )
        .setApplicationName("LynBot")
        .build();

    private static com.google.api.services.customsearch.v1.CustomSearchAPI.Cse.List getCseList()
        throws java.io.IOException {

        String apiKey = Bot.getConfig().getString("google.key");
        String cx = Bot.getConfig().getString("google.cx");
        return API.cse().list()
            .setCx(cx)
            .setPrettyPrint(true)
            .setKey(apiKey);
    }

    @Nullable
    public static List<Result> search(String query) {
        try {
            return getCseList()
                .setQ(query)
                .setNum(10)
                .execute()
                .getItems();

        } catch (IOException e) {
            LOG.error("Error while searching for query: " + query, e);
            return null;
        }
    }
}
