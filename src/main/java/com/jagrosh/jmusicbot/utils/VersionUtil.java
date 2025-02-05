package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.entities.Prompt;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Reader;

public class VersionUtil {
    private static final String NEW_VERSION_AVAILABLE = "There is a new version of JMusicBot available!\n"
            + "Current version: %s\n"
            + "New Version: %s\n\n"
            + "Please visit https://github.com/jagrosh/MusicBot/releases/latest to get the latest release.";

    public static void checkJavaVersion(Prompt prompt) {
        if (!System.getProperty("java.vm.name").contains("64"))
            prompt.alert(Prompt.Level.WARNING, "Java Version",
                    "It appears that you may not be using a supported Java version. Please use 64-bit java.");
    }

    public static void checkVersion(Prompt prompt) {
        String version = getCurrentVersion();
        String latestVersion = getLatestVersion();
        if (latestVersion != null && !latestVersion.equals(version)) {
            prompt.alert(Prompt.Level.WARNING, "JMusicBot Version", String.format(NEW_VERSION_AVAILABLE, version, latestVersion));
        }
    }

    public static String getCurrentVersion() {
        if (JMusicBot.class.getPackage() != null && JMusicBot.class.getPackage().getImplementationVersion() != null)
            return JMusicBot.class.getPackage().getImplementationVersion();
        else
            return "UNKNOWN";
    }

    public static String getLatestVersion() {
        try {
            Response response = new OkHttpClient.Builder().build()
                    .newCall(new Request.Builder().get().url("https://api.github.com/repos/jagrosh/MusicBot/releases/latest").build())
                    .execute();
            if (response.body() != null) {
                try (Reader reader = response.body().charStream()) {
                    JSONObject obj = new JSONObject(new JSONTokener(reader));
                    return obj.getString("tag_name");
                }
            }
        } catch (IOException ignored) {}
        return null;
    }
}
