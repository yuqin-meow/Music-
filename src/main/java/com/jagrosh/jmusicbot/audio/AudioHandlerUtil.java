package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class AudioHandlerUtil {

    private AudioHandlerUtil(){
        throw new IllegalStateException("Utility class");
    }

    public static void setAuthors(RequestMetadata rm, EmbedBuilder eb, Guild guild){
        if(rm.getOwner() != 0L)
            {
                User u = guild.getJDA().getUserById(rm.user.id);
                if(u==null)
                    eb.setAuthor(FormatUtil.formatUsername(rm.user), null, rm.user.avatar);
                else
                    eb.setAuthor(FormatUtil.formatUsername(u), null, u.getEffectiveAvatarUrl());
            }
    }

    public static void setTitle(EmbedBuilder eb, AudioTrack track){
        try 
        {
            eb.setTitle(track.getInfo().title, track.getInfo().uri);
        }
        catch(Exception e) 
        {
            eb.setTitle(track.getInfo().title);
        }
    }
}
