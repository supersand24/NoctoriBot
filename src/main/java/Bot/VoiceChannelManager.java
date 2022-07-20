package Bot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VoiceChannelManager {

    protected final static Logger log = LoggerFactory.getLogger(VoiceChannelManager.class);
    public final static List<AutoVoice> channels = new ArrayList<>();

    private final static long CATEGORY_NAME = 964543894832427018L;

    public final static EnumSet<Permission> a = EnumSet.of(Permission.MANAGE_CHANNEL);

    protected final static EnumSet<Permission> adminAllowedPermissions = EnumSet.of(
            Permission.MANAGE_CHANNEL,      Permission.CREATE_INSTANT_INVITE,
            Permission.VOICE_DEAF_OTHERS,   Permission.VOICE_MUTE_OTHERS,
            Permission.PRIORITY_SPEAKER
    );

    protected final static EnumSet<Permission> memberAllowedPermissions = EnumSet.of(
            Permission.VIEW_CHANNEL,        Permission.VOICE_SPEAK,
            Permission.MESSAGE_TTS
    );

    public static void initialize() {
        log.info("Auto Voice Manager Setting Up...");
        for (VoiceChannel vc : Main.getNoctori().getCategoryById(CATEGORY_NAME).getVoiceChannels()) {
            if (vc.getMembers().size() <= 0) { vc.delete().queue(); break; }
            AutoVoice av = new AutoVoice(vc);
            for (Member member : vc.getMembers()) {
                PermissionOverride perm = vc.getPermissionOverride(member);
                if (perm != null) {
                    if (perm.getAllowed().contains(Permission.MANAGE_CHANNEL)) {
                        av.addChannelAdmin(member);
                    }
                }
            }
            channels.add(av);
            log.info(vc.getName() + " Auto Voice Channel added.");
        }
        log.info("Auto Voice Manager Ready!");
    }

    public static void join(Member member, AudioChannel audioChannel) {
        if (audioChannel.getName().equalsIgnoreCase("New Channel")) {
            newChannel(member, (StageChannel) audioChannel);
        } else {
            log.info(member.getEffectiveName() + " joined " + audioChannel.getName() + ".");
            AutoVoice av = getAutoVoice(audioChannel);
            if (av != null) {
                av.getVoiceChannel().sendMessage("`" + member.getEffectiveName() + "` joined the call.").tts(true).queue();
                av.getVoiceChannel().upsertPermissionOverride(member).grant(memberAllowedPermissions).queue();
                updateChannelName(audioChannel);
            }
        }
    }

    public static void leave(Member member, AudioChannel audioChannel) {
        if (!audioChannel.getName().equals("New Channel")) {
            VoiceChannel voiceChannel = (VoiceChannel) audioChannel;
            if (voiceChannel.getParentCategory().getIdLong() == CATEGORY_NAME) {
                log.info(member.getEffectiveName() + " left " + audioChannel.getName() + ".");
                //Delete Channel if no one is left.
                if (voiceChannel.getMembers().size() <= 0) {
                    channels.remove(getAutoVoice(voiceChannel));
                    voiceChannel.delete().queue();
                } else {
                    if (audioChannel.getType() == ChannelType.VOICE) {
                        Main.getNoctori().getVoiceChannelById(audioChannel.getId()).sendMessage("`" + member.getEffectiveName() + "` left the call.").tts(true).queue();
                        getAutoVoice(audioChannel).getVoiceChannel().getPermissionOverride(member).getManager().reset().queue();
                        //getAutoVoice(audioChannel).getVoiceChannel().getPermissionOverride(member).getManager().deny(memberAllowedPermissions).queue();
                        getAutoVoice(audioChannel).removeChannelAdmin(member);
                        updateChannelName(audioChannel);
                    }
                }
            }
        }
    }

    public static void newChannel(Member member, StageChannel stageChannel) {
        stageChannel.getParentCategory().createVoiceChannel("Vibing").setPosition(0).queue(voiceChannel -> {
            log.info(member.getEffectiveName() + " created a New Voice Channel.");
            voiceChannel.sendMessage("`" + member.getEffectiveName() + "` created a new channel").queue();
            AutoVoice av = new AutoVoice(voiceChannel);
            channels.add(av);
            Main.getNoctori().moveVoiceMember(member,voiceChannel).queue();
            voiceChannel.upsertPermissionOverride(member).grant(memberAllowedPermissions).submit().whenComplete((permissionOverride, throwable) -> {av.addChannelAdmin(member);});
        });
    }

    public static void updateChannelName(AudioChannel audioChannel) {
        if (!audioChannel.getName().equals("New Channel")) {
            if (audioChannel.getType() == ChannelType.VOICE) {
                VoiceChannel voiceChannel = (VoiceChannel) audioChannel;
                if (voiceChannel.getParentCategory().getIdLong() == CATEGORY_NAME) {
                    Map<String, Integer> hashMap = getVoiceChannelActivities(voiceChannel);
                    String mostCommonKey = "Vibing";
                    int maxValue = 1;
                    for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                        if (entry.getValue() > maxValue) {
                            mostCommonKey = entry.getKey();
                            maxValue = entry.getValue();
                        }
                    }

                    if (voiceChannel.getName().equals(mostCommonKey)) {
                        log.debug("Attempted to rename " + voiceChannel.getName() + " VC to " + mostCommonKey);
                    } else {
                        renameChannel(voiceChannel, mostCommonKey);
                    }
                }
            }
        }
    }

    private static Map<String, Integer> getVoiceChannelActivities(VoiceChannel voiceChannel) {
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
                                            //log.error("Fortnite Rich Presence Details are null.");
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

    public static void renameChannel(AudioChannel audioChannel, String name) {
        log.info("Renamed " + audioChannel.getName() + " VC to " + name);
        audioChannel.getManager().setName(name).queue();
    }

    public static void addChannelAdmin(Member member, Message message) {
        if (!member.getUser().isBot() && !member.getUser().isSystem()) {
            AutoVoice voiceChannel = getAutoVoice(message.getChannel().asVoiceChannel());
            if (voiceChannel != null) {
                if (!voiceChannel.getChannelAdmins().contains(member)) {
                    message.reply("`" + member.getEffectiveName() + "` was made a Channel Admin.").queue();
                } else {
                    message.reply("`" + member.getEffectiveName() + "` is already a Channel Admin.").queue();
                }
            }
        }
    }

    public static void removeChannelAdmin(Member member, Message message) {
        if (!member.getUser().isBot() && !member.getUser().isSystem()) {
            AutoVoice voiceChannel = getAutoVoice(message.getChannel().asVoiceChannel());
            if (voiceChannel != null) {
                if (voiceChannel.getChannelAdmins().contains(member)) {
                    message.reply("`" + member.getEffectiveName() + "` was removed as a Channel Admin.").queue();
                } else {
                    message.reply("`" + member.getEffectiveName() + "` is not a Channel Admin.").queue();
                }
            }
        }
    }

    public static List<Member> getChannelAdmins(VoiceChannel voiceChannel) {
        AutoVoice av = getAutoVoice(voiceChannel);
        if (av == null) return new ArrayList<>();
        return av.getChannelAdmins();
    }

    private static AutoVoice getAutoVoice(AudioChannel audioChannel) {
        for (AutoVoice av : channels) {
            if (av.getVoiceChannel().getIdLong() == audioChannel.getIdLong()) {
                return av;
            }
        }
        return null;
    }

    private static AutoVoice getAutoVoice(VoiceChannel voiceChannel) {
        for (AutoVoice av : channels) {
            if (av.getVoiceChannel().getIdLong() == voiceChannel.getIdLong()) {
                return av;
            }
        }
        return null;
    }

}

class AutoVoice {

    private final VoiceChannel voiceChannel;
    private final Set<Member> channelAdmins = new HashSet<>();

    public AutoVoice(VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void addChannelAdmin(Member member) {
        try {
            channelAdmins.add(member);
            PermissionOverride e = voiceChannel.getPermissionOverride(member);
            e.getManager().grant(VoiceChannelManager.adminAllowedPermissions).queue();
        } catch (NullPointerException e) {
            VoiceChannelManager.log.error("Permission Override was null.");
        }
    }

    public void removeChannelAdmin(Member member) {
        try {
            channelAdmins.remove(member);
            voiceChannel.getPermissionOverride(member).getManager().deny(VoiceChannelManager.adminAllowedPermissions).queue();
        } catch (NullPointerException e) {
            VoiceChannelManager.log.error("Permission Override was null.");
        }
    }

    public List<Member> getChannelAdmins() {
        return channelAdmins.stream().toList();
    }

    public List<Member> getChannelMembers() {
        return voiceChannel.getMembers();
    }

}
