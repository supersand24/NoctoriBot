package Game.Minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ServerAPI {

    private static final String SERVER_IP = "76.105.66.107";
    private static final int SERVER_PORT = 4567;

    public static List<Player> getOnlinePlayers() {
        List<Player> playerList = new ArrayList<>();
        JsonElement json = getJson("players");
        for (JsonElement jsonE : json.getAsJsonArray()) {
            JsonObject jsonObject = jsonE.getAsJsonObject();
            playerList.add(new Player(
                            jsonObject.get("uuid").getAsString(),
                            jsonObject.get("displayName").getAsString(),
                            jsonObject.get("location"),
                            jsonObject.get("dimension").getAsString()
                    )
            );
        }
        return playerList;
    }

    public static JsonElement getJson(String string) {
        try {
            URL url = new URL("http://" + SERVER_IP + ":" + SERVER_PORT + "/v1/" + string);
            URLConnection urlConnection = url.openConnection();
            return new JsonParser().parse(getString(urlConnection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonElement getJsonFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            URLConnection urlConnection = url.openConnection();
            return new JsonParser().parse(getString(urlConnection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUUID(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            URLConnection urlConnection = url.openConnection();
            String uuid = new JsonParser().parse(getString(urlConnection.getInputStream())).getAsJsonObject().get("id").toString();
            uuid = uuid.substring(1, uuid.length() - 1);
            return uuid;
        } catch (IllegalStateException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUsername(String uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            URLConnection urlConnection = url.openConnection();
            String username = new JsonParser().parse(getString(urlConnection.getInputStream())).getAsJsonObject().get("name").toString();
            username = username.substring(1,username.length() - 1);
            return username;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")))
        {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(inputLine);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
