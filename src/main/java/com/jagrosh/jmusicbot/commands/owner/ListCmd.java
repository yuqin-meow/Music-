package com.jagrosh.jmusicbot.commands.owner;

import java.util.List;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;

public class ListCmd extends OwnerCommand 
    {

        public ListCmd(Bot bot)
        {
            this.name = "all";
            this.aliases = new String[]{"available","list"};
            this.help = "lists all available playlists";
            this.guildOnly = true;
            this.bot = bot;
        }

        @Override
        protected void execute(CommandEvent event) 
        {
            if(!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if(!bot.getPlaylistLoader().folderExists())
            {
                event.reply(event.getClient().getWarning()+" Playlists folder does not exist and could not be created!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if(list==null)
                event.reply(event.getClient().getError()+" Failed to load available playlists!");
            else if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" There are no playlists in the Playlists folder!");
            else
            {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess()+" Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
