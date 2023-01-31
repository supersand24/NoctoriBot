package Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

//For each guild
//TODO Change this to a general Guild Manager to keep track of Voice Channels too.
public class MusicManager {

    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public Message currentJukeboxControlPanel;

    public MusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer,this);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    public void setCurrentJukeboxControlPanel(Message message) {
        currentJukeboxControlPanel = message;
        updateJukeboxControlPanel();
    }

    public void updateJukeboxControlPanel() {
        AudioTrack track = audioPlayer.getPlayingTrack();
        StringBuilder string = new StringBuilder();
        string.append("`Jukebox Controls and Queue`\n");
        if (track == null) {
            string.append(":stop_button: *Nothing currently playing.*\n");
        } else {
            string.append(":arrow_forward: **").append(track.getInfo().title).append("** by ").append(track.getInfo().author).append("\n");
        }
        final List<AudioTrack> trackList = new ArrayList<>(scheduler.queue);
        final int trackCount = Math.min(trackList.size(), 9);
        for (int i = 0; i < trackCount; i++) {
            string.append(getEmojiFromInt(i + 1)).append(" **").append(trackList.get(i).getInfo().title).append("** by *").append(trackList.get(i).getInfo().author).append("*\n");
        }
        if (trackList.size() > trackCount)
            string.append(":arrow_down: ").append("And **").append(trackList.size() - trackCount).append("** more...");
        currentJukeboxControlPanel.editMessage(string.toString()).queue();
    }

    private String getEmojiFromInt(int number) {
        switch (number) {
            case 0 -> { return ":zero:"; }
            case 1 -> { return ":one:"; }
            case 2 -> { return ":two:"; }
            case 3 -> { return ":three:"; }
            case 4 -> { return ":four:"; }
            case 5 -> { return ":five:"; }
            case 6 -> { return ":six:"; }
            case 7 -> { return ":seven:"; }
            case 8 -> { return ":eight:"; }
            case 9 -> { return ":nine:"; }
            default -> { return ""; }
        }
    }

}