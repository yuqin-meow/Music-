/*
 * Copyright 2018 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.playlist;

import com.jagrosh.jmusicbot.BotConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a Playlist object.
 * 
 * @author John
 */
public class Playlist {
    private final String name;
    private final List<String> items;
    private final boolean shuffle;
    private final BotConfig config;
    private final List<AudioTrack> tracks = new LinkedList<>();
    private final List<PlaylistLoadError> errors = new LinkedList<>();
    private boolean loaded = false;

    public Playlist(String name, List<String> items, boolean shuffle, BotConfig config) {
        this.name = name;
        this.items = items;
        this.shuffle = shuffle;
        this.config = config;
    }

    public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
        if (loaded) return;
        loaded = true;
        for (int i = 0; i < items.size(); i++) {
            boolean last = i + 1 == items.size();
            int index = i;
            manager.loadItemOrdered(name, items.get(i), new AudioLoadResultHandler() {
                private void done() {
                    if (last) {
                        if (shuffle) {
                            shuffleTracks();
                        }
                        if (callback != null) {
                            callback.run();
                        }
                    }
                }

                @Override
                public void trackLoaded(AudioTrack at) {
                    if (config.isTooLong(at)) {
                        errors.add(new PlaylistLoadError(index, items.get(index), "This track is longer than the allowed maximum"));
                    } else {
                        at.setUserData(0L);
                        tracks.add(at);
                        consumer.accept(at);
                    }
                    done();
                }

                @Override
                public void playlistLoaded(AudioPlaylist ap) {
                    if (ap.isSearchResult()) {
                        trackLoaded(ap.getTracks().get(0));
                    } else if (ap.getSelectedTrack() != null) {
                        trackLoaded(ap.getSelectedTrack());
                    } else {
                        List<AudioTrack> loaded = new ArrayList<>(ap.getTracks());
                        if (shuffle) {
                            for (int first = 0; first < loaded.size(); first++) {
                                int second = (int) (Math.random() * loaded.size());
                                AudioTrack tmp = loaded.get(first);
                                loaded.set(first, loaded.get(second));
                                loaded.set(second, tmp);
                            }
                        }
                        loaded.removeIf(track -> config.isTooLong(track));
                        loaded.forEach(at -> at.setUserData(0L));
                        tracks.addAll(loaded);
                        loaded.forEach(at -> consumer.accept(at));
                    }
                    done();
                }

                @Override
                public void noMatches() {
                    errors.add(new PlaylistLoadError(index, items.get(index), "No matches found."));
                    done();
                }

                @Override
                public void loadFailed(FriendlyException fe) {
                    errors.add(new PlaylistLoadError(index, items.get(index), "Failed to load track: " + fe.getLocalizedMessage()));
                    done();
                }
            });
        }
    }

    public void shuffleTracks() {
        PlaylistLoader.shuffle(tracks);
    }

    public String getName() {
        return name;
    }

    public List<String> getItems() {
        return items;
    }

    public List<AudioTrack> getTracks() {
        return tracks;
    }

    public List<PlaylistLoadError> getErrors() {
        return errors;
    }

    public static class PlaylistLoadError {
        private final int number;
        private final String item;
        private final String reason;

        public PlaylistLoadError(int number, String item, String reason) {
            this.number = number;
            this.item = item;
            this.reason = reason;
        }

        public int getIndex() {
            return number;
        }

        public String getItem() {
            return item;
        }

        public String getReason() {
            return reason;
        }
    }
}

