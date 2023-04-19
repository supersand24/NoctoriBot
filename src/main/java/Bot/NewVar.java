package Bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class NewVar {

    private final static Logger log = LoggerFactory.getLogger(NewVar.class);

    static String ip;
    static String username;
    static String password;

    static Statement statement;

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
            statement = DriverManager.getConnection("jdbc:mysql://" + ip + "/noctori_bot", username, password).createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(4);
        }

        log.info("Database Connection Acquired!");
        return true;
    }

    //Guild Related

    private static ResultSet getResultsForGuild(Guild guild) {
        if (guild == null) { log.error("Guild was null."); }
        try {
            ResultSet results = statement.executeQuery("select * from guild where id = " + guild.getId());
            results.next();
            return results;
        } catch (SQLException ex) {
            log.error("Could not find " + guild.getName() + " on database.");
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

}
