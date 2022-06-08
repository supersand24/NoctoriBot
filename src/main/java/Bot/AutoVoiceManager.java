package Bot;

import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoVoiceManager {

    private final static Logger log = LoggerFactory.getLogger(AutoVoiceManager.class);

    public final static List<AutoVoice> channels = new ArrayList<>();
    private final static long textChannelId = 964666264867967008L;

    public static void initialize(Guild guild) {
        log.debug("Auto Voice Manager initialized.");
    }

    public static void join(Member member, AudioChannel audioChannel) {
        join(member,convertAudioChannel(audioChannel));
    }
    public static void join(Member member, VoiceChannel voiceChannel) {
        if (voiceChannel.getName().equalsIgnoreCase("New Channel")) {
            newChannel(member, voiceChannel);
        }
    }

    public static void newChannel(Member member, VoiceChannel voiceChannel) {
        voiceChannel.createCopy().queue();
        renameChannel(voiceChannel,"Vibing");
        log.info("New Channel Created.");
    }

    public static void updateChannelName(AudioChannel audioChannel) {
        updateChannelName(convertAudioChannel(audioChannel));
    }

    public static void updateChannelName(VoiceChannel voiceChannel) {
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
                                        switch (activity.asRichPresence().getDetails()) {
                                            case "Battle Royale - In Lobby" -> hashMap.merge("Fortnite: Battle Royale", 1, Integer::sum);
                                            case "Save The World" -> hashMap.merge("Fortnite: Save the World", 1, Integer::sum);
                                            default -> {
                                                log.info("Unchecked Fortnite Rich Presence Case : " + activity.asRichPresence().getDetails());
                                                hashMap.merge(activity.getName(), 1, Integer::sum);
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

    public static void renameChannel(VoiceChannel voiceChannel, String name) {
        log.info("Renamed " + voiceChannel.getName() + " VC to " + name);
        voiceChannel.getManager().setName(name).queue();
    }

    public static void leave(Member member, AudioChannel audioChannel) {
        leave(member,convertAudioChannel(audioChannel));
    }

    public static void leave(Member member, VoiceChannel voiceChannel) {
        if (voiceChannel.getParentCategory().getName().equals("Auto Voice (WIP)")) {
            if (voiceChannel.getMembers().size() <= 0) {
                voiceChannel.delete().queue();
            }
        }
    }

    private static AutoVoice getAutoVoice(VoiceChannel voiceChannel) {
        for (AutoVoice av : channels) {
            if (av.getVoiceChannel().getIdLong() == voiceChannel.getIdLong()) {
                return av;
            }
        }
        return null;
    }

    private static VoiceChannel convertAudioChannel(AudioChannel audioChannel) {
        return Main.getNoctori().getVoiceChannelById(audioChannel.getId());
    }
}
