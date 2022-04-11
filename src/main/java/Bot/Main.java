package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        JDABuilder builder = JDABuilder.create(
                getToken(),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
        ).setMemberCachePolicy(MemberCachePolicy.ALL);

        JDA jda;

        builder.addEventListeners( new Listener() );

        try {
            jda = builder.build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getToken() {
        try {
            return Files.readAllLines(Paths.get("bot.token")).get(0);
        } catch (IOException e) {
            System.out.println("Could not find the bot.token file!");
            //System.exit(1);
            return "";
        }
    }

}
