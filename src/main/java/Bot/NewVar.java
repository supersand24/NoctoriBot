package Bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class NewVar {

    private final static Logger log = LoggerFactory.getLogger(NewVar.class);

    static String ip;
    static String username;
    static String password;

    static Connection connection;

    public static boolean initialize() {
        //Read the db.credentials files to get ip, username, and password.
        try {
            List<String> dbCredentials = Files.readAllLines(Paths.get("db.credentials"));
            if (dbCredentials.size() != 3) { log.error("db.credentials is not formatted properly."); System.exit(2); }
            ip = dbCredentials.get(0);
            username = dbCredentials.get(1);
            password = dbCredentials.get(2);
        } catch (NoSuchFileException e) {
            log.error("Could not find the db.credentials file!");
            System.exit(3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Open connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + "/noctori_bot", username, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(4);
        }

        log.info("Database Connection Acquired!");
        return true;
    }

    //Guild Related

    private static ResultSet getResultsForGuild(Guild guild) {
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from guild where id = " + guild.getId());
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find " + guild.getName() + " guild on database.");
        }
        return null;
    }

    private static PreparedStatement getStatementForGuild(Guild guild, String update) {
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            //connection.setAutoCommit(false);
            String sql = "UPDATE `noctori_bot`.`guild` SET " + update + " = ? where id = " + guild.getId();
            return connection.prepareStatement(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean hasEconomy(Guild guild) {
        try {
            ResultSet results = getResultsForGuild(guild);
            return results.getBoolean("hasEconomy");
        } catch (Exception ex) {
            log.error("hasEconomy could not be retrieved.");
            return false;
        }
    }

    public static void setEconomy(Guild guild, boolean economy) {
        try {
            if (guild == null) { log.error("Guild was null."); return; }
            PreparedStatement preparedStatement = getStatementForGuild(guild, "hasEconomy");
            if (economy)
                preparedStatement.setInt(1, 1);
            else
                preparedStatement.setInt(1, 0);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static long getAutoVoiceNewChannelId(Guild guild) {
        try {
            ResultSet results = getResultsForGuild(guild);
            return results.getLong("autoVoiceNewChannel");
        } catch (Exception ex) {
            log.error("getAutoVoiceNewChannelId could not be retrieved.");
        }
        return 0;
    }

    public static StageChannel getAutoVoiceNewChannel(Guild guild) {
        return guild.getStageChannelById(getAutoVoiceNewChannelId(guild));
    }

    public static void setAutoVoiceNewChannelId(Guild guild, long autoVoiceNewChannelId) {
        try {
            if (guild == null) { log.error("Guild was null."); return; }
            PreparedStatement preparedStatement = getStatementForGuild(guild, "autoVoiceNewChannel");
            preparedStatement.setLong(1, autoVoiceNewChannelId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void setAutoVoiceNewChannelId(Guild guild, StageChannel autoVoiceNewChannel) {
        try {
            if (guild == null) { log.error("Guild was null."); return; }
            PreparedStatement preparedStatement = getStatementForGuild(guild, "autoVoiceNewChannel");
            preparedStatement.setLong(1, autoVoiceNewChannel.getIdLong());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //User Related

    private static ResultSet getResultsForUser(User user) {
        if (user == null) { log.error("User was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from user where id = " + user.getId());
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find user " + user.getName() + " on database.");
        }
        return null;
    }

    private static PreparedStatement getStatementForUser(User user, String update) {
        if (user == null) { log.error("User was null."); return null; }
        try {
            //connection.setAutoCommit(false);
            String sql = "UPDATE `noctori_bot`.`user` SET " + update + " = ? where id = " + user.getId();
            return connection.prepareStatement(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static LocalDate getGameClaimedDate(User user) {
        try {
            ResultSet results = getResultsForUser(user);
            return results.getDate("gameClaimedDate").toLocalDate();
        } catch (Exception ex) {
            log.error("getGameClaimedDate could not be retrieved.");
        }
        return null;
    }

    public static void setGameClaimedDate(User user, LocalDate gameClaimedDate) {
        try {
            if (user == null) { log.error("User was null."); return; }
            PreparedStatement preparedStatement = getStatementForUser(user, "autoVoiceNewChannel");
            preparedStatement.setDate(1, Date.valueOf(gameClaimedDate));
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static long getGenshinUID(User user) {
        try {
            ResultSet results = getResultsForUser(user);
            return results.getLong("genshinUID");
        } catch (Exception ex) {
            log.error("getGenshinUID could not be retrieved.");
        }
        return 0;
    }

    public static void setGenshinUID(User user, long genshinUID) {
        try {
            if (user == null) { log.error("User was null."); return; }
            PreparedStatement preparedStatement = getStatementForUser(user, "genshinUID");
            preparedStatement.setLong(1, genshinUID);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static String getMinecraftUUID(User user) {
        try {
            ResultSet results = getResultsForUser(user);
            return results.getString("minecraftUUID");
        } catch (Exception ex) {
            log.error("getMinedcraftUUID could not be retrieved.");
        }
        return "";
    }

    public static void setMinecraftUUID(User user, String minecraftUUID) {
        try {
            if (user == null) { log.error("User was null."); return; }
            PreparedStatement preparedStatement = getStatementForUser(user, "minecraftUUID");
            preparedStatement.setString(1, minecraftUUID);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Member Related

    private static ResultSet getResultsForMember(User user, Guild guild) {
        if (user == null) { log.error("User was null."); return null; }
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from member where user_id = " + user.getId() + " and guild_id = " + guild.getId() );
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find user " + user.getName() + " for " + guild.getName() + " guild on database.");
        }
        return null;
    }

    private static ResultSet getResultsForMember(Member member) {
        if (member == null) { log.error("Member was null."); return null; }
        return getResultsForMember(member.getUser(),member.getGuild());
    }

    private static PreparedStatement getStatementForMember(Member member, String update) {
        if (member == null) { log.error("Member was null."); return null; }
        try {
            //connection.setAutoCommit(false);
            String sql = "UPDATE `noctori_bot`.`member` SET " + update + " = ? where 'user_id' = " + member.getId() + " and 'guild_id' = " + member.getGuild().getId();
            return connection.prepareStatement(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int getMoney(Member member) {
        try {
            ResultSet results = getResultsForMember(member);
            return results.getInt("money");
        } catch (Exception ex) {
            log.error("getMoney could not be retrieved.");
        }
        return 0;
    }

    public static void setMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "money");
            preparedStatement.setInt(1, money);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "money");
            preparedStatement.setInt(1, money + getMoney(member));
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removeMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "money");
            preparedStatement.setInt(1, money - getMoney(member));
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static LocalDate getDailyClaimed(Member member) {
        try {
            ResultSet results = getResultsForMember(member);
            return results.getDate("dailyClaimed").toLocalDate();
        } catch (Exception ex) {
            log.error("getDailyClaimed could not be retrieved.");
        }
        return null;
    }

    public static void setDailyClaimed(Member member, LocalDate dailyClaimed) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "dailyClaimed");
            preparedStatement.setDate(1, Date.valueOf(dailyClaimed));
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateDailyClaimed(Member member) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "dailyClaimed");
            preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int getDailiesClaimed(Member member) {
        try {
            ResultSet results = getResultsForMember(member);
            return results.getInt("dailiesClaimed");
        } catch (Exception ex) {
            log.error("getDailiesClaimed could not be retrieved.");
        }
        return 0;
    }

    public static void setDailiesClaimed(Member member, int dailiesClaimed) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            PreparedStatement preparedStatement = getStatementForMember(member, "dailiesClaimed");
            preparedStatement.setInt(1, dailiesClaimed);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Game Key Related

    private static ResultSet getResultsForGameKeys(Guild guild) {
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from game_keys where guild_id = " + guild.getId() );
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find any game keys for " + guild.getName() + " guild on database.");
        }
        return null;
    }

    private static ResultSet getResultsForGameKeys(User user) {
        if (user == null) { log.error("User was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from game_keys where claimedBy = " + user.getId() );
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find any game keys for user " + user.getName() + " on database.");
        }
        return null;
    }

    private static ResultSet getResultsForGameKeys(User user, Guild guild) {
        if (user == null) { log.error("User was null."); return null; }
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            ResultSet results = connection.createStatement().executeQuery("select * from game_keys where claimedBy = " + user.getId() + " and guild_id = " + guild.getId() );
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find any game keys for user " + user.getName() + " in " + guild.getName() + " guild on database.");
        }
        return null;
    }

    //TODO Add methods to get games claimed

}
