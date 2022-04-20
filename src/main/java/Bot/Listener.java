package Bot;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Listener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(Listener.class);
    private final long GUILD_ID = 444523714420408322L;

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        log.info("Listener is ready!");
        AutoVoiceManager.initialize(
                Objects.requireNonNull(
                        e.getJDA().getGuildById(GUILD_ID)
                )
        );
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        log.info("Message Received");
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        AudioChannel audioChannel = e.getChannelJoined();
        Member member = e.getMember();
        log.info(member.getEffectiveName() + " joined " + audioChannel.getName() + ".");
        AutoVoiceManager.join(member, audioChannel);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        log.info(e.getMember().getEffectiveName() + " left " + e.getChannelLeft().getName() + ".");
    }
}
