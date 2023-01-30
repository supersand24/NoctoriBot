package Bot;

import Command.*;
import Game.BlackOps3.Manager;
import Game.Minecraft.GetOnlinePlayers;
import Game.Minecraft.Username;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class Listener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(Listener.class);
    private final String COMMAND_SIGN = "n!";

    @Override
    public void onReady(ReadyEvent e) {
        log.info("Listener is ready!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "profile" -> {
                switch (e.getSubcommandName()) {
                    case "display" -> {
                        OptionMapping option = e.getOption("member");
                        if (option == null) {
                            e.replyEmbeds(Profile.getProfile(e.getMember())).queue();
                        } else {
                            if (Main.getNoctori().getMembers().stream().map(Member::getIdLong).collect(Collectors.toList()).contains(option.getAsUser().getIdLong())) {
                                e.replyEmbeds(Profile.getProfile(option.getAsMember())).queue();
                            } else {
                                e.reply("That user isn't in Noctori").setEphemeral(true).queue();
                            }
                        }
                    }
                    case "set" -> Profile.set(e);
                }
            }
            case "money" -> {
                switch (e.getSubcommandName()) {
                    case "balance" -> e.reply("$" + Var.getMoney(e.getUser())).setEphemeral(true).queue();
                    case "pay" -> {
                        int sentMoney = e.getOption("amount").getAsInt();
                        User user = e.getOption("member").getAsUser();
                        if (Var.getMoney(user) >= sentMoney) {
                            Var.addMoney(user, sentMoney);
                            e.reply("$" + sentMoney + " has been sent to " + user.getName()).queue();
                        } else {
                            e.reply("Insufficient Funds...").setEphemeral(true).queue();
                        }
                    }
                }
            }/*
            case "vc" -> {
                if (e.getMember().getVoiceState().inAudioChannel()) {
                    NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getMember().getVoiceState().getChannel().getIdLong());
                    if (vc == null) {
                        e.reply("You are not in a voice channel that can be managed.").queue();
                    } else {
                        switch (e.getSubcommandName()) {
                            case "give-key" -> {
                                Member member = e.getOption("member").getAsMember();
                                if (member == null) {
                                    e.reply("That user does not appear to be a member in the server.").setEphemeral(true).queue();
                                } else {
                                    AudioChannelUnion channel = e.getMember().getVoiceState().getChannel();
                                    if (channel == null) {
                                        e.reply("You are not in a voice channel!").setEphemeral(true).queue();
                                    } else {
                                        VoiceManager.giveChannelKey(channel.getIdLong(),e.getMember(),member);
                                        member.getUser().openPrivateChannel().queue(privateChannel -> {
                                            channel.createInvite().queue( invite -> {
                                                privateChannel.sendMessage(e.getMember().getEffectiveName() + " has gifted you a key for the " + e.getMember().getVoiceState().getChannel().getName() + " voice channel.\n" +
                                                        invite.getUrl()).queue();
                                            });
                                            //privateChannel.sendMessage(e.getMember().getEffectiveName() + " has gifted you a key for the " + e.getMember().getVoiceState().getChannel().getName() + " voice channel.").queue();
                                        });
                                        e.reply(member.getEffectiveName() + " has received a key.").setEphemeral(true).queue();
                                    }
                                }
                            }
                            case "edit" -> {
                                for (OptionMapping option : e.getOptions()) {
                                    switch (option.getName()) {
                                        case "name" -> vc.setName(option.getAsString());
                                        case "auto-rename" -> vc.setAutoRename(option.getAsBoolean());
                                        case "locked" -> vc.setLocked(option.getAsBoolean());
                                    }
                                }
                                e.reply("Edits applied.").setEphemeral(true).queue();
                            }
                        }
                    }
                } else {
                    e.reply("You are not in a voice channel.").queue();
                }
            }*/
            case "vc" -> {
                Member member = e.getMember();
                //TODO Move outside of vc switch when merged back into main.
                if (member == null) { log.error("Unknown member using slash command. /" + e.getFullCommandName()); e.reply("There was an error.").setEphemeral(true).queue(); return; }
                GuildVoiceState memberVoiceState = member.getVoiceState();
                if (memberVoiceState == null) { e.reply("You need to be in a voice channel!").setEphemeral(true).queue(); return; }
                switch (e.getSubcommandGroup()) {
                    case "music" -> {
                        Member botMember = e.getGuild().getMemberById(e.getJDA().getSelfUser().getId());
                        GuildVoiceState botVoiceState = botMember.getVoiceState();
                        if (botVoiceState == null) { log.error("Bot has a null Voice State."); e.reply("There was an error."); return; }
                        if (botVoiceState.inAudioChannel()) {
                            switch (e.getSubcommandName()) {
                                case "join" -> e.reply("I am already in a voice channel.").queue();
                                case "leave" -> {
                                    VoiceManager.botLeaveVoice(e.getGuild());
                                    e.reply("Left the Voice Channel.").setEphemeral(true).queue();
                                }
                                case "play" -> {
                                    if (botVoiceState.getChannel().getIdLong() == memberVoiceState.getChannel().getIdLong()) {
                                        String search = e.getOption("url").getAsString();
                                        if (!VoiceManager.isUrl(search)) {
                                            search = "ytsearch:" + search;
                                        }
                                        VoiceManager.loadAndPlay(search, e);
                                    } else {
                                        //TODO Add a way to pay to hijack the jukebox.
                                        //Server Boosters would get a discount on hijack price.
                                        //You can pay to have higher access and a huger discount, would depend on how many times the bot has been hijacked.
                                        e.reply("I am currently being used by another member.").queue();
                                    }
                                }
                                case "stop" -> {
                                    if (botVoiceState.getChannel().getIdLong() == memberVoiceState.getChannel().getIdLong()) {
                                        if (e.getOption("clear-queue") == null) {
                                            VoiceManager.stopAndClear(e.getGuild(),true);
                                            e.reply("I stopped playing music, and cleared the queue.").queue();
                                        } else {
                                            if (e.getOption("clear-queue").getAsBoolean()) {
                                                VoiceManager.stopAndClear(e.getGuild(),true);
                                                e.reply("I stopped playing music, and cleared the queue.").queue();
                                            } else {
                                                VoiceManager.stopAndClear(e.getGuild(),false);
                                                e.reply("I stopped playing music.").queue();
                                            }
                                        }
                                    } else {
                                        e.reply("I am currently being used by another member.").queue();
                                    }
                                }
                                case "skip" -> {
                                    if (botVoiceState.getChannel().getIdLong() == memberVoiceState.getChannel().getIdLong()) {
                                        e.reply(VoiceManager.skipTrack(e.getGuild())).queue();
                                    } else {
                                        e.reply("I am currently being used by another member.").queue();
                                    }
                                }
                                case "queue" -> {
                                    if (botVoiceState.getChannel().getIdLong() == memberVoiceState.getChannel().getIdLong()) {
                                        e.reply(VoiceManager.getQueue(e.getGuild())).queue();
                                    } else {
                                        e.reply("I am currently being used by another member.").queue();
                                    }
                                }
                            }
                        } else {
                            switch (e.getSubcommandName()) {
                                case "leave","stop","queue" -> e.reply("I am not in a voice channel.").queue();
                                case "join" -> {
                                    AudioChannelUnion channelUnion = memberVoiceState.getChannel();
                                    if (channelUnion.getType() == ChannelType.STAGE) { e.reply("Sorry this does not work with Stage Channels at the moment.").queue(); return; }
                                    VoiceManager.botJoinVoice(channelUnion.asVoiceChannel());
                                    e.reply("Joined the Voice Channel.").setEphemeral(true).queue();
                                }
                                case "play" -> {
                                    AudioChannelUnion channelUnion = memberVoiceState.getChannel();
                                    if (channelUnion.getType() == ChannelType.STAGE) { e.reply("Sorry this does not work with Stage Channels at the moment.").queue(); return; }
                                    VoiceManager.botJoinVoice(channelUnion.asVoiceChannel());
                                    String search = e.getOption("url").getAsString();
                                    if (!VoiceManager.isUrl(search)) {
                                        search = "ytsearch:" + search;
                                    }
                                    VoiceManager.loadAndPlay(search, e);
                                }
                            }
                        }
                    }
                    default -> {
                        NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getMember().getVoiceState().getChannel().getIdLong());
                        if (vc == null) { e.reply("You are not in a voice channel that can be managed.").queue(); return; }
                        switch (e.getSubcommandName()) {
                            case "give-key" -> {
                                Member giftMember = e.getOption("member").getAsMember();
                                if (giftMember == null) {e.reply("That user does not appear to be a member in the server.").setEphemeral(true).queue(); return; }
                                AudioChannelUnion channel = e.getMember().getVoiceState().getChannel();
                                if (channel == null) {
                                    e.reply("You are not in a voice channel!").setEphemeral(true).queue();
                                } else {
                                    VoiceManager.giveChannelKey(channel.getIdLong(),e.getMember(),member);
                                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                                        channel.createInvite().queue( invite -> {
                                            privateChannel.sendMessage(e.getMember().getEffectiveName() + " has gifted you a key for the " + e.getMember().getVoiceState().getChannel().getName() + " voice channel.\n" +
                                                    invite.getUrl()).queue();
                                        });
                                    });
                                    e.reply(member.getEffectiveName() + " has received a key.").setEphemeral(true).queue();
                                }
                            }
                            case "edit" -> {
                                for (OptionMapping option : e.getOptions()) {
                                    switch (option.getName()) {
                                        case "name" -> vc.setName(option.getAsString());
                                        case "auto-rename" -> vc.setAutoRename(option.getAsBoolean());
                                        case "locked" -> vc.setLocked(option.getAsBoolean());
                                    }
                                }
                                e.reply("Edits applied.").setEphemeral(true).queue();
                            }
                            case "lock" -> vc.setLocked(!vc.isLocked());
                            case "auto-rename" -> vc.setAutoRename(!vc.getAutoRename());
                        }
                    }
                }
            }
            case "dev" -> {
                switch (e.getSubcommandName()) {
                    case "print" -> {
                        switch (e.getOption("object").getAsInt()) {
                            case 0 -> {
                                Collection<NoctoriVoiceChannel> channels = VoiceManager.getNoctoriVoiceChannels();
                                if (channels.size() == 0) System.out.println("No Voice Channels, in memory."); else {
                                    for (NoctoriVoiceChannel vc : channels) System.out.println(vc);
                                    System.out.println("--------------------------------------");
                                }
                            }
                            case 1 -> {
                                for (Member member : Main.getNoctori().getMembers()) {
                                    System.out.println(Var.print(member.getUser()));
                                }
                            }
                            case 99 -> {
                                for (Member member : Main.getNoctori().getMembers()) {
                                    if (Var.getInvitedByMember(member.getUser()).equals("0") && !member.getUser().isBot()) {
                                        System.out.println(member.getEffectiveName() + " | " + member.getIdLong());
                                    }
                                }
                            }
                        }
                        e.reply("Printed to console.").setEphemeral(true).queue();
                    }
                    case "var" -> {
                        StringBuilder sb = new StringBuilder();
                        User user = e.getOption("user").getAsUser();
                        if (user.isBot()) {
                            sb.append(user.getName()).append(" is a bot.");
                        } else {
                            sb.append("Submitted changes for ").append(user.getName());
                            for (OptionMapping option : e.getOptions()) {
                                switch (option.getName()) {
                                    case "money" -> Var.setMoney(user,option.getAsInt());
                                    case "notification" -> Var.setNotification(user,option.getAsBoolean());
                                    case "genshin-uid" -> Var.setGenshinUid(user,option.getAsLong());
                                    case "minecraft-username" -> Var.setMinecraftUsername(user,option.getAsString());
                                    case "profile-fields" -> {
                                        String fields = option.getAsString();
                                        if (fields.startsWith("[") && fields.endsWith("]")) {
                                            Var.setMinecraftUsername(user,fields);
                                        } else {
                                            sb.append("\nThe profile format is incorrect.");
                                        }
                                    }
                                    case "invited-by" -> {
                                        Member invitedBy = option.getAsMember();
                                        if (invitedBy == null) {
                                            sb.append("\nThat member is not in the server.");
                                        } else {
                                            Var.setInvitedByMember(user, option.getAsMember());
                                            Var.addMemberInvited(invitedBy.getUser(),Main.getNoctori().getMemberById(user.getIdLong()));
                                        }
                                    }
                                }
                            }
                        }
                        e.reply(sb.toString()).setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.getAuthor().isBot()) {
            String content = e.getMessage().getContentStripped();
            switch (e.getMessage().getType()) {
                case DEFAULT -> {
                    if (content.startsWith(COMMAND_SIGN.toLowerCase()) || content.startsWith(COMMAND_SIGN.toUpperCase())) {
                        String[] messageSplit = content.split("\\s+");
                        String command = messageSplit[0].substring(COMMAND_SIGN.length());
                        //Commands that can be used anywhere
                        switch (command) {
                            case "balance" -> new Balance(e.getAuthor(),e.getChannel());
                            case "profile" -> {
                                if (e.getMessage().getMentions().getUsers().size() > 0) {
                                    Profile.sendProfile(e.getMessage().getMentions().getUsers().get(0),e.getChannel());
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
                                                    //case "coinflip", "cf" -> Casino.newGame(e.getThreadChannel(),e.getMember());
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
                                            case "hoyoverse" -> {
                                                switch (command) {
                                                    case "genshin-uid" -> new Genshin(e.getMember(),e.getMessage(),messageSplit);
                                                }
                                            }
                                            case "casino-alpha" -> {
                                                switch (command) {
                                                    //case "table" -> Casino.newTable(e.getMessage());
                                                }
                                            }
                                            default -> {
                                                switch (command) {
                                                    //Unknown Command
                                                    case "pay" -> {
                                                        if (e.getMessage().getMentions().getMembers().size() > 0) {
                                                            Bank.payMember(Integer.parseInt(messageSplit[1]),e.getMember(), e.getMessage().getMentions().getMembers().get(0));
                                                            e.getMessage().reply("Payment Successful.").queue();
                                                        }
                                                    }
                                                    default -> e.getMessage().reply("Unknown Command").queue();
                                                }
                                            }
                                        }
                                    }
                                    case VOICE -> {
                                        log.info("Voice Command " + command + " Received!");
                                        switch (command) {
                                            //Unknown Command
                                            case "help" -> VoiceManager.sendHelpEmbed(e.getChannel().asVoiceChannel());
                                            case "toggleLock" -> VoiceManager.toggleChannelLock(e.getChannel().getIdLong(), e.getMember());
                                            case "settings" -> VoiceManager.getVoiceChannel(e.getChannel().getIdLong()).sendSettingsEmbed();
                                            default -> e.getMessage().reply("Unknown Command").queue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                case INLINE_REPLY -> {
                    if (content.startsWith("repost to")) {
                        e.getMessage().getMentions().getChannels().get(0).getIdLong();
                        GuildChannel targetChannel = e.getMessage().getMentions().getChannels().get(0);
                        Message messageToBeMoved = null;
                        switch (targetChannel.getType()) {
                            case TEXT -> {
                                messageToBeMoved = e.getMessage().getReferencedMessage();
                                e.getGuild().getTextChannelById(targetChannel.getId()).sendMessage(
                                        "***Message from " + messageToBeMoved.getAuthor().getAsMention() + " has been moved from " + e.getChannel().getAsMention() + " to this channel.***\n"
                                                + messageToBeMoved.getContentRaw()
                                ).queue();
                            }
                            case GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> {
                                messageToBeMoved = e.getMessage().getReferencedMessage();
                                e.getGuild().getThreadChannelById(targetChannel.getId()).sendMessage(
                                        "***Message from " + messageToBeMoved.getAuthor().getAsMention() + " has been moved from " + e.getChannel().getAsMention() + " to this channel.***\n"
                                                + messageToBeMoved.getContentRaw()
                                ).queue();
                            }
                            case VOICE -> {
                                messageToBeMoved = e.getMessage().getReferencedMessage();
                                e.getGuild().getVoiceChannelById(targetChannel.getId()).sendMessage(
                                        "***Message from " + messageToBeMoved.getAuthor().getAsMention() + " has been moved from " + e.getChannel().getAsMention() + " to this channel.***\n"
                                                + messageToBeMoved.getContentRaw()
                                ).queue();
                            }
                            default -> {
                                log.error("Unknown Channel Type -> " + targetChannel.getType());
                                e.getMessage().delete().queue();
                            }
                        }
                        if (messageToBeMoved != null) {
                            messageToBeMoved.delete().queue();
                            e.getMessage().delete().queue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(e.getUser().getName(), e.getUser().getEffectiveAvatarUrl(), e.getUser().getEffectiveAvatarUrl());
        embed.setDescription(e.getUser().getName() + " joined Noctori.");
        embed.setImage(e.getUser().getAvatarUrl());
        embed.addField("Is Bot", String.valueOf(e.getUser().isBot()),true);
        embed.setFooter("Account Creation Date");
        embed.setTimestamp(e.getUser().getTimeCreated());
        Main.getLogChannel().sendMessageEmbeds(embed.build()).queue();
        Var.createNewVariableFile(e.getMember());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(e.getUser().getName(), e.getUser().getEffectiveAvatarUrl(), e.getUser().getEffectiveAvatarUrl());
        embed.setDescription(e.getUser().getName() + " left Noctori.");
        embed.setImage(e.getUser().getAvatarUrl());
        embed.addField("Bot", String.valueOf(e.getUser().isBot()),true);
        Main.getLogChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
