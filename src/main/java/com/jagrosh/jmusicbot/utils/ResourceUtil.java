package com.jagrosh.jmusicbot.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ResourceUtil {
    public static String loadResource(Object clazz, String name) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clazz.getClass().getResourceAsStream(name)))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append("\r\n").append(line));
            return sb.toString().trim();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static InputStream imageFromUrl(String url) {
        if (url == null) return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0");
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignored) {
            return null;
        }
    }
}
