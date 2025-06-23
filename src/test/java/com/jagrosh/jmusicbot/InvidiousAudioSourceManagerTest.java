package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.audio.InvidiousAudioSourceManager;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class InvidiousAudioSourceManagerTest {
    @Test
    public void convertYoutubeLink() {
        String result = InvidiousAudioSourceManager.convertToInstance(
                "https://www.youtube.com/watch?v=abc", "yewtu.be");
        assertEquals("https://yewtu.be/watch?v=abc", result);
    }

    @Test
    public void rotateInstances() {
        InvidiousAudioSourceManager mgr = new InvidiousAudioSourceManager(
                Arrays.asList("a", "b"));
        assertEquals("a", mgr.getCurrentInstance());
        mgr.rotateInstance();
        assertEquals("b", mgr.getCurrentInstance());
        mgr.rotateInstance();
        assertEquals("a", mgr.getCurrentInstance());
    }
}
