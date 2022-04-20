package Bot;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class AutoVoice {

    private final ThreadChannel thread;
    private final VoiceChannel voiceChannel;
    private final List<Member> channelAdmins = new ArrayList<>();

    public AutoVoice(Member member, VoiceChannel voiceChannel, ThreadChannel thread) {
        channelAdmins.add(member);
        this.voiceChannel = voiceChannel;
        this.thread = thread;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public ThreadChannel getThread() {
        return thread;
    }
}
