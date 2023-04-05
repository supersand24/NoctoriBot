package Bot;

import Command.Profile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);
    private static JDA jda;

    private static Guild noctori;

    public static void main(String[] args) {

        JDABuilder builder = JDABuilder.create(
                getToken(),
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.SCHEDULED_EVENTS
        ).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(
                        CacheFlag.STICKER,
                        CacheFlag.EMOJI
        );

        builder.addEventListeners( new Listener(), new VoiceManager() );

        try {
            jda = builder.build();
            jda.awaitReady();

            //Generic Slash Commands
            getNoctori().upsertCommand("money","Checks your Noctori Bank.").addSubcommands(
                    new SubcommandData("balance","Check how much money is in your account."),
                    new SubcommandData("pay","Pay another user money, from your account.").addOptions(
                            new OptionData(OptionType.USER,"member","Who you would like to pay.",true),
                            new OptionData(OptionType.INTEGER,"amount","The amount you would like to send.",true).setMinValue(1)
                    )
            ).queue();

            //Profile Command
            Profile.init();

            //Voice Channel Command
            getNoctori().upsertCommand("vc", "Voice Channel Commands").addSubcommandGroups(
                    new SubcommandGroupData("music", "Music Related Commands").addSubcommands(
                            new SubcommandData("join", "Make me join the Voice Channel."),
                            new SubcommandData("leave", "Make me leave the Voice Channel."),
                            new SubcommandData("play", "Play the music.").addOptions(
                                    new OptionData(OptionType.STRING, "url", "A link to the music.").setRequired(true)
                            ),
                            new SubcommandData("stop", "Stop the music").addOptions(
                                    new OptionData(OptionType.BOOLEAN, "clear-queue", "If true, clears the queue.")
                            )
                    )
            ).addSubcommands(
                    new SubcommandData("give-key", "Give a key to a member.").addOptions(
                            new OptionData(OptionType.USER, "member", "Who you want to give a key.")
                    ),
                    new SubcommandData("edit","Edits your current Voice Channel, if you are a Voice Channel Admin.").addOptions(
                            new OptionData(OptionType.STRING, "name", "The Channel Name."),
                            new OptionData(OptionType.BOOLEAN, "auto-rename", "If the channel should auto renamed based off of user activities."),
                            new OptionData(OptionType.BOOLEAN, "locked", "If people are allowed to join the channel.")
                    ),
                    new SubcommandData("lock", "Quickly lock the channel."),
                    new SubcommandData("auto-rename", "Quickly toggle auto renaming.")
            ).queue();

            //Dev Command
            getNoctori().upsertCommand("dev","Development Only!").addSubcommands(
                    new SubcommandData("print","Outputs a report.").addOptions(
                            new OptionData(OptionType.INTEGER,"object","What to print.").setRequired(true)
                                    .addChoice("Voice Channels",0)
                                    .addChoice("User Variables",1)
                                    .addChoice("All Unclaimed Users",99)
                    ),
                    new SubcommandData("var","Edits a users variables.").addOptions(
                            new OptionData(OptionType.USER,"user","Who to modify.").setRequired(true),
                            new OptionData(OptionType.INTEGER,"money","Change Money Value").setMinValue(0).setMaxValue(999999),
                            new OptionData(OptionType.BOOLEAN,"notification","Change Notification Value"),
                            new OptionData(OptionType.USER,"invited-by","Change Invited By Value"),
                            new OptionData(OptionType.INTEGER,"genshin-uid","Change Genshin UID Value"),
                            new OptionData(OptionType.STRING,"minecraft-username","Change Minecraft Username Value"),
                            new OptionData(OptionType.STRING,"profile-fields","Change Profile Fields Value")
                    )
            ).queue();

            //Game Specific Commands
            getNoctori().upsertCommand("game-specific","Game Specific Commands").addSubcommandGroups(
                    new SubcommandGroupData("bo3", "Black Ops 3 Zombies").addSubcommands(
                            new SubcommandData("maps", "Sends a link to the maps."),
                            new SubcommandData("leaderboard", "Shows a leaderboard for a map.").addOptions(
                                    new OptionData(OptionType.STRING,"map","Map").setRequired(true)
                            )
                    )
            ).queue();
            //e.getChannel().sendMessage(Manager.getSteamCollectionURL()).queue();
            //case "bo3" -> Manager.sendMapLeaderboard(e.getMessage());


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getToken() {
        try {
            return Files.readAllLines(Paths.get("bot.token")).get(0);
        } catch (NoSuchFileException e) {
            log.error("Could not find the bot.token file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        return "";
    }

    public static Guild getNoctori() {
        if (noctori == null) {
            noctori = jda.getGuilds().get(0);
        }
        return noctori;
    }

    public static TextChannel getLogChannel() {
        return getNoctori().getTextChannelById(444524933415043073L);
    }

}
