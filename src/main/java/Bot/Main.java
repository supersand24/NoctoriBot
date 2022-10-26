package Bot;

import Command.Profile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
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
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
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
            getNoctori().upsertCommand("vc","Voice Channel Commands").addSubcommands(
                    new SubcommandData("edit","Edits your current Voice Channel, if you are a Voice Channel Admin.").addOptions(
                            new OptionData(OptionType.STRING,"name","The Channel Name."),
                            new OptionData(OptionType.BOOLEAN, "auto-rename", "If the channel should auto renamed based off of user activities."),
                            new OptionData(OptionType.BOOLEAN, "locked", "If people are allowed to join the channel.")
                    )
            ).queue();

            //Dev Command
            getNoctori().upsertCommand("dev","Development Only!").addSubcommands(
                    new SubcommandData("print","Outputs a report.").addOptions(
                            new OptionData(OptionType.INTEGER,"object","What to print.").setRequired(true)
                                    .addChoice("All Voice Channels",0)
                    )
            ).queue();

        } catch (LoginException | InterruptedException e) {
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
