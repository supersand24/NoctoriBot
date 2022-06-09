package Bot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AutoVoice {

    private final VoiceChannel voiceChannel;
    private final List<Member> channelAdmins = new ArrayList<>();

    public AutoVoice(Member member, VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
        addMemberAsChannelAdmin(member);
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void addMemberAsChannelAdmin(Member member) {
        channelAdmins.add(member);
        Collection<Permission> allowed = new ArrayList<>();
        allowed.add(Permission.KICK_MEMBERS);
        allowed.add(Permission.MANAGE_CHANNEL);
        allowed.add(Permission.MANAGE_PERMISSIONS);
        allowed.add(Permission.CREATE_INSTANT_INVITE);
        allowed.add(Permission.VOICE_DEAF_OTHERS);
        allowed.add(Permission.VOICE_MUTE_OTHERS);
        allowed.add(Permission.MESSAGE_MENTION_EVERYONE);
        allowed.add(Permission.MESSAGE_MENTION_EVERYONE);
        getVoiceChannel().getManager().putMemberPermissionOverride(member.getIdLong(),allowed, new ArrayList<>()).queue();
    }

    public void removeMemberAsChannelAdmin(Member member) {
        System.out.println(channelAdmins);
        channelAdmins.remove(member);
        getVoiceChannel().getPermissionOverride(member).getManager().resetAllow().queue();
    }

}
