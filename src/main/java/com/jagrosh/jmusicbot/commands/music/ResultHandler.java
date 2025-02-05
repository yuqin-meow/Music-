package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class ResultHandler implements AudioLoadResultHandler {

    private static final String LOAD = "\uD83D\uDCE5"; // 📥
    private static final String CANCEL = "\uD83D\uDEAB"; // 🚫

    private final Bot bot;
    private final Message m;
    private final CommandEvent event;
    private final boolean ytsearch;

    public ResultHandler(Bot bot, Message m, CommandEvent event, boolean ytsearch) {
        this.bot = bot;
        this.m = m;
        this.event = event;
        this.ytsearch = ytsearch;
    }

    private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
        if (bot.getConfig().isTooLong(track)) {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                    + TimeUtil.formatTime(track.getDuration()) + "` > `" + TimeUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
            return;
        }
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
        String addMsg = FormatUtil.filter(event.getClient().getSuccess() + " Added **" + track.getInfo().title
                + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "to begin playing" : " to the queue at position " + pos));
        if (playlist == null || !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION))
            m.editMessage(addMsg).queue();
        else {
            new ButtonMenu.Builder()
                    .setText(addMsg + "\n" + event.getClient().getWarning() + " This track has a playlist of **" + playlist.getTracks().size() + "** tracks attached. Select " + LOAD + " to load playlist.")
                    .setChoices(LOAD, CANCEL)
                    .setEventWaiter(bot.getWaiter())
                    .setTimeout(30, TimeUnit.SECONDS)
                    .setAction(re -> {
                        if (re.getName().equals(LOAD))
                            m.editMessage(addMsg + "\n" + event.getClient().getSuccess() + " Loaded **" + loadPlaylist(playlist, track) + "** additional tracks!").queue();
                        else
                            m.editMessage(addMsg).queue();
                    }).setFinalAction(m -> {
                        try {
                            m.clearReactions().queue();
                        } catch (PermissionException ignore) {
                        }
                    }).build().display(m);
        }
    }

    private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
        int[] count = {0};
        playlist.getTracks().forEach(track -> {
            if (!bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event)));
                count[0]++;
            }
        });
        return count[0];
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        loadSingle(track, null);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
            AudioTrack single = playlist.getSelectedTrack() == null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            loadSingle(single, null);
        } else if (playlist.getSelectedTrack() != null) {
            AudioTrack single = playlist.getSelectedTrack();
            loadSingle(single, playlist);
        } else {
            int count = loadPlaylist(playlist, null);
            if (playlist.getTracks().isEmpty()) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " The playlist " + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                        + "**) ") + " could not be loaded or contained 0 entries")).queue();
            } else if (count == 0) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " All entries in this playlist " + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                        + "**) ") + "were longer than the allowed maximum (`" + bot.getConfig().getMaxTime() + "`)")).queue();
            } else {
                m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " Found "
                        + (playlist.getName() == null ? "a playlist" : "playlist **" + playlist.getName() + "**") + " with `"
                        + playlist.getTracks().size() + "` entries; added to the queue!"
                        + (count < playlist.getTracks().size() ? "\n" + event.getClient().getWarning() + " Tracks longer than the allowed maximum (`"
                        + bot.getConfig().getMaxTime() + "`) have been omitted." : ""))).queue();
            }
        }
    }

    @Override
    public void noMatches() {
        if (ytsearch)
            m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " No results found for `" + event.getArgs() + "`.")).queue();
        else
            bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:" + event.getArgs(), new ResultHandler(bot, m, event, true));
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        if (throwable.severity == Severity.COMMON)
            m.editMessage(event.getClient().getError() + " Error loading: " + throwable.getMessage()).queue();
        else
            m.editMessage(event.getClient().getError() + " Error loading track.").queue();
    }
}
