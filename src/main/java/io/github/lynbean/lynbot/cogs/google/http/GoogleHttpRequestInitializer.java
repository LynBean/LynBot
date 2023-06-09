package io.github.lynbean.lynbot.cogs.google.http;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

public class GoogleHttpRequestInitializer implements HttpRequestInitializer {
    @Override
    public void initialize(HttpRequest request) {
        request.setConnectTimeout(3 * 60000);
        request.setReadTimeout(3 * 60000);
    }
}
