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
import java.util.Scanner;
import java.util.stream.Collectors;

public class Var {

    private final static Logger log = LoggerFactory.getLogger(Var.class);

    private final static int VAR_MONEY = 0;
    private final static int VAR_DAILY_CLAIMED_DATE = 1;
    private final static int VAR_DAILIES_CLAIMED = 2;
    private final static int VAR_NOTIFICATION = 3;
    private final static int VAR_MEMBERS_INVITED = 4;
    private final static int VAR_INVITED_BY_MEMBER = 5;
    private final static int VAR_GAME_CLAIMED_DATE = 6;
    private final static int VAR_GAME_KEYS = 7;
    private final static int VAR_GENSHIN_UID = 8;
    private final static int VAR_MINECRAFT_USERNAME = 9;
    private final static int VAR_PROFILE_FIELDS = 10;

    public static int getMoney(User user) {
        try {
            int money = Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_MONEY));
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
            content.set(VAR_MONEY, String.valueOf(money));
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
            content.set(VAR_MONEY, String.valueOf(Integer.parseInt(content.get(VAR_MONEY)) + money));
            log.debug("Gave " + money + " Noctori Bucks to " + user.getName() + ", new balance is " + content.get(VAR_MONEY) + ".");
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
            int wallet = Integer.parseInt(content.get(VAR_MONEY));
            if (wallet > money) {
                content.set(VAR_MONEY, String.valueOf(wallet - money));
            }
            log.debug("Took " + money + " Noctori Bucks from " + user.getName() + ", new balance is " + content.get(VAR_MONEY) + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDate getDailyClaimed(User user) {
        try {
            LocalDate dailyClaimed = LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_DAILY_CLAIMED_DATE));
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
            int dailiesClaimed = Integer.parseInt(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_DAILIES_CLAIMED));
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
            return Boolean.parseBoolean(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_NOTIFICATION));
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
            content.set(VAR_NOTIFICATION, String.valueOf(bool));
            log.debug("Set " + user.getName() + " Notification Setting " + bool + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean toggleNotification(User user) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            boolean notification = Boolean.parseBoolean(content.get(VAR_NOTIFICATION));
            content.set(VAR_NOTIFICATION, String.valueOf(!notification));
            log.debug("Set " + user.getName() + " toggled Notification Setting to " + !notification + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
            return notification;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateDailyClaimed(User user) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            LocalDate now = LocalDate.now();
            int dailiesClaimed = Integer.parseInt(content.get(VAR_DAILIES_CLAIMED)) + 1;
            content.set(VAR_DAILY_CLAIMED_DATE, now.toString());
            content.set(VAR_DAILIES_CLAIMED, String.valueOf(dailiesClaimed));
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
            List<String> membersInvited = parseStringArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_MEMBERS_INVITED));
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
            List<String> members = parseStringArray(content.get(VAR_MEMBERS_INVITED));
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
            String invitedByMember = Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_INVITED_BY_MEMBER);
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
            content.set(VAR_INVITED_BY_MEMBER, member.getId());
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
            LocalDate gameClaimedDate = LocalDate.parse(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_GAME_CLAIMED_DATE));
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
            List<String> gameKeys = parseStringArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_GAME_KEYS));
            log.debug("Read " + user.getName() + " owns these game keys " + gameKeys);
            return gameKeys;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void addGameKey(User user, String game) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            List<String> gameKeys = parseStringArray(content.get(VAR_GAME_KEYS));
            gameKeys.add(game);
            LocalDate now = LocalDate.now();
            content.set(VAR_GAME_CLAIMED_DATE, now.toString() );
            content.set(VAR_GAME_KEYS, gameKeys.toString() );
            log.debug("Set " + user.getName() + " last claimed game date to " + now + ".");
            log.debug("Added " + game + " to " + user.getName() + "'s game collection.");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getGenshinUid(User user) {
        try {
            long uid = Long.parseLong(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_GENSHIN_UID));
            log.debug("Read " + user.getName() + " Genshin UID " + uid);
            return uid;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setGenshinUid(User user, long uid) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(VAR_GENSHIN_UID, String.valueOf(uid));
            log.debug("Set " + user.getName() + " Genshin UID to " + uid + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMinecraftUsername(User user) {
        try {
            String username = Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_MINECRAFT_USERNAME);
            log.debug("Read " + user.getName() + " Minecraft Username is " + username);
            return username;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setMinecraftUsername(User user, String username) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(VAR_MINECRAFT_USERNAME, username);
            log.debug("Set " + user.getName() + " Minecraft Username to " + username + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getProfileFields(User user) {
        try {
            List<Integer> profileFields = parseIntegerArray(Files.readAllLines(Paths.get("variables/" + user.getId() + ".var")).get(VAR_PROFILE_FIELDS));
            log.debug("Read " + user.getName() + " has this profile field set " + profileFields);
            return profileFields;
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void setProfileFields(User user, List<Integer> profileFields) {
        try {
            Path filePath = Paths.get("variables/" + user.getId() + ".var");
            List<String> content = Files.readAllLines(filePath);
            content.set(VAR_PROFILE_FIELDS, profileFields.toString());
            log.debug("Set " + user.getName() + " Profile Fields to " + profileFields + ".");
            Files.write(filePath, content, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            log.error(user.getId() + ".var file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseStringArray(String array) {
        String[] strings;
        if (array.substring(1, array.length() - 1).isEmpty()) {
            strings = new String[0];
        } else {
            strings = array.substring(1, array.length() - 1).split(", ");
        }
        return new ArrayList<>(Arrays.asList(strings));
    }

    private static List<Integer> parseIntegerArray(String array) {
        return parseStringArray(array).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    private static void insertVariableForAll(int insertIndex, String defaultVar, boolean save) {
        for ( Member member : Main.getNoctori().getMembers() ) {
            try {
                Path filePath = Paths.get("variables/" + member.getId() + ".var");
                List<String> content = Files.readAllLines(filePath);
                content.add(insertIndex,defaultVar);
                if (save) {
                    Files.write(filePath, content, StandardCharsets.UTF_8);
                } else {
                    System.out.println(member.getId() + " => " + content);
                }
            } catch (NoSuchFileException e) {
                log.error(member.getId() + ".var file not found!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteVariableForAll(int insertIndex, boolean save) {
        for ( Member member : Main.getNoctori().getMembers() ) {
            try {
                Path filePath = Paths.get("variables/" + member.getId() + ".var");
                List<String> content = Files.readAllLines(filePath);
                content.remove(insertIndex);
                if (save) {
                    Files.write(filePath, content, StandardCharsets.UTF_8);
                } else {
                    System.out.println(content);
                }
            } catch (NoSuchFileException e) {
                log.error(member.getId() + ".var file not found!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createNewVariable(int insertIndex, String defaultVar) {
        insertVariableForAll(insertIndex, defaultVar, false);
        System.out.println("Format Okay? 1 = Yes");
        Scanner in = new Scanner(System.in);
        if (in.nextInt() == 1) {
            insertVariableForAll(insertIndex, defaultVar, true);
            log.info("Changes were saved.");
        } else {
            log.info("Changes were not saved.");
        }
        in.close();
    }

    public static void createNewVariable(int insertIndex, int defaultVar) {
        createNewVariable(insertIndex,String.valueOf(defaultVar));
    }

    public static void createNewVariable(int insertIndex, long defaultVar) {
        createNewVariable(insertIndex,String.valueOf(defaultVar));
    }

    public static void createNewVariable(int insertIndex, double defaultVar) {
        createNewVariable(insertIndex,String.valueOf(defaultVar));
    }

    public static void createNewVariable(int insertIndex, boolean defaultVar) {
        createNewVariable(insertIndex,String.valueOf(defaultVar));
    }

    public static void deleteVariable(int deleteIndex) {
        deleteVariableForAll(deleteIndex, false);
        System.out.println("Format Okay? 1 = Yes");
        Scanner in = new Scanner(System.in);
        if (in.nextInt() == 1) {
            deleteVariableForAll(deleteIndex, true);
            log.info("Changes were saved.");
        } else {
            log.info("Changes were not saved.");
        }
        in.close();
    }


}
