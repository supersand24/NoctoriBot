package Game.ClashOfClans;

import Bot.Var;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class Manager {

    private final static Logger log = LoggerFactory.getLogger(Manager.class);

    private static String apiKey;

    public static void importAPIKey() {
        try {
            List<String> cocAPI = Files.readAllLines(Paths.get("coc.api"));
            if (cocAPI.size() != 1) { log.error("coc.api is not formatted properly."); System.exit(2); }
            apiKey = cocAPI.get(0);
            log.info("Clash of Clans API Key Imported");
        } catch (NoSuchFileException e) {
            log.error("Could not find the coc.api file!");
            System.exit(4);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Clan getClan(Guild guild) {
        if (guild == null) { log.error("Guild was null."); return null; }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = "https://api.clashofclans.com/v1/clans/%23" + Var.getClashOfClansClanId(guild);

            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", "Bearer " + apiKey);

            CloseableHttpResponse response = httpClient.execute(get);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                Gson gson = new Gson();
                return gson.fromJson(EntityUtils.toString(response.getEntity()), Clan.class);
            } else {
                log.error("Status Code: " + statusCode);
                Gson gson = new Gson();
                ClientError error = gson.fromJson(EntityUtils.toString(response.getEntity()), ClientError.class);
                log.error(error.toString());
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Player getPlayer(String id) {
        if (id.isEmpty()) { log.error("ID was empty."); return null; }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = "https://api.clashofclans.com/v1/players/%23" + id;

            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", "Bearer " + apiKey);

            CloseableHttpResponse response = httpClient.execute(get);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                Gson gson = new Gson();
                //System.out.println(EntityUtils.toString(response.getEntity()));
                return gson.fromJson(EntityUtils.toString(response.getEntity()), Player.class);
            } else {
                log.error("Status Code: " + statusCode);
                Gson gson = new Gson();
                ClientError error = gson.fromJson(EntityUtils.toString(response.getEntity()), ClientError.class);
                log.error(error.toString());
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
