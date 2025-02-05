package com.jagrosh.jmusicbot.audio;

import java.nio.ByteBuffer;

import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

public class StatusHandler extends AudioEventAdapter implements AudioSendHandler {

    public static final String PLAY_EMOJI  = "\u25B6"; // ▶
    public static final String PAUSE_EMOJI = "\u23F8"; // ⏸
    public static final String STOP_EMOJI  = "\u23F9"; // ⏹

    private final AudioPlayer audioPlayer;
    private final long guildId;
    private final PlayerManager manager;

    private AudioFrame lastFrame;

    protected StatusHandler(PlayerManager manager, Guild guild, AudioPlayer player)
    {
        this.manager = manager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();
    }

    public boolean isMusicPlaying(JDA jda)
    {
        return guild(jda).getSelfMember().getVoiceState().inVoiceChannel() && audioPlayer.getPlayingTrack()!=null;
    }


    public Message getNowPlaying(JDA jda)
    {
        if(isMusicPlaying(jda))
        {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageBuilder mb = new MessageBuilder();
            mb.append(FormatUtil.filter(manager.getBot().getConfig().getSuccess()+" **Now Playing in "+guild.getSelfMember().getVoiceState().getChannel().getAsMention()+"...**"));
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(guild.getSelfMember().getColor());
            RequestMetadata rm = getRequestMetadata();
            AudioHandlerUtil.setAuthors(rm, eb, guild);
            AudioHandlerUtil.setTitle(eb, track);
            if(track instanceof YoutubeAudioTrack && manager.getBot().getConfig().useNPImages())
            {
                eb.setThumbnail("https://img.youtube.com/vi/"+track.getIdentifier()+"/mqdefault.jpg");
            }
            
            if(track.getInfo().author != null && !track.getInfo().author.isEmpty())
                eb.setFooter("Source: " + track.getInfo().author, null);

            double progress = (double)audioPlayer.getPlayingTrack().getPosition()/track.getDuration();
            eb.setDescription(getStatusEmoji()
                    + " "+FormatUtil.progressBar(progress)
                    + " `[" + TimeUtil.formatTime(track.getPosition()) + "/" + TimeUtil.formatTime(track.getDuration()) + "]` "
                    + FormatUtil.volumeIcon(audioPlayer.getVolume()));
            
            return mb.setEmbeds(eb.build()).build();
        }
        else return null;
    }

    public Message getNoMusicPlaying(JDA jda)
    {
        Guild guild = guild(jda);
        return new MessageBuilder()
                .setContent(FormatUtil.filter(manager.getBot().getConfig().getSuccess()+" **Now Playing...**"))
                .setEmbeds(new EmbedBuilder()
                .setTitle("No music playing")
                .setDescription(STOP_EMOJI+" "+FormatUtil.progressBar(-1)+" "+FormatUtil.volumeIcon(audioPlayer.getVolume()))
                .setColor(guild.getSelfMember().getColor())
                .build()).build();
    }

    public String getStatusEmoji()
    {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;
    }

    public RequestMetadata getRequestMetadata()
    {
        if(audioPlayer.getPlayingTrack() == null)
            return RequestMetadata.EMPTY;
        RequestMetadata rm = audioPlayer.getPlayingTrack().getUserData(RequestMetadata.class);
        return rm == null ? RequestMetadata.EMPTY : rm;
    }

    private Guild guild(JDA jda)
    {
        return jda.getGuildById(guildId);
    }

   @Override
    public boolean canProvide() 
    {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() 
    {
        return ByteBuffer.wrap(lastFrame.getData());
    }
    
}
