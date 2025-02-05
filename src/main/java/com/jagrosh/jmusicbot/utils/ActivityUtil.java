package com.jagrosh.jmusicbot.utils;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class ActivityUtil {
    public static Activity parseGame(String game) {
        if (game == null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default"))
            return null;
        String lower = game.toLowerCase();
        if (lower.startsWith("playing"))
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
        if (lower.startsWith("listening to"))
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
        if (lower.startsWith("listening"))
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
        if (lower.startsWith("watching"))
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
        if (lower.startsWith("streaming")) {
            String[] parts = game.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
            }
        }
        return Activity.playing(game);
    }

    public static String makeNonEmpty(String str) {
        return str == null || str.isEmpty() ? "\u200B" : str;
    }

    public static OnlineStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty())
            return OnlineStatus.ONLINE;
        OnlineStatus st = OnlineStatus.fromKey(status);
        return st == null ? OnlineStatus.ONLINE : st;
    }
}
