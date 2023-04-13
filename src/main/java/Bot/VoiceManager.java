package Bot;

import Music.MusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class VoiceManager extends ListenerAdapter {

    protected static final Logger log = LoggerFactory.getLogger(VoiceManager.class);

    protected final static Hashtable<Long,NoctoriVoiceChannel> channels = new Hashtable<>();

    private long AUTO_VOICE_NEW_CHANNEL_ID = 984491055636443216L;
    private long AUTO_VOICE_CATEGORY_ID = 964543894832427018L;

    public final static Map<Long, MusicManager> musicManagers = new HashMap<>();
    private final static AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    protected final static EnumSet<Permission> adminAllowedPermissions = EnumSet.of(
            Permission.MANAGE_CHANNEL,      Permission.CREATE_INSTANT_INVITE,
            Permission.VOICE_DEAF_OTHERS,   Permission.VOICE_MUTE_OTHERS,
            Permission.PRIORITY_SPEAKER
    );

    protected final static EnumSet<Permission> memberAllowedPermissions = EnumSet.of(
            Permission.VOICE_SPEAK,         Permission.MESSAGE_TTS
    );

    protected final static EnumSet<Permission> joinChannelPermissions = EnumSet.of(
            Permission.MESSAGE_SEND
    );

    protected final static EnumSet<Permission> lockChannelPermissions = EnumSet.of(
            Permission.VOICE_CONNECT
    );

    protected final static EnumSet<Permission> newChannelPermissions = EnumSet.of(
            Permission.VIEW_CHANNEL,        Permission.VOICE_SPEAK,
            Permission.VOICE_STREAM,        Permission.MESSAGE_TTS,
            Permission.VOICE_START_ACTIVITIES
    );

    @Override
    public void onReady(ReadyEvent e) {
        log.info("Auto Voice Manager Setting Up...");
        //Locate the New Channel, and update the voice channel id.
        for (StageChannel stageChannel : Main.getNoctori().getStageChannels())
            if (stageChannel.getName().equals("New Channel")) AUTO_VOICE_NEW_CHANNEL_ID = stageChannel.getIdLong();
        //Check all voice channels
        for (VoiceChannel vc : Main.getNoctori().getVoiceChannels()) {
            //Make sure the channel isn't the AFK Channel.
            if (vc.getIdLong() != Main.getNoctori().getAfkChannel().getIdLong()) {
                //Delete Abandoned Channels
                if (vc.getMembers().size() <= 0) {
                    vc.delete().queue();
                    break;
                }

                //Needs reworking.
                NoctoriVoiceChannel av = new NoctoriVoiceChannel(vc);
                for (Member member : vc.getMembers()) {
                    PermissionOverride perm = vc.getPermissionOverride(member);
                    if (perm != null) {
                        if (perm.getAllowed().contains(Permission.MANAGE_CHANNEL)) {
                            av.addChannelAdmin(member);
                            log.info(member.getEffectiveName() + " was made a Channel Admin.");
                        }
                    }
                }

                //Default on the creator.
                //if (av.creator == null) av.getChannelAdmins().get(0);

                //Check if channel is locked.
                if (vc.getPermissionOverride(Main.getNoctori().getPublicRole()) != null) {
                    av.setLocked(vc.getPermissionOverride(Main.getNoctori().getPublicRole()).getDenied().contains(Permission.VOICE_CONNECT));
                }

                //Finally, add to master list.
                channels.put(vc.getIdLong(), av);

                //Print the results to the console.
                System.out.println(av);

            }
        }

        //Final Print
        System.out.println("--------------------------------------");
        log.info("Auto Voice Manager Ready!");

        //Music Setup
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        log.info("Music Manager Ready!");
    }

    public static MusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final MusicManager guildManager = new MusicManager(audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildManager.getSendHandler());
            return guildManager;
        });
    }

    public static boolean isUrl(String string) {
        try {
            new URI(string);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static void addToQueue(String trackURL, SlashCommandInteractionEvent e) {
        final MusicManager musicManager = getMusicManager(e.getGuild());

        audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.queue(audioTrack);

                log.info("Loaded " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " in " + e.getGuild().getName() + ".");

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(audioTrack.getInfo().title);
                embed.setAuthor(audioTrack.getInfo().author);

                e.replyEmbeds(embed.build()).setEphemeral(true).queue();

                musicManager.updateJukeboxControlPanel();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();

                log.info("Loaded a playlist in " + e.getGuild().getName() + ".");

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(audioPlaylist.getName() + " loaded " + tracks.size() + " songs.");
                final int trackCount = Math.min(tracks.size(), 22);
                for (int i = 0; i < trackCount; i++) embed.addField(tracks.get(i).getInfo().title,tracks.get(i).getInfo().author,true);
                for (AudioTrack track : tracks) musicManager.queue(track);

                e.replyEmbeds(embed.build()).setEphemeral(true).queue();

                musicManager.updateJukeboxControlPanel();
            }

            @Override
            public void noMatches() { log.error("No Matches."); }

            @Override
            public void loadFailed(FriendlyException e) { log.error("Load Failed."); }
        });
    }

    public static void addToQueue(String trackURL, Guild guild) {
        final MusicManager musicManager = getMusicManager(guild);

        audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.queue(audioTrack);
                log.info("Loaded " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " in " + guild.getName() + ".");

                musicManager.updateJukeboxControlPanel();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                log.info("Loaded a playlist in " + guild.getName() + ".");
                for (AudioTrack track : tracks) musicManager.queue(track);

                musicManager.updateJukeboxControlPanel();
            }

            @Override
            public void noMatches() { log.error("No Matches."); }

            @Override
            public void loadFailed(FriendlyException e) { log.error("Load Failed."); }
        });
    }

    public static String skipTrack(Guild guild) {
        MusicManager manager = getMusicManager(guild);
        if (manager.audioPlayer.getPlayingTrack() == null) { return "Nothing is currently playing."; }
        manager.nextTrack();
        return "Skipping song...";
    }

    public static String stopAndClear(Guild guild, boolean clearQueue) {
        MusicManager manager = getMusicManager(guild);
        manager.audioPlayer.stopTrack();
        log.info("The current song was stopped.");
        if (clearQueue) { manager.queue.clear(); log.info("The queue for " + guild.getName() + " was cleared."); }
        manager.updateJukeboxControlPanel();
        return "The music was stopped.";
    }

    public static String pauseMusic(Guild guild) {
        MusicManager manager = getMusicManager(guild);
        manager.audioPlayer.setPaused(!manager.audioPlayer.isPaused());
        manager.updateJukeboxControlPanel();
        if (manager.audioPlayer.isPaused()) {
            log.info("Music in " + guild.getName() + " was paused.");
            return "The music was paused.";
        } else {
            log.info("Music in " + guild.getName() + " was unpaused.");
            return "The music plays on.";
        }
    }

    public static void botJoinVoice(VoiceChannel channel) {
        AudioManager audioManager = channel.getGuild().getAudioManager();
        audioManager.openAudioConnection(channel);
        MusicManager musicManager = getMusicManager(channel.getGuild());
        channel.sendMessage("Jukebox Controls").addActionRow(
                Button.primary("music-pause", "Pause/Unpause"),
                Button.secondary("music-skip", "Skip Song")
        ).addActionRow(
                Button.primary("music-addToQueue","Add to Queue"),
                Button.danger("music-stop","Stop Music")
        ).queue(musicManager::setCurrentJukeboxControlPanel);
    }

    public static void botLeaveVoice(Guild guild) {
        MusicManager musicManager = getMusicManager(guild);
        AudioManager audioManager = guild.getAudioManager();
        musicManager.currentJukeboxControlPanel.delete().queue(unused -> audioManager.closeAudioConnection());
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        log.info("Auto Voice Manager Restarting...");
        log.info("Auto Voice Manager Ready!");
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        Member member = e.getMember();
        AudioChannel channelJoined = e.getChannelJoined();
        AudioChannel channelLeft = e.getChannelLeft();

        Bank.daily(member);

        //Get the channel joined.
        if (channelJoined != null) {
            log.info(member.getEffectiveName() + " joined " + channelJoined.getName() + ".");
            switch (channelJoined.getType()) {
                case STAGE -> newChannel(member);
                case VOICE -> {
                    if (isAfkChannel(channelJoined) && channelLeft != null) {
                        NoctoriVoiceChannel vc = channels.get(channelJoined.getIdLong());
                        if (vc != null) {
                            vc.sendMessage("`" + member.getEffectiveName() + "` went AFK.").setTTS(true).queue();

                        }
                    } else {
                        NoctoriVoiceChannel vc = channels.get(channelJoined.getIdLong());
                        if (vc != null) {
                            vc.sendMessage("`" + member.getEffectiveName() + "` joined the call.").setTTS(true).queue();
                            vc.getVoiceChannel().upsertPermissionOverride(member).grant(joinChannelPermissions).queue();
                        }
                    }
                }
            }
        }

        if (    channelLeft != null
             && channelLeft.getType() == ChannelType.VOICE
             && !isAfkChannel(channelLeft)
        ) {
            NoctoriVoiceChannel vc = channels.get(channelLeft.getIdLong());
            log.info(member.getEffectiveName() + " left " + channelLeft.getName() + ".");
            vc.getVoiceChannel().sendMessage("`" + member.getEffectiveName() + "` left the call.").setTTS(true).queue(message -> {
                if (channelLeft.getMembers().size() <= 0) {
                    channels.remove(channelLeft.getIdLong());
                    vc.delete();
                } else {
                    vc.removeChannelAdmin(member);
                    vc.getVoiceChannel().upsertPermissionOverride(member).clear(joinChannelPermissions).clear(adminAllowedPermissions).queue();
                }
            });
        }
    }

    private void newChannel(Member member) {
        if (!member.getVoiceState().inAudioChannel()) return;

        StageChannel stageChannel = member.getVoiceState().getChannel().asStageChannel();
        if (stageChannel.getIdLong() != AUTO_VOICE_NEW_CHANNEL_ID) return;

        stageChannel.getParentCategory().createVoiceChannel("Vibing").setPosition(0).setNSFW(false).queue(voiceChannel -> {
            NoctoriVoiceChannel vc = new NoctoriVoiceChannel(voiceChannel, member);
            channels.put(voiceChannel.getIdLong(), vc);
            Main.getNoctori().moveVoiceMember(member, voiceChannel).queue();
            voiceChannel.upsertPermissionOverride(Main.getNoctori().getPublicRole()).grant(newChannelPermissions).queue();
            voiceChannel.upsertPermissionOverride(member).grant(adminAllowedPermissions).grant(joinChannelPermissions).queue();
            log.info(member.getEffectiveName() + " created a New Voice Channel.");
            voiceChannel.sendMessageEmbeds(vc.toEmbed()).addActionRow(
                    Button.primary("vc-lock", "Lock"),
                    Button.secondary("vc-rename", "Rename"),
                    Button.secondary("vc-autoRename", "Auto Rename")
            ).addActionRow(
                    Button.primary("vc-addMusic", "Add Music"),
                    Button.success("vc-addAdmin", "Add Admin")
            ).queue(vc::setControlPanel);
        });

    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent e) {
        GuildVoiceState voiceState = e.getMember().getVoiceState();
        if (!voiceState.inAudioChannel()) { return; }
        NoctoriVoiceChannel vc = getVoiceChannel(voiceState.getChannel().getIdLong());
        if (vc == null) { log.error(e.getMember().getEffectiveName() + " isn't in a channel that can be renamed. "); return; }
        autoRename(vc);
    }

    @Override
    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent e) {
        if (e.getChannelType() == ChannelType.VOICE) {
            //Make sure it is an auto voice channel.
            if (!isAfkChannel(e.getChannel().asVoiceChannel())) {
                NoctoriVoiceChannel voiceChannel = channels.get(e.getChannel().getIdLong());
                voiceChannel.sendMessage("Channel was renamed to `" + e.getNewValue() + "`.").queue();
                voiceChannel.updateLastRenamed();
                //TODO This was causing the AutoRename to turn itself off, after one rename.
                //voiceChannel.setAutoRename(false);
            }
        }
    }

    private boolean isAfkChannel(AudioChannel audioChannel) {
        if (Main.getNoctori().getAfkChannel() == null) return false;
        return audioChannel.getIdLong() == Main.getNoctori().getAfkChannel().getIdLong();
    }

    private void autoRename(NoctoriVoiceChannel voiceChannel) {
        //If Auto Rename is turned on.
        if (voiceChannel.getAutoRename()) {
            //If Enough Time has passed.
            log.info("Attempting to rename " + voiceChannel.getName() + ", " + voiceChannel.getTimeSinceRename() + " mins have passed since last rename.");
            if (voiceChannel.getTimeSinceRename() >= 10) {

                Map<String, Integer> hashMap = getVoiceChannelActivities(voiceChannel);
                log.info(voiceChannel.getName() + " current activities : " + hashMap);

                String mostCommonKey = "Vibing";
                int maxValue = 1;
                for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                    if (entry.getValue() > maxValue) {
                        mostCommonKey = entry.getKey();
                        maxValue = entry.getValue();
                    }
                }

                if (!mostCommonKey.equals(voiceChannel.getName())) {
                    voiceChannel.sendMessage("Channel was automatically renamed to `" + mostCommonKey + "`.").queue();
                    voiceChannel.getVoiceChannel().getManager().setName(mostCommonKey).queue();
                    voiceChannel.updateLastRenamed();
                    log.info(voiceChannel.getVoiceChannel().getName() + " was automatically renamed to " + mostCommonKey + ".");
                }

            } else {
                log.trace("Not enough time passed, will not rename " + voiceChannel.getName() + " channel.");
            }
        }
    }

    public static Collection<NoctoriVoiceChannel> getNoctoriVoiceChannels() {
        return channels.values();
    }

    protected static Map<String, Integer> getVoiceChannelActivities(NoctoriVoiceChannel voiceChannel) {
        List<Member> members = voiceChannel.getMembers();
        Map<String, Integer> hashMap = new HashMap<>();
        for (Member member : members) {
            if (!member.getUser().isBot()) {
                for (Activity activity : member.getActivities()) {
                    switch (activity.getType()) {
                        case PLAYING -> {
                            switch (activity.getName()) {
                                case "Fortnite" -> {
                                    if (activity.isRich()) {
                                        String details = activity.asRichPresence().getDetails();
                                        if (details == null) {
                                            hashMap.merge(activity.getName(), 1, Integer::sum);
                                        } else {
                                            switch (details.split(" ")[0]) {
                                                case "Battle" -> hashMap.merge("Fortnite: Battle Royale", 1, Integer::sum);
                                                case "Save" -> hashMap.merge("Fortnite: Save the World", 1, Integer::sum);
                                                default -> {
                                                    log.error("Unchecked Fortnite Rich Presence Case : " + activity.asRichPresence().getDetails());
                                                    hashMap.merge(activity.getName(), 1, Integer::sum);
                                                }
                                            }
                                        }
                                    }
                                }
                                case "tModLoader" -> hashMap.merge("Terraria", 1, Integer::sum);
                                default -> hashMap.merge(activity.getName(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        return hashMap;
    }

    public static NoctoriVoiceChannel getVoiceChannel(long channelId) {
        return channels.get(channelId);
    }

    public static void toggleChannelLock(long channelId, Member commandAuthor) {
        NoctoriVoiceChannel vc = getVoiceChannel(channelId);
        if (vc == null) return;
        if (vc.channelAdmins.contains(commandAuthor)) {
            vc.setLocked(!vc.isLocked());
        }
    }

    public static void giveChannelKey(long channelId, Member commandAuthor, Member member) {
        NoctoriVoiceChannel vc = getVoiceChannel(channelId);
        if (vc == null) return;
        if (vc.channelAdmins.contains(commandAuthor)) {
            vc.getVoiceChannel().upsertPermissionOverride(member).grant(lockChannelPermissions).queue();
        }
    }

    public static void sendHelpEmbed(VoiceChannel voiceChannel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Voice Commands Help");
        embed.setDescription("Various Voice Channel Commands.");

        embed.addField("n!help", "Shows this message box.", true);
        embed.addField("n!settings", "Shows the current Voice Channel Settings.", true);
        embed.addField("n!toggleLock", "Unlock/Locks the voice channel.", true);

        voiceChannel.sendMessageEmbeds(embed.build()).queue();
    }
}

class NoctoriVoiceChannel {

    final VoiceChannel voiceChannel;
    Member creator;
    Message controlPanel;

    final List<Member> channelAdmins = new ArrayList<>();

    boolean autoRename = true;
    private LocalDateTime lastRenamed = LocalDateTime.now();

    private boolean locked = false;

    //Constructors

    @Deprecated
    public NoctoriVoiceChannel(VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
    }

    public NoctoriVoiceChannel(VoiceChannel voiceChannel, Member member) {
        this.voiceChannel = voiceChannel;
        this.creator = member;
        channelAdmins.add(member);
    }

    //Getters and Setters

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void addChannelAdmin(Member member) {
        channelAdmins.add(member);
        updateControlPanel();
    }

    public void removeChannelAdmin(Member member) {
        channelAdmins.remove(member);
        updateControlPanel();
    }

    public List<Member> getChannelAdmins() {
        return channelAdmins;
    }

    public List<Member> getMembers() {
        return getVoiceChannel().getMembers();
    }

    protected void setAutoRename(boolean autoRename) {
        if (autoRename != this.autoRename) {
            this.autoRename = autoRename;
            if (autoRename) {
                sendMessage("This channel will now `auto rename`.").queue();
            } else {
                sendMessage("This channel will now `not auto rename`.").queue();
            }
        }
    }

    public boolean getAutoRename() {
        return autoRename;
    }

    public long getTimeSinceRename() {
        return Duration.between(lastRenamed,LocalDateTime.now()).toMinutes();
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        if (locked != this.locked) {
            this.locked = locked;
            if (locked) {
                getVoiceChannel().upsertPermissionOverride(Main.getNoctori().getPublicRole()).deny(VoiceManager.lockChannelPermissions).queue(permissionOverride ->
                        sendMessage("This channel is now `locked`.").queue()
                );
            } else {
                getVoiceChannel().upsertPermissionOverride(Main.getNoctori().getPublicRole()).grant(VoiceManager.lockChannelPermissions).queue(permissionOverride ->
                        sendMessage("This channel is now `unlocked`.").queue()
                );
            }
        }
    }

    //Ease of Access Commands

    public void setName(String name) {
        voiceChannel.getManager().setName(name).queue();
    }

    protected String getName() {
        return getVoiceChannel().getName();
    }

    protected void updateLastRenamed() {
        lastRenamed = LocalDateTime.now();
    }

    protected MessageCreateAction sendMessage(String message) {
        return getVoiceChannel().sendMessage(message);
    }

    protected MessageCreateAction sendMessageEmbeds(MessageEmbed embed) {
        return getVoiceChannel().sendMessageEmbeds(embed);
    }

    protected void setControlPanel(Message message) {
        this.controlPanel = message;
    }

    protected void updateControlPanel() {
        if (controlPanel == null) {
            sendMessageEmbeds(toEmbed()).queue(this::setControlPanel);
            return;
        }
        controlPanel.editMessageEmbeds(toEmbed()).queue();
    }

    protected void delete() {
        getVoiceChannel().delete().queue();
    }

    public void sendSettingsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(getName());
        embed.setDescription("Voice Channel Settings");
        embed.addField("Auto Rename", String.valueOf(getAutoRename()), true);
        embed.addField("Locked", String.valueOf(isLocked()), true);
        embed.addField("", "", true);
        embed.addField("Channel Admins", getChannelAdmins().stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")), false);
        Map<String,Integer> activities = VoiceManager.getVoiceChannelActivities(this);
        if (!activities.isEmpty())
            embed.addField("Channel Activities", String.join("\n", activities.keySet()), false);
        voiceChannel.sendMessageEmbeds(embed.build()).queue();
    }

    protected MessageEmbed toEmbed() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(getName());
        embed.setDescription("Voice Channel Controls");

        if (autoRename) { embed.addField("Auto Rename", "On", true); } else { embed.addField("Auto Rename", "Off", true); }
        if (locked) { embed.addField("Locked", "Yes", true); } else { embed.addField("Locked", "No", true); }
        embed.addField("", "", true);

        embed.addField("Channel Admins", getChannelAdmins().stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")), false);

        return embed.build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------\n");
        sb.append(voiceChannel.getName()).append(" | ").append(voiceChannel.getIdLong()).append("\n");
        sb.append("--------------------------------------\n");
        if (creator == null) {
            sb.append("Creator : None  | Admins : ").append(channelAdmins.stream().map(Member::getEffectiveName).toList()).append("\n");
        } else {
            sb.append("Creator : ").append(creator.getEffectiveName()).append(" | Admins : ").append(channelAdmins.stream().map(Member::getEffectiveName).toList()).append("\n");
        }
        sb.append("Auto Rename = ").append(autoRename).append(" | Locked = ").append(locked).append("\n");
        sb.append("Last Renamed = ").append(lastRenamed.toLocalTime()).append("\n");
        sb.append("Voice Channel Members");
        for (Member member : voiceChannel.getMembers()) {
            sb.append("\n   ").append(member.getEffectiveName()).append(" -> ").append(member.getActivities().stream().map(Activity::getName).collect(Collectors.toList()));
        }
        return sb.toString();
    }
}
