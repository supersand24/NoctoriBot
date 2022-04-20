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
    private static TextChannel textChannel;

    public static void initialize(Guild guild) {
        textChannel = guild.getTextChannelById(textChannelId);
        log.debug("Auto Voice Manager initialized.");
    }

    public static void join(Member member, AudioChannel audioChannel) {
        join(member,convertAudioChannel(audioChannel));
    }
    public static void join(Member member, VoiceChannel voiceChannel) {
        if (voiceChannel.getName().equalsIgnoreCase("New Channel")) {
            newChannel(member, voiceChannel);
        } else {
            AutoVoice av = getAutoVoice(voiceChannel);
            av.getThread().sendMessage(member.getEffectiveName() + " has joined the voice channel.").queue();
        }
    }

    public static void newChannel(Member member, VoiceChannel voiceChannel) {
        textChannel.createThreadChannel("Voice Channel Text").queue(
                threadChannel -> {
                    threadChannel.addThreadMember(member).queue( unused -> {
                        threadChannel.sendMessage(member.getEffectiveName() + " has created the voice channel.").queue();
                    } );
                    channels.add(new AutoVoice(member, voiceChannel, threadChannel));
                    voiceChannel.createCopy().queue();
                    voiceChannel.getManager().setName("Vibing").queue();
                }
        );
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
