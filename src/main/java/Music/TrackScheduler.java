package Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final static Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    private final MusicManager manager;
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player, MusicManager manager) {
        this.manager = manager;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public void nextTrack() {
        if (!this.queue.isEmpty()) {
            AudioTrack audioTrack = this.queue.poll();
            log.info("Now playing " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author);
            this.player.startTrack(audioTrack, false);
        }
        manager.updateJukeboxControlPanel();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

}
