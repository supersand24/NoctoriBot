package Bot;

import Command.*;
import Game.Minecraft.GetOnlinePlayers;
import Game.Minecraft.Username;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(Listener.class);
    private final String COMMAND_SIGN = "n!";

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        log.info("Listener is ready!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.getAuthor().isBot()) {
            String message = e.getMessage().getContentStripped();
            if (message.startsWith(COMMAND_SIGN.toLowerCase()) || message.startsWith(COMMAND_SIGN.toUpperCase())) {
                String[] messageSplit = message.split("\\s+");
                String command = messageSplit[0].substring(COMMAND_SIGN.length());
                //Commands that can be used anywhere
                switch (command) {
                    case "balance" -> new Balance(e.getAuthor(),e.getChannel());
                    //Commands that can not be used anywhere.
                    default -> {
                        //Private Channel only.
                        if (e.isFromType(ChannelType.PRIVATE)) {
                            log.info("Private Command " + command + " Received!");
                            switch (command) {
                                case "notification" -> new Notification(e.getAuthor(),e.getChannel());
                                //Unknown Command
                                default -> e.getMessage().reply("Unknown Command").queue();
                            }
                        }
                        //Server Only
                        else {
                            Bank.daily(e.getMember());
                            log.info(command + " Received!");
                            switch (e.getChannel().getName()) {
                                case "minecraft" -> {
                                    switch (command) {
                                        case "username" -> new Username(e.getMember(), e.getMessage(), messageSplit);
                                        case "onlinePlayers" -> new GetOnlinePlayers(e.getMessage());
                                    }
                                }
                                case "genshin_impact" -> {
                                    switch (command) {
                                        case "uid" -> new Genshin(e.getMember(),e.getMessage(),messageSplit);
                                    }
                                }
                                default -> {
                                    switch (command) {
                                        //Unknown Command
                                        default -> e.getMessage().reply("Unknown Command").queue();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent e) {
        GuildVoiceState voiceState = e.getMember().getVoiceState();
        if (voiceState.inAudioChannel()) AutoVoiceManager.updateChannelName(voiceState.getChannel());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        Member member = e.getMember();
        Bank.daily(e.getMember());
        AutoVoiceManager.join(member,e.getChannelJoined());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        AutoVoiceManager.leave(e.getMember(),e.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        Member member = e.getMember();
        AutoVoiceManager.leave(member,e.getChannelLeft());
        AutoVoiceManager.join(member,e.getChannelJoined());
    }
}
