package Bot;

import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
        voiceChannel.getManager().setName("Voice Channel").queue();
        log.info("New Channel Created.");
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
        return audioChannel.getGuild().getVoiceChannelById( audioChannel.getId() );
    }
}
