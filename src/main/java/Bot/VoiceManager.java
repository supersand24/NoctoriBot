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

    protected final Logger log = LoggerFactory.getLogger(VoiceManager.class);

    //protected final List<NoctoriVoiceChannel> channels = new ArrayList<>();
    protected final static Hashtable<Long,NoctoriVoiceChannel> channels = new Hashtable<>();

    private long AUTO_VOICE_NEW_CHANNEL_ID = 984491055636443216L;
    private long AUTO_VOICE_CATEGORY_ID = 964543894832427018L;

    protected final static EnumSet<Permission> adminAllowedPermissions = EnumSet.of(
            Permission.MANAGE_CHANNEL,      Permission.CREATE_INSTANT_INVITE,
            Permission.VOICE_DEAF_OTHERS,   Permission.VOICE_MUTE_OTHERS,
            Permission.PRIORITY_SPEAKER
    );

    protected final static EnumSet<Permission> memberAllowedPermissions = EnumSet.of(
            Permission.VIEW_CHANNEL,        Permission.VOICE_SPEAK,
            Permission.MESSAGE_TTS
    );

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        log.info("Auto Voice Manager Setting Up...");
        for (StageChannel stageChannel : Main.getNoctori().getStageChannels()) {
            if (stageChannel.getName().equals("New Channel")) {
                AUTO_VOICE_NEW_CHANNEL_ID = stageChannel.getIdLong();
            }
        }
        for (VoiceChannel vc : Main.getNoctori().getCategoryById(AUTO_VOICE_CATEGORY_ID).getVoiceChannels()) {
            if (vc.getMembers().size() <= 0) { vc.delete().queue(); break; }
            NoctoriVoiceChannel av = new NoctoriVoiceChannel(vc,Main.getNoctori().getMemberById(262982533157879810L));
            for (Member member : vc.getMembers()) {
                PermissionOverride perm = vc.getPermissionOverride(member);
                if (perm != null) {
                    if (perm.getAllowed().contains(Permission.MANAGE_CHANNEL)) {
                        av.addChannelAdmin(member);
                    }
                }
            }
            channels.put(vc.getIdLong(), av);
            log.info(vc.getName() + " Auto Voice Channel added.");
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
                    vc.grantPermissions(member);
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
                if (channelLeft.getMembers().size() <= 0) {
                    channels.remove(channelLeft.getIdLong());
                    vc.delete();
                } else {
                    vc.denyPermissions(member);
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
            log.info(member.getEffectiveName() + " moved from " + channelLeft.getName() + " to " + channelJoined.getName() + ".");
            if (isAfkChannel(channelJoined)) {
                vcLeft.sendMessage("`" + member.getEffectiveName() + "` went AFK.").tts(true).queue();
            } else {
                if (!isAfkChannel(channelLeft)) {
                    vcLeft.sendMessage("`" + member.getEffectiveName() + "` moved to another call.").tts(true).queue();
                    if (channelLeft.getMembers().size() <= 0) {
                        channels.remove(channelLeft.getIdLong());
                        vcLeft.delete();
                    } else {
                        vcLeft.denyPermissions(member);
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
                    vcJoined.grantPermissions(member);
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
                    channels.get(voiceChannel.getIdLong()).grantPermissions(member);
                });
                log.info(member.getEffectiveName() + " created a New Voice Channel.");
            });
        }
    }

    @Override
    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent e) {
        if (e.getChannelType() == ChannelType.VOICE) {
            //Make sure it is an auto voice channel.
            if (!isAfkChannel(e.getChannel().asVoiceChannel())) {
                NoctoriVoiceChannel voiceChannel = channels.get(e.getChannel().getIdLong());
                voiceChannel.sendMessage("Channel was renamed to " + e.getNewValue() + ".").queue();
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
            if (voiceChannel.getTimeSinceRename() >= 10) {

                Map<String, Integer> hashMap = getVoiceChannelActivities(voiceChannel);

                System.out.println(hashMap);
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
                System.out.println("Not enough time passed, will not rename.");
            }
        }
    }

    private Map<String, Integer> getVoiceChannelActivities(NoctoriVoiceChannel voiceChannel) {
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

}

class NoctoriVoiceChannel {

    final VoiceChannel voiceChannel;
    final Member creator;

    final List<Member> channelAdmins = new ArrayList<>();

    boolean autoRename = true;
    private LocalDateTime lastRenamed = LocalDateTime.now();

    //Constructors

    public NoctoriVoiceChannel(AudioChannel audioChannel, Member member) {
        this.voiceChannel = Main.getNoctori().getVoiceChannelById(audioChannel.getId());
        this.creator = member;
        channelAdmins.add(member);
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

    //Ease of Access Commands

    protected String getName() {
        return getVoiceChannel().getName();
    }

    protected MessageAction sendMessage(String message) {
        return getVoiceChannel().sendMessage(message);
    }

    protected void updateLastRenamed() {
        lastRenamed = LocalDateTime.now();
    }

    protected void delete() {
        getVoiceChannel().delete().queue();
    }

    protected void grantPermissions(Member member) {

        EnumSet<Permission> permissions = VoiceManager.memberAllowedPermissions;
        if (getChannelAdmins().contains(member)) {
            permissions.addAll(VoiceManager.adminAllowedPermissions);
        }
        getVoiceChannel().upsertPermissionOverride(member).grant(permissions).queue();

    }

    protected void denyPermissions(Member member) {

        EnumSet<Permission> permissions = VoiceManager.memberAllowedPermissions;
        if (getChannelAdmins().contains(member)) {
            permissions.addAll(VoiceManager.adminAllowedPermissions);
        }
        getVoiceChannel().upsertPermissionOverride(member).deny(permissions).queue();

    }

    public void sendSettingsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(getName());
        embed.setDescription("Voice Channel Settings");
        embed.addField("Channel Admins", getChannelAdmins().stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")), true);
        embed.addField("Auto Rename", String.valueOf(getAutoRename()), true);
        voiceChannel.sendMessageEmbeds(embed.build()).queue();
    }

}
