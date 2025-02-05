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
import com.jagrosh.jmusicbot.utils.OtherUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PlaylistLoader class responsible for loading, creating, and managing playlists.
 * 
 * @author John Grosh
 */
public class PlaylistLoader {
    private final BotConfig config;

    public PlaylistLoader(BotConfig config) {
        this.config = config;
    }

    public List<String> getPlaylistNames() {
        if (folderExists()) {
            File folder = new File(OtherUtil.getPath(config.getPlaylistsFolder()).toString());
            return Arrays.asList(folder.listFiles((pathname) -> pathname.getName().endsWith(".txt")))
                    .stream().map(f -> f.getName().substring(0, f.getName().length() - 4)).collect(Collectors.toList());
        } else {
            createFolder();
            return Collections.emptyList();
        }
    }

    public void createFolder() {
        try {
            Files.createDirectory(OtherUtil.getPath(config.getPlaylistsFolder()));
        } catch (IOException ignore) {}
    }

    public boolean folderExists() {
        return Files.exists(OtherUtil.getPath(config.getPlaylistsFolder()));
    }

    public void createPlaylist(String name) throws IOException {
        Files.createFile(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void deletePlaylist(String name) throws IOException {
        Files.delete(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void writePlaylist(String name, String text) throws IOException {
        Files.write(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"), text.trim().getBytes());
    }

    public Playlist getPlaylist(String name) {
        if (!getPlaylistNames().contains(name)) {
            return null;
        }
        try {
            if (folderExists()) {
                boolean[] shuffle = {false};
                List<String> list = new ArrayList<>();
                Files.readAllLines(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt")).forEach(str -> {
                    String s = str.trim();
                    if (s.isEmpty()) {
                        return;
                    }
                    if (s.startsWith("#") || s.startsWith("//")) {
                        s = s.replaceAll("\\s+", "");
                        if (s.equalsIgnoreCase("#shuffle") || s.equalsIgnoreCase("//shuffle")) {
                            shuffle[0] = true;
                        }
                    } else {
                        list.add(s);
                    }
                });
                if (shuffle[0]) {
                    shuffle(list);
                }
                return new Playlist(name, list, shuffle[0], config);
            } else {
                createFolder();
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static <T> void shuffle(List<T> list) {
        for (int first = 0; first < list.size(); first++) {
            int second = (int) (Math.random() * list.size());
            T tmp = list.get(first);
            list.set(first, list.get(second));
            list.set(second, tmp);
        }
    }
}



