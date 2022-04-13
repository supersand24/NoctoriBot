package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

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
        } catch (NoSuchFileException e) {
            System.out.println("Could not find the bot.token file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        return "";
    }

    public static int getMoney(String userID) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(0));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static LocalDate getDailyClaimed(String userID) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(1));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static int getDailiesClaimed(String userID) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(2));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<String> getMembersInvited(String userID) {
        try {
            return List.of(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(3).split(","));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInvitedByMember(String userID) {
        try {
            return Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(4);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LocalDate getGameClaimed(String userID) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(5));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static List<String> getGameKeys(String userID) {
        try {
            return List.of(Files.readAllLines(Paths.get("variables/" + userID + ".var")).get(6).split(","));
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
