package io.github.lynbean.lynbot.cogs.epicgames.promotions.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GamePool {
    private List<Game> gamePool = new ArrayList<>();
    public LocalDateTime lastUpdate;

    public List<Game> getGames() {
        return gamePool;
    }

    public void add(Game game) {
        gamePool.add(game);
    }

    public int size() {
        return gamePool.size();
    }

    public void clear() {
        gamePool.clear();
    }
}
