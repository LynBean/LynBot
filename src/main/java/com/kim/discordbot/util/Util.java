package com.kim.discordbot.util;

import com.google.common.io.CharStreams;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class Util {
    private static final Logger log = BotLogger.getLogger(Util.class);
    public static final Path APPDATA = Path.of("data");

    public static File validFile(String filename) {
        return APPDATA.toAbsolutePath().resolve(filename).toFile();
    }

    public static File validFile(String filename, Path inFolderPath) {
        return APPDATA.toAbsolutePath().resolve(inFolderPath).toFile();
    }

    public static InputStream loadResources(@NotNull Object clazz, String filename) {
        InputStream input = clazz.getClass().getResourceAsStream(filename);
        if (input == null) {
            return null;
        }
        return input;
    }

    public static String loadResourcesAsString(@NotNull Object clazz, String filename) {
        InputStream input = loadResources(clazz, filename);
        if (input == null) {
            return null;
        }
        return toString(input);
    }

    public static InputStream loadFile(@NotNull String filePathString) {
        File file = Path.of(filePathString).toAbsolutePath().toFile();

        if (!file.exists()) {
            return null;
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            return new ByteArrayInputStream(CharStreams.toString(reader)
                .getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String loadFileAsString(@NotNull String filename) {
        InputStream input = loadFile(filename);
        if (input == null) {
            return null;
        }
        return toString(input);
    }

    private static String toString(InputStream in) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        ) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(
                line -> sb.append(line).append(System.lineSeparator())
            );
            return sb.toString().trim();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
