package Bot;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        System.out.println("Listener is ready.");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        System.out.println("Message Received");
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        System.out.println(e.getMember().getEffectiveName() + " joined voice channel.");
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        System.out.println(e.getMember().getNickname() + " left voice channel.");
    }
}
