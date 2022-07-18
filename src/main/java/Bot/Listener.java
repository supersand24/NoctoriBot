package Bot;

import Command.*;
import Game.BlackOps3.Manager;
import Game.Minecraft.GetOnlinePlayers;
import Game.Minecraft.Username;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
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
            switch (e.getMessage().getType()) {
                case DEFAULT -> {
                    String content = e.getMessage().getContentStripped();
                    if (content.startsWith(COMMAND_SIGN.toLowerCase()) || content.startsWith(COMMAND_SIGN.toUpperCase())) {
                        String[] messageSplit = content.split("\\s+");
                        String command = messageSplit[0].substring(COMMAND_SIGN.length());
                        //Commands that can be used anywhere
                        switch (command) {
                            case "balance" -> new Balance(e.getAuthor(),e.getChannel());
                            case "profile" -> {
                                if (e.getMessage().getMentionedUsers().size() > 0) {
                                    Profile.sendProfile(e.getMessage().getMentionedUsers().get(0),e.getChannel());
                                } else {
                                    Profile.sendProfile(e.getAuthor(),e.getChannel());
                                }
                            }
                            case "setprofile" -> Profile.set(e.getMessage());
                            case "profilehelp" -> Profile.help(e.getMessage());
                            case "bo3maps" -> e.getChannel().sendMessage(Manager.getSteamCollectionURL()).queue();
                            default -> {
                                switch (e.getChannelType()) {
                                    case PRIVATE -> {
                                        log.info("Private Command " + command + " Received!");
                                        switch (command) {
                                            case "notification" -> new Notification(e.getAuthor(),e.getChannel());
                                            //Unknown Command
                                            default -> e.getMessage().reply("Unknown Command").queue();
                                        }
                                    }
                                    case GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> {
                                        log.info(command + " Received!");
                                        switch (e.getChannel().getName()) {
                                            case "Table" -> {
                                                switch (command) {
                                                    case "coinflip", "cf" -> Casino.newGame(e.getThreadChannel(),e.getMember());
                                                }
                                            }
                                        }
                                    }
                                    case TEXT -> {
                                        Bank.daily(e.getMember());
                                        log.info(command + " Received!");
                                        switch (e.getChannel().getName()) {
                                            case "bot_spam" -> {
                                                switch (command) {
                                                    case "bo3" -> Manager.sendMapLeaderboard(e.getMessage());
                                                }
                                            }
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
                                            case "casino-alpha" -> {
                                                switch (command) {
                                                    case "table" -> Casino.newTable(e.getMessage());
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
                case INLINE_REPLY -> {
                    switch (e.getChannel().getName()) {
                        case "Table" -> {
                            Casino.addBet(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent e) {
        if (!e.getUser().isBot()) {
            if ("Table".equals(e.getChannel().getName())) {
                switch (e.getReactionEmote().getEmoji()) {
                    case "\uD83C\uDDED" -> Casino.addBet(e.getMember(),e.getThreadChannel());
                    case "\uD83C\uDDF9" -> Casino.addBet(e.getMember(),e.getThreadChannel());
                }
            }
        }
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent e) {
        GuildVoiceState voiceState = e.getMember().getVoiceState();
        if (voiceState.inAudioChannel()) VoiceChannelManager.updateChannelName(voiceState.getChannel());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        Member member = e.getMember();
        Bank.daily(e.getMember());
        VoiceChannelManager.join(member,e.getChannelJoined());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        VoiceChannelManager.leave(e.getMember(),e.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        Member member = e.getMember();
        AudioChannel channelLeft = e.getChannelLeft();
        VoiceChannelManager.leave(member,channelLeft);
        if (!channelLeft.getName().equals("New Channel")) VoiceChannelManager.join(member,e.getChannelJoined());

    }
}
