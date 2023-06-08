package io.github.lynbean.lynbot.database;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class HoconManager {
    private final Config config;

    public HoconManager(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            config = ConfigFactory.parseReader(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
