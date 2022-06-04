package Bot;

import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;

public class AutoVoice {

    private final VoiceChannel voiceChannel;
    private final List<Member> channelAdmins = new ArrayList<>();

    public AutoVoice(Member member, VoiceChannel voiceChannel) {
        channelAdmins.add(member);
        this.voiceChannel = voiceChannel;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

}
