package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.JMusicBot;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
    private static final String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";

    public static Path getPath(String path) {
        Path result = Paths.get(path);
        if (result.toAbsolutePath().toString().toLowerCase().startsWith(WINDOWS_INVALID_PATH)) {
            try {
                result = Paths.get(new File(JMusicBot.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getParentFile()
                        .getPath() + File.separator + path);
            } catch (URISyntaxException ignored) {}
        }
        return result;
    }
}
