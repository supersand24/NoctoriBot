package Bot;

import net.dv8tion.jda.api.entities.Member;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void setMoney(String userID, int money) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(0, String.valueOf(money));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addMoney(String userID, int money) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(0, String.valueOf(
                    Integer.parseInt(content.get(0)) + money
            ));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeMoney(String userID, int money) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            int wallet = Integer.parseInt(content.get(0));
            if (wallet > money) {
                content.set(0, String.valueOf(wallet - money));
            }
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void updateDailyClaimed(String userID) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(1, LocalDate.now().toString());
            content.set(2, String.valueOf(Integer.parseInt(content.get(2)) + 1));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void addMemberInvited(String userID, Member member) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> members = parseStringArray(content.get(3));
            members.add(member.getId());
            content.set(3, members.toString() );
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void setInvitedByMember(String userID, Member member) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(4, member.getId());
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void addGameKey(String userID, String game) {
        try {
            Path filePath = Paths.get("variables/" + userID + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> gameKeys = parseStringArray(content.get(6));
            gameKeys.add(game);
            content.set(5, LocalDate.now().toString() );
            content.set(6, gameKeys.toString() );
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(userID + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> parseStringArray(String array) {
        String[] strings;
        if (array.substring(1, array.length() - 1).isEmpty()) {
            strings = new String[0];
        } else {
            strings = array.substring(1, array.length() - 1).split(", ");
        }
        return new ArrayList<>(Arrays.asList(strings));
    }

}
