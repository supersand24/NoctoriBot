package Bot;

import Command.*;
import Game.BlackOps3.Manager;
import Game.Minecraft.GetOnlinePlayers;
import Game.Minecraft.Username;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
            }
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
                                            privateChannel.sendMessage(e.getMember().getEffectiveName() + " has gifted you a key for the " + e.getMember().getVoiceState().getChannel().getName() + " voice channel.").queue();
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
                    switch (e.getChannel().getName()) {
                        case "Table" -> {
                            //Casino.addBet(e.getMessage());
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
