package Bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class Var {

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
