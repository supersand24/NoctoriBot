package Bot;

import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VoiceChannelManager {

    private final static Logger log = LoggerFactory.getLogger(VoiceChannelManager.class);
    public final static List<AutoVoice> channels = new ArrayList<>();

    public static void join(Member member, AudioChannel audioChannel) {
        if (audioChannel.getName().equalsIgnoreCase("New Channel")) {
            newChannel(member, (StageChannel) audioChannel);
        } else {
            log.info(member.getEffectiveName() + " joined " + audioChannel.getName() + ".");
            updateChannelName(audioChannel);
        }
    }

    public static void leave(Member member, AudioChannel audioChannel) {
        if (!audioChannel.getName().equals("New Channel")) {
            VoiceChannel voiceChannel = (VoiceChannel) audioChannel;
            if (voiceChannel.getParentCategory().getName().equals("Auto Voice (WIP)")) {
                log.info(member.getEffectiveName() + " left " + audioChannel.getName() + ".");
                //Delete Channel if no one is left.
                if (voiceChannel.getMembers().size() <= 0) {
                    channels.remove(getAutoVoice(voiceChannel));
                    voiceChannel.delete().queue();
                } else {
                    if (audioChannel.getType() == ChannelType.VOICE) updateChannelName(audioChannel);
                }
            }
        }
    }

    public static void newChannel(Member member, StageChannel stageChannel) {
        stageChannel.getParentCategory().createVoiceChannel("Vibing").setPosition(0).queue(voiceChannel -> {
            log.info(member.getEffectiveName() + " created a New Voice Channel.");
            channels.add( new AutoVoice(member, voiceChannel) );
            Main.getNoctori().moveVoiceMember(member,voiceChannel).queue();
        });
    }

    public static void updateChannelName(AudioChannel audioChannel) {
        if (!audioChannel.getName().equals("New Channel")) {
            if (audioChannel.getType() == ChannelType.VOICE) {
                VoiceChannel voiceChannel = (VoiceChannel) audioChannel;
                if (voiceChannel.getParentCategory().getName().equals("Auto Voice (WIP)")) {
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
                                            log.error("Fortnite Rich Presence Details are null.");
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

    private static AutoVoice getAutoVoice(VoiceChannel voiceChannel) {
        for (AutoVoice av : channels) {
            if (av.getVoiceChannel().getIdLong() == voiceChannel.getIdLong()) {
                return av;
            }
        }
        return null;
    }

}
