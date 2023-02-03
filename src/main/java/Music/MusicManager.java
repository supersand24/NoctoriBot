package Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//For each guild
//TODO Change this to a general Guild Manager to keep track of Voice Channels too.
public class MusicManager extends AudioEventAdapter {

    private final static Logger log = LoggerFactory.getLogger(MusicManager.class);

    public final AudioPlayer audioPlayer;
    private final AudioPlayerSendHandler sendHandler;
    public final BlockingQueue<AudioTrack> queue;

    public Message currentJukeboxControlPanel;

    public MusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.audioPlayer.addListener(this);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.queue = new LinkedBlockingQueue<>();
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
        string.append("```Jukebox Controls and Queue```");
        if (track == null) string.append(":stop_button: *Nothing currently playing.*\n"); else {
            if (audioPlayer.isPaused()) string.append(":pause_button: "); else string.append(":arrow_forward: ");
            string.append("__**").append(track.getInfo().title).append("**__ by *").append(track.getInfo().author).append("*\n");
        }
        final List<AudioTrack> trackList = new ArrayList<>(queue);
        final int trackCount = Math.min(trackList.size(), 9);
        for (int i = 0; i < trackCount; i++)
            string.append(getEmojiFromInt(i + 1)).append(" **").append(trackList.get(i).getInfo().title).append("** by *").append(trackList.get(i).getInfo().author).append("*\n");
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

    public void queue(AudioTrack track) {
        if (!this.audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public void nextTrack() {
        if (!this.queue.isEmpty()) {
            AudioTrack audioTrack = this.queue.poll();
            log.info("Now playing " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author);
            this.audioPlayer.startTrack(audioTrack, false);
        }
        updateJukeboxControlPanel();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

}