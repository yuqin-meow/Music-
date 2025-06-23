package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Audio source manager that loads YouTube links through an Invidious instance.
 */
public class InvidiousAudioSourceManager extends YoutubeAudioSourceManager {
    private final List<String> instances;
    private int index = 0;
    private static final Pattern YT_DOMAIN =
            Pattern.compile("https?://(?:www\\.)?(?:youtube\\.com|youtu\\.be)(/.*)");

    public InvidiousAudioSourceManager(List<String> instances) {
        super(true);
        this.instances = instances;
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager apm, AudioReference ar) {
        if (ar.identifier == null)
            return null;
        for (int i = 0; i < instances.size(); i++) {
            String url = convertToInstance(ar.identifier, getCurrentInstance());
            try {
                AudioItem item = super.loadItem(apm, new AudioReference(url, ar.title));
                if (item != null)
                    return item;
            } catch (Exception ex) {
                rotateInstance();
            }
        }
        return null;
    }

    static String convertToInstance(String url, String instance) {
        Matcher m = YT_DOMAIN.matcher(url);
        String domain = instance.endsWith("/") ? instance.substring(0, instance.length()-1) : instance;
        if (m.find())
            return "https://" + domain + m.group(1);
        return url.replaceFirst("https?://[^/]+", "https://" + domain);
    }

    synchronized String getCurrentInstance() {
        return instances.get(index);
    }

    synchronized void rotateInstance() {
        index = (index + 1) % instances.size();
    }
}
