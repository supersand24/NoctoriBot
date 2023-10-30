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
import java.util.ArrayList;
import java.util.List;

public class Var {

    private final static Logger log = LoggerFactory.getLogger(Var.class);

    static String ip;
    static String username;
    static String password;

    public static void verifyCredentials() {
        try {
            List<String> dbCredentials = Files.readAllLines(Paths.get("db.credentials"));
            if (dbCredentials.size() != 3) { log.error("db.credentials is not formatted properly."); System.exit(2); }
            ip = dbCredentials.get(0);
            username = dbCredentials.get(1);
            password = dbCredentials.get(2);
            log.info("Database Credentials Imported");
        } catch (NoSuchFileException e) {
            log.error("Could not find the db.credentials file!");
            System.exit(3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Connection openConnection() {
        try {
            log.trace("Database Connection Opened");
            return DriverManager.getConnection("jdbc:mysql://" + ip + "/noctori_bot", username, password);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(4);
        }
        return null;
    }

    //Guild Related

    private static ResultSet getResultsForGuild(Connection connection, Guild guild) {
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

    private static PreparedStatement getStatementForGuild(Connection connection, Guild guild, String update) {
        if (guild == null) { log.error("Guild was null."); return null; }
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `noctori_bot`.`guild` SET " + update + " = ? WHERE `id` = ?"
            );
            statement.setString(2, guild.getId());
            return statement;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean hasEconomy(Guild guild) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForGuild(connection, guild);
            boolean economy = results.getBoolean("hasEconomy");
            if (economy)
                log.debug("Read " + guild.getName() + " has Economy enabled.");
            else
                log.debug("Read " + guild.getName() + " has Economy disabled.");
            return economy;
        } catch (NullPointerException | SQLException ex) {
            log.error("hasEconomy could not be retrieved.");
            return false;
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static void setEconomy(Guild guild, boolean economy) {
        try {
            Connection connection = openConnection();
            PreparedStatement preparedStatement = getStatementForGuild(connection, guild, "hasEconomy");
            if (economy) {
                preparedStatement.setInt(1, 1);
                log.debug("Enabled Economy for " + guild.getName() + ".");
            }
            else {
                preparedStatement.setInt(1, 0);
                log.debug("Disabled Economy for " + guild.getName() + ".");
            }
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static long getAutoVoiceNewChannelId(Guild guild) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForGuild(connection, guild);
            long autoVoiceNewChannelId = results.getLong("autoVoiceNewChannel");
            log.debug("Read " + guild.getName() + " Auto Voice New Channel ID is " + autoVoiceNewChannelId + ".");
            return autoVoiceNewChannelId;
        } catch (NullPointerException | SQLException ex) {
            log.error("getAutoVoiceNewChannelId could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return 0;
    }

    public static StageChannel getAutoVoiceNewChannel(Guild guild) {
        return guild.getStageChannelById(getAutoVoiceNewChannelId(guild));
    }

    public static void setAutoVoiceNewChannelId(Guild guild, long autoVoiceNewChannelId) {
        try {
            if (guild == null) { log.error("Guild was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForGuild(connection, guild, "autoVoiceNewChannel");
            preparedStatement.setLong(1, autoVoiceNewChannelId);
            preparedStatement.executeUpdate();
            log.debug("Auto Voice New Channel ID for " + guild.getName() + " was set to " + autoVoiceNewChannelId);

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void setAutoVoiceNewChannelId(Guild guild, StageChannel autoVoiceNewChannel) {
        try {
            if (guild == null) { log.error("Guild was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForGuild(connection, guild, "autoVoiceNewChannel");
            preparedStatement.setLong(1, autoVoiceNewChannel.getIdLong());
            preparedStatement.executeUpdate();
            log.debug("Auto Voice New Channel ID for " + guild.getName() + " was set to " + autoVoiceNewChannel.getIdLong());

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //User Related

    private static ResultSet getResultsForUser(Connection connection, User user) {
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

    private static PreparedStatement getStatementForUser(Connection connection, User user, String update) {
        if (user == null) { log.error("User was null."); return null; }
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `noctori_bot`.`user` SET " + update + " = ? WHERE `id` = ?"
            );
            statement.setString(2, user.getId());
            return statement;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean getPaymentNotification(User user) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForUser(connection, user);
            boolean notification = results.getBoolean("paymentNotification");
            if (notification)
                log.debug("Read " + user.getName() + " has Payment Notifications enabled.");
            else
                log.debug("Read " + user.getName() + " has Payment Notifications disabled.");
            return notification;
        } catch (NullPointerException | SQLException ex) {
            log.error("paymentNotification could not be retrieved.");
            return false;
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static void setPaymentNotification(User user, boolean notification) {
        try {
            Connection connection = openConnection();
            PreparedStatement preparedStatement = getStatementForUser(connection, user, "hasEconomy");
            if (notification) {
                preparedStatement.setInt(1, 1);
                log.debug("Enabled Payment Notifications for " + user.getName() + ".");
            }
            else {
                preparedStatement.setInt(1, 0);
                log.debug("Disabled Payment Notifications for " + user.getName() + ".");
            }
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static LocalDate getGameClaimedDate(User user) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForUser(connection, user);
            LocalDate date = results.getDate("gameClaimedDate").toLocalDate();
            log.debug("Read " + user.getName() + " Last Game Claimed on " + date.toString() + ".");
            return date;
        } catch (Exception ex) {
            log.error("getGameClaimedDate could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }

    public static void setGameClaimedDate(User user, LocalDate gameClaimedDate) {
        try {
            if (user == null) { log.error("User was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForUser(connection, user, "autoVoiceNewChannel");
            preparedStatement.setDate(1, Date.valueOf(gameClaimedDate));
            preparedStatement.executeUpdate();
            log.debug("Last Game Claimed Date for " + user.getName() + " was set to " + gameClaimedDate + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> getGameKeys(User user) {
        Connection connection = openConnection();
        try {
            //TODO Make a Game Key it's own object.
            List<String> keys = new ArrayList<>();
            ResultSet userResults = getResultsForUser(connection, user);
            ResultSet gameKeyResults = getResultsForGameKeys(connection, user);
            keys.add(gameKeyResults.getString(userResults.getString("gameKeys")));
            log.debug("Read " + user.getName() + " Claimed " + keys.get(0) + ".");
            return keys;
        } catch (Exception ex) {
            log.error("gameKeys could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }

    public static long getGenshinUID(User user) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForUser(connection, user);
            long genshinUID = results.getLong("genshinUID");
            log.debug("Read " + user.getName() + " Genshin UID is " + genshinUID + ".");
            return results.getLong("genshinUID");
        } catch (Exception ex) {
            log.error("getGenshinUID could not be retrieved.");
        } finally {
            try { connection.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
        return 0;
    }

    public static void setGenshinUID(User user, long genshinUID) {
        try {
            if (user == null) { log.error("User was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForUser(connection, user, "genshinUID");
            preparedStatement.setLong(1, genshinUID);
            preparedStatement.executeUpdate();
            log.debug("Genshin UID for " + user.getName() + " was set to " + genshinUID + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static String getMinecraftUUID(User user) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForUser(connection, user);
            String minecraftUUID = results.getString("minecraftUUID");
            log.debug("Read " + user.getName() + " Minecraft UUID is " + minecraftUUID + ".");
            return minecraftUUID;
        } catch (Exception ex) {
            log.error("getMinecraftUUID could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return "";
    }

    public static void setMinecraftUUID(User user, String minecraftUUID) {
        try {
            if (user == null) { log.error("User was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForUser(connection, user, "minecraftUUID");
            preparedStatement.setString(1, minecraftUUID);
            preparedStatement.executeUpdate();
            log.debug("Minecraft UUID for " + user.getName() + " was set to " + minecraftUUID + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Member Related

    private static ResultSet getResultsForMember(Connection connection, User user, Guild guild) {
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

    private static ResultSet getResultsForMember(Connection connection, Member member) {
        if (member == null) { log.error("Member was null."); return null; }
        if (member.getUser().isBot()) { log.error("Member is a bot."); return null; }
        return getResultsForMember(connection, member.getUser(),member.getGuild());
    }

    private static PreparedStatement getStatementForMember(Connection connection, Member member, String update) {
        if (member == null) { log.error("Member was null."); return null; }
        if (member.getUser().isBot()) { log.error("Member is a bot."); return null; }
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `noctori_bot`.`member` SET " + update + " = ? WHERE `user_id` = ? AND `guild_id` = ?"
            );
            statement.setString(2, member.getId());
            statement.setString(3, member.getGuild().getId());
            return statement;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int getMoney(Member member) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForMember(connection, member);
            int money = results.getInt("money");
            log.debug("Read " + member.getEffectiveName() + " Money is " + money + " in " + member.getGuild().getName() + ".");
            return money;
        } catch (SQLException ex) {
            log.error("getMoney could not be retrieved.");
        }
        finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return 0;
    }

    public static void setMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "money");
            preparedStatement.setInt(1, money);
            preparedStatement.executeUpdate();
            log.debug("Money for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was set to " + money + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "money");
            preparedStatement.setInt(1, money + getMoney(member));
            preparedStatement.executeUpdate();
            log.debug("Money for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was increased by " + money + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void removeMoney(Member member, int money) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "money");
            preparedStatement.setInt(1, money - getMoney(member));
            preparedStatement.executeUpdate();
            log.debug("Money for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was decreased by " + money + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static LocalDate getDailyClaimed(Member member) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForMember(connection, member);
            LocalDate date = results.getDate("dailyClaimed").toLocalDate();
            log.debug("Read " + member.getEffectiveName() + " Last Daily Logged on " + date + " in " + member.getGuild().getName() + ".");
            return date;
        } catch (Exception ex) {
            log.error("getDailyClaimed could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }

    public static void setDailyClaimed(Member member, LocalDate dailyClaimed) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "dailyClaimed");
            preparedStatement.setDate(1, Date.valueOf(dailyClaimed));
            preparedStatement.executeUpdate();
            log.debug("Daily Claimed for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was set to " + dailyClaimed + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateDailyClaimed(Member member) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "dailyClaimed");
            log.debug("Setting date to " + Date.valueOf(LocalDate.now()));
            preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
            preparedStatement.executeUpdate();
            log.debug("Daily Claimed for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was set to " + LocalDate.now() + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int getDailiesClaimed(Member member) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForMember(connection, member);
            int dailiesClaimed = results.getInt("dailiesClaimed");
            log.debug("Read " + member.getEffectiveName() + " Dailies Claimed is " + dailiesClaimed + " in " + member.getGuild().getName() + ".");
            return dailiesClaimed;
        } catch (Exception ex) {
            log.error("dailiesClaimed could not be retrieved.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return 0;
    }

    public static void setDailiesClaimed(Member member, int dailiesClaimed) {
        try {
            if (member == null) { log.error("Member was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "dailiesClaimed");
            preparedStatement.setInt(1, dailiesClaimed);
            preparedStatement.executeUpdate();
            log.debug("Dailies Claimed for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was set to " + dailiesClaimed + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Member getInvitedBy(Member member) {
        Connection connection = openConnection();
        try {
            ResultSet results = getResultsForMember(connection, member);
            Member invitedBy = member.getGuild().getMemberById(results.getLong("invitedByMember"));
            log.debug("Read " + member.getEffectiveName() + " was Invited By " + invitedBy.getEffectiveName() + ".");
            return invitedBy;
        } catch (SQLException ex) {
            log.error("invitedBy for " + member.getEffectiveName() + " could not be retrieved");
        } catch (NullPointerException ex) {
            log.error(member.getEffectiveName() + " was not invited by anyone.");
        } finally {
            try { connection.close();}
            catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }

    public static void setInvitedBy(Member member, Member invitedBy) {
        try {
            if (member == null ) { log.error("Member was null."); return; }
            if (invitedBy == null) { log.error("Member Invited By was null."); return; }
            Connection connection = openConnection();

            PreparedStatement preparedStatement = getStatementForMember(connection, member, "invitedByMember");
            preparedStatement.setLong(1, invitedBy.getIdLong());
            preparedStatement.executeUpdate();
            log.debug("Set " + member.getEffectiveName() + " was invited by " + invitedBy.getEffectiveName() + " to " + invitedBy.getGuild().getName() + ".");

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Game Key Related

    private static ResultSet getResultsForGameKeys(Connection connection, Guild guild) {
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

    private static ResultSet getResultsForGameKeys(Connection connection, User user) {
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

    private static ResultSet getResultsForGameKeys(Connection connection, User user, Guild guild) {
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

    public static void addNewMemberAndUser(Member member) {
        if (member == null) { log.error("Member was null."); return; }

        Connection connection = openConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO `noctori_bot`.`user` SET (`id`) VALUES ?"
            );
            statement.setLong(1,member.getUser().getIdLong());
            statement.executeUpdate();

            statement = connection.prepareStatement(
                    "INSERT INTO `noctori_bot`.`member` (`user_id`, `guild_id`) VALUES (?, ?)"
            );
            statement.setLong(1, member.getIdLong());
            statement.setLong(2, member.getGuild().getIdLong());
            statement.executeUpdate();

            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Economy Related

    /**
     * Checks the Database if the Guild has Economy Enabled and Checks if the Member has not logged in today.
     * @param member The Member to check their last log in date.
     * @return
     */
    public static boolean daily(Member member) {
        if (member == null) { log.error("Member was null."); return false; }
        Connection connection = openConnection();

        try {
            ResultSet guildResults = getResultsForGuild(connection, member.getGuild());
            boolean economy = guildResults.getBoolean("hasEconomy");
            if (economy) {
                log.debug("Read " + member.getGuild().getName() + " has Economy enabled.");
                ResultSet memberResults = getResultsForMember(connection, member);
                LocalDate date = memberResults.getDate("dailyClaimed").toLocalDate();
                log.debug("Read " + member.getEffectiveName() + " Last Daily Logged on " + date + " in " + member.getGuild().getName() + ".");
                if (LocalDate.now().compareTo(date) > 0) {
                    log.info(member.getEffectiveName() + " has logged in to " + member.getGuild().getName() + " today.");
                    PreparedStatement preparedStatement = getStatementForMember(connection, member, "dailyClaimed");
                    preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
                    preparedStatement.executeUpdate();
                    log.debug("Daily Claimed for " + member.getEffectiveName() + " of " + member.getGuild().getName() + " was set to " + LocalDate.now() + ".");
                    return true;
                }
            } else log.debug(member.getGuild().getName() + " has Economy disabled.");
        } catch (SQLException ex) {
            log.error("getMoney could not be retrieved.");
        }
        finally {
            try {
                connection.close();
            }
            catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }
}
