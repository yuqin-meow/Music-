package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class BotConfig {
    private static final String CONTEXT = "Config";
    private final Prompt prompt;
    private Path path;
    private Config config;
    private boolean valid;
    
    private String token, prefix, altprefix, helpWord, playlistsFolder, logLevel, 
                   successEmoji, warningEmoji, errorEmoji, loadingEmoji, searchingEmoji, evalEngine;
    private boolean stayInChannel, songInGame, npImages, updatealerts, useEval, dbots;
    private long owner, maxSeconds, aloneTimeUntilStop;
    private int maxYTPlaylistPages;
    private double skipratio;
    private OnlineStatus status;
    private Activity game;
    private Config aliases, transforms;
    
    public BotConfig(Prompt prompt) {
        this.prompt = prompt;
        load();
    }
    
    private void load() {
        try {
            path = getConfigPath();
            config = ConfigFactory.load();
            parseConfig();
            validateConfig();
            valid = true;
        } catch (ConfigException ex) {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, "Config error: " + ex.getMessage());
        }
    }
    
    private void parseConfig() {
        token = config.getString("token");
        prefix = config.getString("prefix");
        altprefix = config.getString("altprefix");
        helpWord = config.getString("help");
        owner = config.getLong("owner");
        successEmoji = config.getString("success");
        warningEmoji = config.getString("warning");
        errorEmoji = config.getString("error");
        loadingEmoji = config.getString("loading");
        searchingEmoji = config.getString("searching");
        game = OtherUtil.parseGame(config.getString("game"));
        status = OtherUtil.parseStatus(config.getString("status"));
        stayInChannel = config.getBoolean("stayinchannel");
        songInGame = config.getBoolean("songinstatus");
        npImages = config.getBoolean("npimages");
        updatealerts = config.getBoolean("updatealerts");
        logLevel = config.getString("loglevel");
        useEval = config.getBoolean("eval");
        evalEngine = config.getString("evalengine");
        maxSeconds = config.getLong("maxtime");
        maxYTPlaylistPages = config.getInt("maxytplaylistpages");
        aloneTimeUntilStop = config.getLong("alonetimeuntilstop");
        playlistsFolder = config.getString("playlistsfolder");
        aliases = config.getConfig("aliases");
        transforms = config.getConfig("transforms");
        skipratio = config.getDouble("skipratio");
        dbots = owner == 113156185389092864L;
    }
    
    private void validateConfig() {
        boolean write = false;
        if (token == null || token.isEmpty() || "BOT_TOKEN_HERE".equalsIgnoreCase(token)) {
            token = promptForValue("Please provide a bot token", "Bot Token: ");
            write = true;
        }
        if (owner <= 0) {
            owner = promptForLong("Owner ID is missing or invalid", "Owner User ID: ");
            write = true;
        }
        if (write) writeToFile();
    }
    
    private String promptForValue(String message, String promptText) {
        return prompt.prompt(message + "\n" + promptText);
    }
    
    private long promptForLong(String message, String promptText) {
        try {
            return Long.parseLong(promptForValue(message, promptText));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
    
    private void writeToFile() {
        try {
            Files.write(path, loadDefaultConfig().replace("BOT_TOKEN_HERE", token)
                .replace("0 // OWNER ID", Long.toString(owner)).getBytes());
        } catch (IOException ex) {
            prompt.alert(Prompt.Level.WARNING, CONTEXT, "Failed to write config: " + ex.getMessage());
        }
    }
    
    private static Path getConfigPath() {
        return OtherUtil.getPath(System.getProperty("config.file", "config.txt"));
    }
    
    public boolean isValid() { return valid; }
    public String getPrefix() { return prefix; }
    public String getAltPrefix() { return "NONE".equalsIgnoreCase(altprefix) ? null : altprefix; }
    public String getToken() { return token; }
    public double getSkipRatio() { return skipratio; }
    public long getOwnerId() { return owner; }
    public Activity getGame() { return game; }
    public OnlineStatus getStatus() { return status; }
    public boolean getStay() { return stayInChannel; }
    public boolean getSongInStatus() { return songInGame; }
    public boolean useUpdateAlerts() { return updatealerts; }
    public boolean useEval() { return useEval; }
    public String getEvalEngine() { return evalEngine; }
    public boolean useNPImages() { return npImages; }
    public long getMaxSeconds() { return maxSeconds; }
    public int getMaxYTPlaylistPages() { return maxYTPlaylistPages; }
    public long getAloneTimeUntilStop() { return aloneTimeUntilStop; }
    public boolean isTooLong(AudioTrack track) { return maxSeconds > 0 && track.getDuration() / 1000.0 > maxSeconds; }
}
