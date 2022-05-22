package Bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final static Logger log = LoggerFactory.getLogger(Var.class);

    public static int getMoney(User user) {
        try {
            int money = Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(0));
            log.debug("Read " + user.getName() + " has " + money + " Noctori Bucks.");
            return money;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
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
            log.debug("Set " + user.getName() + " Noctori Bucks to " + money + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addMoney(User user, int money) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(0, String.valueOf(Integer.parseInt(content.get(0)) + money));
            log.debug("Gave " + money + " Noctori Bucks to " + user.getName() + ", new balance is " + content.get(0) + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
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
            log.debug("Took " + money + " Noctori Bucks from " + user.getName() + ", new balance is " + content.get(0) + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDate getDailyClaimed(User user) {
        try {
            LocalDate dailyClaimed = LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(1));
            log.debug("Read " + user.getName() + " was last active on " + dailyClaimed + ".");
            return dailyClaimed;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static int getDailiesClaimed(User user) {
        try {
            int dailiesClaimed = Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(2));
            log.debug("Read " + user.getName() + " has claimed " + dailiesClaimed + " dailies.");
            return dailiesClaimed;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean getNotification(User user) {
        try {
            return Boolean.parseBoolean(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(3));
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setNotification(User user, boolean bool) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(3, String.valueOf(bool));
            log.debug("Set " + user.getName() + " Notification Setting " + bool + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateDailyClaimed(User user) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            LocalDate now = LocalDate.now();
            int dailiesClaimed = Integer.parseInt(content.get(2)) + 1;
            content.set(1, now.toString());
            content.set(2, String.valueOf(dailiesClaimed));
            log.debug("Set " + user.getName() + " daily claimed to " + now + ".");
            log.debug("Set " + user.getName() + " has claimed " + dailiesClaimed + " dailies.");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getMembersInvited(User user) {
        try {
            List<String> membersInvited = parseStringArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(4));
            log.debug("Read " + user.getName() + " invited " + membersInvited + " to the server.");
            return membersInvited;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addMemberInvited(User user, Member member) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> members = parseStringArray(content.get(4));
            members.add(member.getId());
            content.set(4, members.toString() );
            log.debug(user.getName() + " invited " + member.getEffectiveName() + " to the Server.");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getInvitedByMember(User user) {
        try {
            String invitedByMember = Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(5);
            if (invitedByMember.equals("0")) {
                log.debug("Read " + user.getName() + " was invited by no one.");
            } else {
                log.debug("Read " + user.getName() + " was invited by " + invitedByMember);
            }
            return invitedByMember;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setInvitedByMember(User user, Member member) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(5, member.getId());
            log.debug("Set " + user.getName() + " was invited by " + member.getEffectiveName() + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDate getGameClaimed(User user) {
        try {
            LocalDate gameClaimedDate = LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(6));
            log.debug("Read " + user.getName() + " last claimed a game on " + gameClaimedDate + ".");
            return gameClaimedDate;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }

    public static List<String> getGameKeys(User user) {
        try {
            List<String> gameKeys = parseStringArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(7));
            log.debug("Read " + user.getName() + " owns these game keys " + gameKeys);
            return gameKeys;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addGameKey(User user, String game) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> gameKeys = parseStringArray(content.get(7));
            gameKeys.add(game);
            LocalDate now = LocalDate.now();
            content.set(6, now.toString() );
            content.set(7, gameKeys.toString() );
            log.debug("Set " + user.getName() + " last claimed game date to " + now + ".");
            log.debug("Added " + game + " to " + user.getName() + "'s game collection.");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
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
