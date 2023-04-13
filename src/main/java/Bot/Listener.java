package Bot;

import Command.*;
import Game.BlackOps3.Manager;
import Game.Minecraft.GetOnlinePlayers;
import Game.Minecraft.Username;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        String[] commandSplit = e.getFullCommandName().split("\\s+");
        switch (commandSplit[0]) {
            case "profile" -> {
                switch (commandSplit[1]) {
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
                switch (commandSplit[1]) {
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
                Member member = e.getMember();
                //TODO Move outside of vc switch when merged back into main.
                if (member == null) { log.error("Unknown member using slash command. /" + e.getFullCommandName()); e.reply("There was an error.").setEphemeral(true).queue(); return; }
                GuildVoiceState memberVoiceState = member.getVoiceState();
                if (memberVoiceState == null) { e.reply("You need to be in a voice channel!").setEphemeral(true).queue(); return; }
                switch (commandSplit[1]) {
                    case "music" -> {
                        Member botMember = e.getGuild().getMemberById(e.getJDA().getSelfUser().getId());
                        GuildVoiceState botVoiceState = botMember.getVoiceState();
                        if (botVoiceState == null) { log.error("Bot has a null Voice State."); e.reply("There was an error."); return; }
                        if (botVoiceState.inAudioChannel()) {
                            switch (commandSplit[2]) {
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
                                        VoiceManager.addToQueue(search, e);
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
                            }
                        } else {
                            switch (commandSplit[2]) {
                                case "leave","stop" -> e.reply("I am not in a voice channel.").queue();
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
                                    if (VoiceManager.isUrl(search)) {
                                        VoiceManager.addToQueue(search, e);
                                    } else {
                                        VoiceManager.addToQueue("ytsearch:" + search, e);
                                    }
                                }
                            }
                        }
                    }
                    case "lock" -> {
                        NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getMember().getVoiceState().getChannel().getIdLong());
                        if (vc == null) { e.reply("You are not in a voice channel that can be managed.").queue(); return; }
                        vc.setLocked(!vc.isLocked());
                        e.deferReply();
                    }
                    case "auto-rename" -> {
                        NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getMember().getVoiceState().getChannel().getIdLong());
                        if (vc == null) { e.reply("You are not in a voice channel that can be managed.").queue(); return; }
                        vc.setAutoRename(!vc.getAutoRename());
                        e.reply("Auto Rename was toggled.").queue();
                    }
                    case "give-key" -> {
                        Member giftMember = e.getOption("member").getAsMember();
                        if (giftMember == null) {e.reply("That user does not appear to be a member in the server.").setEphemeral(true).queue(); return; }
                        AudioChannelUnion channel = e.getMember().getVoiceState().getChannel();
                        if (channel == null) {
                            e.reply("You are not in a voice channel!").setEphemeral(true).queue();
                        } else {
                            VoiceManager.giveChannelKey(channel.getIdLong(),e.getMember(),member);
                            giftMember.getUser().openPrivateChannel().queue(privateChannel -> {
                                channel.createInvite().queue( invite -> {
                                    privateChannel.sendMessage(e.getMember().getEffectiveName() + " has gifted you a key for the " + channel.getName() + " voice channel.\n" +
                                            invite.getUrl()).queue();
                                });
                            });
                            e.reply(giftMember.getEffectiveName() + " has received a key.").setEphemeral(true).queue();
                        }
                    }
                    case "edit" -> {
                        NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getMember().getVoiceState().getChannel().getIdLong());
                        if (vc == null) { e.reply("You are not in a voice channel that can be managed.").queue(); return; }
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
            case "game-specific" -> {
                switch (commandSplit[1]) {
                    case "bo3" -> {
                        switch (commandSplit[2]) {
                            case "maps" -> e.reply(Manager.getSteamCollectionURL()).setEphemeral(true).queue();
                            case "leaderboard" -> {
                                String search = e.getOption("map").getAsString();
                                MessageEmbed leaderboard = Manager.getMapLeaderboard(search);
                                if (leaderboard == null) {
                                    e.reply("Could not find a map by that name.").setEphemeral(true).queue();
                                    log.error("Could not find a map by name: " + search);
                                    return;
                                }
                                e.replyEmbeds(leaderboard).queue();
                            }
                        }
                    }
                }
            }
            case "dev" -> {
                switch (commandSplit[1]) {
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
    public void onButtonInteraction(ButtonInteractionEvent e) {
        switch (e.getComponentId()) {
            case "music-addToQueue" -> {
                TextInput url = TextInput.create("url", "URL Link", TextInputStyle.SHORT)
                        .setPlaceholder("www.youtube.com/???")
                        .setRequired(false)
                        .build();
                TextInput searchYoutube = TextInput.create("search-youtube","Search on YouTube", TextInputStyle.SHORT)
                        .setPlaceholder("Radioactive by Imagine Dragons")
                        .setRequired(false)
                        .build();
                Modal modal = Modal.create("music-addToQueue", "Play Music")
                        .addActionRow(url)
                        .addActionRow(searchYoutube)
                        .build();
                e.replyModal(modal).queue();
            }
            case "music-pause" -> e.reply(VoiceManager.pauseMusic(e.getGuild())).setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            case "music-skip" -> e.reply(VoiceManager.skipTrack(e.getGuild())).setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            case "music-stop" -> e.reply(VoiceManager.stopAndClear(e.getGuild(),true)).setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            case "vc-lock" -> {
                NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getChannel().getIdLong());
                if (vc == null) { e.reply("Sorry! That can not be done.").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS)); return; }
                vc.setLocked(!vc.isLocked());
                e.reply("It worked!").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            }
            case "vc-rename" -> {
                TextInput rename = TextInput.create("name", "New Channel Name", TextInputStyle.SHORT)
                        .setPlaceholder("Vibing")
                        .setRequired(true)
                        .build();
                Modal modal = Modal.create("vc-rename", "Rename Channel")
                        .addActionRow(rename)
                        .build();
                e.replyModal(modal).queue();
            }
            case "vc-autoRename" -> {
                NoctoriVoiceChannel vc = VoiceManager.getVoiceChannel(e.getChannel().getIdLong());
                if (vc == null) { e.reply("Sorry! That can not be done.").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS)); return; }
                vc.setAutoRename(!vc.getAutoRename());
                e.reply("It worked!").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            }
            case "vc-addMusic" -> {
                GuildVoiceState memberVoiceState = e.getMember().getVoiceState();
                AudioChannelUnion channelUnion = memberVoiceState.getChannel();
                if (channelUnion.getType() == ChannelType.STAGE) { e.reply("Sorry this does not work with Stage Channels at the moment.").queue(); return; }
                VoiceManager.botJoinVoice(channelUnion.asVoiceChannel());
                e.reply("Joined the Voice Channel.").setEphemeral(true).queue();
            }
            case "vc-addAdmin" -> {
                //TODO Pick a random member from the server.
                TextInput memberSearch = TextInput.create("member", "Search for Member", TextInputStyle.SHORT)
                        .setPlaceholder("supersand24")
                        .setRequired(true)
                        .build();
                Modal modal = Modal.create("vc-addAdmin", "Add Channel Admin")
                        .addActionRow(memberSearch)
                        .build();
                e.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        switch (e.getModalId()) {
            case "music-addToQueue" -> {
                String url = e.getValue("url").getAsString();
                if (url.isEmpty()) {
                    String searchYoutube = "ytsearch:" + e.getValue("search-youtube").getAsString();
                    VoiceManager.addToQueue(searchYoutube,e.getGuild());
                    e.reply("Searching for " + e.getValue("search-youtube").getAsString() + ".");
                } else {
                    VoiceManager.addToQueue(url, e.getGuild());
                    e.reply("Adding " + url + " to queue.").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
                }
            }
            case "vc-rename" -> {
                String newName = e.getValue("name").getAsString();
                if (newName.isEmpty()) { e.reply("Not a valid name!"); return; }
                VoiceManager.getVoiceChannel(e.getChannel().getIdLong()).setName(newName);
                e.reply("It worked!").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
            }
            case "vc-addAdmin" -> {
                String memberSearch = e.getValue("member").getAsString();
                if (memberSearch.isEmpty()) { e.reply("Not a valid name!"); return; }
                List<Member> foundMembers = Main.getNoctori().getMembersByName(memberSearch, true);
                if (foundMembers.size() <= 0) {
                    e.reply("Could not find anyone with that name.").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
                } else {
                    VoiceManager.getVoiceChannel(e.getChannel().getIdLong()).addChannelAdmin(foundMembers.get(0));
                    e.reply("It worked!").setEphemeral(true).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3 ,TimeUnit.SECONDS));
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.getAuthor().isBot()) {
            Bank.daily(e.getMember());
            String content = e.getMessage().getContentStripped();
            if (e.getMessage().getType() == MessageType.INLINE_REPLY) {
                if (content.startsWith("repost to")) {
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
