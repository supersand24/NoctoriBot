package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class VoiceManager extends ListenerAdapter {

    protected static final Logger log = LoggerFactory.getLogger(VoiceManager.class);

    protected final static Hashtable<Long,NoctoriVoiceChannel> channels = new Hashtable<>();

    private long AUTO_VOICE_NEW_CHANNEL_ID = 984491055636443216L;
    private long AUTO_VOICE_CATEGORY_ID = 964543894832427018L;

    protected final static EnumSet<Permission> adminAllowedPermissions = EnumSet.of(
            Permission.MANAGE_CHANNEL,      Permission.CREATE_INSTANT_INVITE,
            Permission.VOICE_DEAF_OTHERS,   Permission.VOICE_MUTE_OTHERS,
            Permission.PRIORITY_SPEAKER
    );

    protected final static EnumSet<Permission> memberAllowedPermissions = EnumSet.of(
            Permission.VOICE_SPEAK,         Permission.MESSAGE_TTS
    );

    protected final static EnumSet<Permission> joinChannelPermissions = EnumSet.of(
            Permission.MESSAGE_SEND
    );

    protected final static EnumSet<Permission> lockChannelPermissions = EnumSet.of(
            Permission.VOICE_CONNECT
    );

    protected final static EnumSet<Permission> newChannelPermissions = EnumSet.of(
            Permission.VIEW_CHANNEL,        Permission.VOICE_SPEAK,
            Permission.VOICE_STREAM,        Permission.MESSAGE_TTS
    );

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        log.info("Auto Voice Manager Setting Up...");
        //Locate the New Channel, and update the voice channel id.
        for (StageChannel stageChannel : Main.getNoctori().getStageChannels())
            if (stageChannel.getName().equals("New Channel")) AUTO_VOICE_NEW_CHANNEL_ID = stageChannel.getIdLong();

        //Check all voice channels
        for (VoiceChannel vc : Main.getNoctori().getVoiceChannels()) {
            //Make sure the channel isn't the AFK Channel.
            if (vc.getIdLong() != Main.getNoctori().getAfkChannel().getIdLong()) {
                //Delete Abandoned Channels
                if (vc.getMembers().size() <= 0) {
                    vc.delete().queue();
                    break;
                }

                //Needs reworking.
                NoctoriVoiceChannel av = new NoctoriVoiceChannel(vc);
                for (Member member : vc.getMembers()) {
                    PermissionOverride perm = vc.getPermissionOverride(member);
                    if (perm != null) {
                        if (perm.getAllowed().contains(Permission.MANAGE_CHANNEL)) {
                            av.addChannelAdmin(member);
                            log.info(member.getEffectiveName() + " was made a Channel Admin.");
                        }
                    }
                }

                //Default on the creator.
                if (av.creator == null) av.getChannelAdmins().get(0);

                //Check if channel is locked.
                av.setLocked(vc.getPermissionOverride(Main.getNoctori().getPublicRole()).getDenied().contains(Permission.VOICE_CONNECT));

                //Finally, add to master list.
                channels.put(vc.getIdLong(), av);
                log.info(vc.getName() + " Auto Voice Channel added.");
            }
        }
        log.info("Auto Voice Manager Ready!");
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent e) {
        log.info("Auto Voice Manager Restarting...");
        log.info("Auto Voice Manager Ready!");
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        Member member = e.getMember();
        AudioChannel channelJoined = e.getChannelJoined();
        if (!isAfkChannel(channelJoined)) {
            switch (channelJoined.getType()) {
                case VOICE -> {
                    log.info(member.getEffectiveName() + " joined " + channelJoined.getName() + ".");
                    NoctoriVoiceChannel vc = channels.get(channelJoined.getIdLong());
                    vc.sendMessage("`" + member.getEffectiveName() + "` joined the call.").tts(true).queue();
                    vc.getVoiceChannel().upsertPermissionOverride(member).grant(joinChannelPermissions).queue();
                }
                case STAGE -> newChannel((StageChannel) channelJoined,member);
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        Member member = e.getMember();
        AudioChannel channelLeft = e.getChannelLeft();
        if (!isAfkChannel(channelLeft)) {
            if (channelLeft.getType() == ChannelType.VOICE) {
                NoctoriVoiceChannel vc = channels.get(channelLeft.getIdLong());
                log.info(member.getEffectiveName() + " left " + channelLeft.getName() + ".");
                vc.getVoiceChannel().sendMessage("`" + member.getEffectiveName() + "` left the call.").tts(true).queue();
                vc.removeChannelAdmin(member);
                if (channelLeft.getMembers().size() <= 0) {
                    channels.remove(channelLeft.getIdLong());
                    vc.delete();
                } else {
                    vc.getVoiceChannel().upsertPermissionOverride(member).deny(joinChannelPermissions).queue();
                    rename(vc);
                }
            }
        }
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        Member member = e.getMember();
        AudioChannel channelJoined = e.getChannelJoined();
        AudioChannel channelLeft = e.getChannelLeft();
        NoctoriVoiceChannel vcJoined = channels.get(channelJoined.getIdLong());
        NoctoriVoiceChannel vcLeft = channels.get(channelLeft.getIdLong());

        if (channelLeft.getType() == ChannelType.VOICE) {
            if (channelJoined.getType() == ChannelType.STAGE)
                log.info(member.getEffectiveName() + " moved from " + channelLeft.getName() + " to create a new channel.");
            else
                log.info(member.getEffectiveName() + " moved from " + channelLeft.getName() + " to " + channelJoined.getName() + ".");
            if (isAfkChannel(channelJoined)) {
                vcLeft.sendMessage("`" + member.getEffectiveName() + "` went AFK.").tts(true).queue();
                if (channelLeft.getMembers().size() <= 0) {
                    channels.remove(channelLeft.getIdLong());
                    vcLeft.delete();
                }
            } else {
                if (!isAfkChannel(channelLeft)) {
                    vcLeft.sendMessage("`" + member.getEffectiveName() + "` moved to another call.").tts(true).queue();
                    vcLeft.removeChannelAdmin(member);
                    if (channelLeft.getMembers().size() <= 0) {
                        channels.remove(channelLeft.getIdLong());
                        vcLeft.delete();
                    } else {
                        vcLeft.getVoiceChannel().upsertPermissionOverride(member).deny(joinChannelPermissions).queue();
                        rename(vcLeft);
                    }
                }
            }
        }

        switch (channelJoined.getType()) {
            case VOICE -> {
                if (!isAfkChannel(channelJoined)) {
                    if (isAfkChannel(channelLeft))
                        vcJoined.sendMessage("`" + member.getEffectiveName() + "` returned from AFK.").tts(true).queue();
                    else
                        vcJoined.sendMessage("`" + member.getEffectiveName() + "` joined from another call.").tts(true).queue();
                    vcJoined.getVoiceChannel().upsertPermissionOverride(member).grant(joinChannelPermissions).queue();
                }
            }
            case STAGE -> newChannel((StageChannel) channelJoined, member);
        }
    }

    private void newChannel(StageChannel stageChannel, Member member) {
        if (stageChannel.getIdLong() == AUTO_VOICE_NEW_CHANNEL_ID) {
            stageChannel.getParentCategory().createVoiceChannel("Vibing").setPosition(0).queue(voiceChannel -> {
                channels.put(voiceChannel.getIdLong(), new NoctoriVoiceChannel(voiceChannel,member));
                Main.getNoctori().moveVoiceMember(member, voiceChannel).queue(unused -> {
                    NoctoriVoiceChannel vc = channels.get(voiceChannel.getIdLong());
                    vc.getVoiceChannel().upsertPermissionOverride(Main.getNoctori().getPublicRole()).grant(newChannelPermissions).queue();
                    vc.getVoiceChannel().upsertPermissionOverride(member).grant(adminAllowedPermissions).grant(joinChannelPermissions).queue();
                    log.info(member.getEffectiveName() + " was made a Channel Admin.");
                });
                log.info(member.getEffectiveName() + " created a New Voice Channel.");
            });
        }
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent e) {
        GuildVoiceState voiceState = e.getMember().getVoiceState();
        if (voiceState.inAudioChannel()) rename(getVoiceChannel(voiceState.getChannel().getIdLong()));
    }

    @Override
    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent e) {
        if (e.getChannelType() == ChannelType.VOICE) {
            //Make sure it is an auto voice channel.
            if (!isAfkChannel(e.getChannel().asVoiceChannel())) {
                NoctoriVoiceChannel voiceChannel = channels.get(e.getChannel().getIdLong());
                voiceChannel.sendMessage("Channel was renamed to `" + e.getNewValue() + "`.").queue();
                voiceChannel.updateLastRenamed();
                voiceChannel.setAutoRename(false);
            }
        }
    }

    private boolean isAfkChannel(AudioChannel audioChannel) {
        if (Main.getNoctori().getAfkChannel() == null) return false;
        return audioChannel.getIdLong() == Main.getNoctori().getAfkChannel().getIdLong();
    }

    private void rename(NoctoriVoiceChannel voiceChannel) {
        //If Auto Rename is turned on.
        if (voiceChannel.getAutoRename()) {
            //If Enough Time has passed.
            log.info("Attempting to rename " + voiceChannel.getName() + ", " + voiceChannel.getTimeSinceRename() + " mins have passed since last rename.");
            if (voiceChannel.getTimeSinceRename() >= 10) {

                Map<String, Integer> hashMap = getVoiceChannelActivities(voiceChannel);
                log.info(voiceChannel.getName() + " current activities : " + hashMap);

                String mostCommonKey = "Vibing";
                int maxValue = 1;
                for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                    if (entry.getValue() > maxValue) {
                        mostCommonKey = entry.getKey();
                        maxValue = entry.getValue();
                    }
                }

                if (!mostCommonKey.equals(voiceChannel.getName())) {
                    voiceChannel.sendMessage("Channel was automatically renamed to `" + mostCommonKey + "`.").queue();
                    voiceChannel.getVoiceChannel().getManager().setName(mostCommonKey).queue();
                    voiceChannel.updateLastRenamed();
                    log.info(voiceChannel.getVoiceChannel().getName() + " was automatically renamed to `" + mostCommonKey + "`.");
                }

            } else {
                log.trace("Not enough time passed, will not rename " + voiceChannel.getName() + " channel.");
            }
        }
    }

    protected static Map<String, Integer> getVoiceChannelActivities(NoctoriVoiceChannel voiceChannel) {
        List<Member> members = voiceChannel.getMembers();
        Map<String, Integer> hashMap = new HashMap<>();
        for (Member member : members) {
            if (!member.getUser().isBot()) {
                for (Activity activity : member.getActivities()) {
                    switch (activity.getType()) {
                        case PLAYING -> {
                            switch (activity.getName()) {
                                case "Fortnite" -> {
                                    if (activity.isRich()) {
                                        String details = activity.asRichPresence().getDetails();
                                        if (details == null) {
                                            hashMap.merge(activity.getName(), 1, Integer::sum);
                                        } else {
                                            switch (details.split(" ")[0]) {
                                                case "Battle" -> hashMap.merge("Fortnite: Battle Royale", 1, Integer::sum);
                                                case "Save" -> hashMap.merge("Fortnite: Save the World", 1, Integer::sum);
                                                default -> {
                                                    log.error("Unchecked Fortnite Rich Presence Case : " + activity.asRichPresence().getDetails());
                                                    hashMap.merge(activity.getName(), 1, Integer::sum);
                                                }
                                            }
                                        }
                                    }
                                }
                                case "tModLoader" -> hashMap.merge("Terraria", 1, Integer::sum);
                                default -> hashMap.merge(activity.getName(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        return hashMap;
    }

    public static NoctoriVoiceChannel getVoiceChannel(long channelId) {
        return channels.get(channelId);
    }

    public static void toggleChannelLock(long channelId, Member commandAuthor) {
        NoctoriVoiceChannel vc = getVoiceChannel(channelId);
        if (vc == null) return;
        if (vc.channelAdmins.contains(commandAuthor)) {
            if (vc.isLocked()) vc.unlockChannel(); else vc.lockChannel();
        }
    }

    public static void sendHelpEmbed(VoiceChannel voiceChannel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Voice Commands Help");
        embed.setDescription("Various Voice Channel Commands.");

        embed.addField("n!help", "Shows this message box.", true);
        embed.addField("n!settings", "Shows the current Voice Channel Settings.", true);
        embed.addField("n!toggleLock", "Unlock/Locks the voice channel.", true);

        voiceChannel.sendMessageEmbeds(embed.build()).queue();
    }

}

class NoctoriVoiceChannel {

    final VoiceChannel voiceChannel;
    Member creator;

    final List<Member> channelAdmins = new ArrayList<>();

    boolean autoRename = true;
    private LocalDateTime lastRenamed = LocalDateTime.now();

    private boolean locked = false;

    //Constructors

    @Deprecated
    public NoctoriVoiceChannel(VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
    }

    public NoctoriVoiceChannel(VoiceChannel voiceChannel, Member member) {
        this.voiceChannel = voiceChannel;
        this.creator = member;
        channelAdmins.add(member);
    }

    //Getters and Setters

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void addChannelAdmin(Member member) {
        channelAdmins.add(member);
    }

    public void removeChannelAdmin(Member member) {
        channelAdmins.remove(member);
    }

    public List<Member> getChannelAdmins() {
        return channelAdmins;
    }

    public List<Member> getMembers() {
        return getVoiceChannel().getMembers();
    }

    protected void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    public boolean getAutoRename() {
        return autoRename;
    }

    public long getTimeSinceRename() {
        return Duration.between(lastRenamed,LocalDateTime.now()).toMinutes();
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    //Ease of Access Commands

    protected String getName() {
        return getVoiceChannel().getName();
    }

    protected void updateLastRenamed() {
        lastRenamed = LocalDateTime.now();
    }

    protected void lockChannel() {
        setLocked(true);
        getVoiceChannel().upsertPermissionOverride(Main.getNoctori().getPublicRole()).deny(VoiceManager.lockChannelPermissions).queue(permissionOverride ->
            sendMessage("This channel is now `locked`.").queue()
        );
    }

    protected void unlockChannel() {
        setLocked(false);
        getVoiceChannel().upsertPermissionOverride(Main.getNoctori().getPublicRole()).grant(VoiceManager.lockChannelPermissions).queue(permissionOverride ->
            sendMessage("This channel is now `unlocked`.").queue()
        );
    }

    protected MessageAction sendMessage(String message) {
        return getVoiceChannel().sendMessage(message);
    }

    protected void delete() {
        getVoiceChannel().delete().queue();
    }

    public void sendSettingsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(getName());
        embed.setDescription("Voice Channel Settings");
        embed.addField("Auto Rename", String.valueOf(getAutoRename()), true);
        embed.addField("Locked", String.valueOf(isLocked()), true);
        embed.addField("", "", true);
        embed.addField("Channel Admins", getChannelAdmins().stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")), false);
        Map<String,Integer> activities = VoiceManager.getVoiceChannelActivities(this);
        if (!activities.isEmpty())
            embed.addField("Channel Activities", String.join("\n", activities.keySet()), false);
        voiceChannel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public String toString() {
        return "Voice Channel ID=" + voiceChannel.getIdLong() +
                "\nCreator=" + creator.getEffectiveName() +
                "\nChannel Admins=" + channelAdmins.stream().map(Member::getEffectiveName).toList() +
                "\nAuto Rename=" + autoRename +
                "\nLast Renamed=" + lastRenamed.toLocalTime() +
                "\nLocked=" + locked;
    }
}
