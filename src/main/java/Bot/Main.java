package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public enum Var {
        money(0),
        dailyClaimed(1),
        dailiesClaimed(2),
        membersInvited(3),
        invitedByMember(4),
        gameClaimed(5),
        gameKeys(6);

        public final int index;
        Var(int index) { this.index = index; }

    }

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
            System.exit(1);
            return "";
        }
    }

    public static String readLine(String userID, Var var) {
        try {
            return Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(var.index);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getMoney(String userID) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(0));
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static LocalDate getDailyClaimed(String userID) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(1));
        } catch (IOException e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }

    public static int getDailiesClaimed(String userID) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(2));
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<String> getMembersInvited(String userID) {
        try {
            return List.of(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(3).split(","));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getInvitedByMember(String userID) {
        try {
            return Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(4);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocalDate getGameClaimed(String userID) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(5));
        } catch (IOException e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }

    public static List<String> getGameKeys(String userID) {
        try {
            return List.of(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(6).split(","));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
