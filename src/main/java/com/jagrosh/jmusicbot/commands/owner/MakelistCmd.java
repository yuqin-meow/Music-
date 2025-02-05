package com.jagrosh.jmusicbot.commands.owner;

import java.io.IOException;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;

public class MakelistCmd extends OwnerCommand 
    {
        
     public MakelistCmd(Bot bot)
        {
            this.bot = bot;
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) 
        {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            pname = pname.replaceAll("[*?|\\/\":<>]", "");
            if(pname == null || pname.isEmpty()) 
            {
                event.replyError("Please provide a name for the playlist!");
            } 
            else if(bot.getPlaylistLoader().getPlaylist(pname) == null)
            {
                try
                {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess()+" Successfully created playlist `"+pname+"`!");
                }
                catch(IOException e)
                {
                    event.reply(event.getClient().getError()+" I was unable to create the playlist: "+e.getLocalizedMessage());
                }
            }
            else
                event.reply(event.getClient().getError()+" Playlist `"+pname+"` already exists!");
        }
    }