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

public class ServerAPI {

    private static final String SERVER_IP = "76.105.66.107";
    private static final int SERVER_PORT = 4567;

    public static String getOnlinePlayers() {
        JsonElement json = getJson("players");
        if (json.toString().equals("[]")) {
            return "No Online Players";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (JsonElement jsonE : json.getAsJsonArray()) {
                String username = jsonE.getAsJsonObject().get("displayName").toString();
                username = username.substring(1,username.length() - 1);
                stringBuilder.append(username).append("\n");
            }
            return stringBuilder.toString();
        }
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

    private static String getString(InputStream inputStream) {
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
