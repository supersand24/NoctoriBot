package Bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

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

    public static int getMoney(User user) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(0));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setMoney(User user, int money) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(0, String.valueOf(money));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addMoney(User user, int money) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(0, String.valueOf(
                    Integer.parseInt(content.get(0)) + money
            ));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeMoney(User user, int money) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            int wallet = Integer.parseInt(content.get(0));
            if (wallet > money) {
                content.set(0, String.valueOf(wallet - money));
            }
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDate getDailyClaimed(User user) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(1));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static int getDailiesClaimed(User user) {
        try {
            return Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(2));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void updateDailyClaimed(User user) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(1, LocalDate.now().toString());
            content.set(2, String.valueOf(Integer.parseInt(content.get(2)) + 1));
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getMembersInvited(User user) {
        try {
            return parseStringArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(3));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addMemberInvited(User user, Member member) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> members = parseStringArray(content.get(3));
            members.add(member.getId());
            content.set(3, members.toString() );
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getInvitedByMember(User user) {
        try {
            return Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(4);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setInvitedByMember(User user, Member member) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(4, member.getId());
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDate getGameClaimed(User user) {
        try {
            return LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(5));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static List<String> getGameKeys(User user) {
        try {
            return List.of(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(6).split(","));
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addGameKey(User user, String game) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> gameKeys = parseStringArray(content.get(6));
            gameKeys.add(game);
            content.set(5, LocalDate.now().toString() );
            content.set(6, gameKeys.toString() );
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            System.out.println(user.getId() + ".var file not found!");
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
