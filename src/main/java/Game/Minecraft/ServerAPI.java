package Game.Minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerAPI {

    private static final String SERVER = "http://76.105.66.107:4567/v1/";

    private static final String getUuidResource = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String getNameResource = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    public static List<Player> getOnlinePlayers() {
        Gson gson = new Gson();
        List<Player> playerList = new ArrayList<>();
        ApiResponse apiResponse = getApiResponse(SERVER + "players");
        JsonElement json = new JsonParser().parse(apiResponse.getContent());
        for (JsonElement jsonE : json.getAsJsonArray()) {
            System.out.println(jsonE.toString());
            gson.fromJson(jsonE,Player.class);
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

    public static String getUUID(String username) {
        Gson gson = new Gson();
        ApiResponse apiResponse = getApiResponse(String.format(getUuidResource,username));
        if (apiResponse.getHttpStatus() == HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IllegalArgumentException("The given username was not found by Mojang API.");
        }
        return gson.fromJson(apiResponse.getContent(), PlayerInfo.class).getId();
    }

    public static String getUsername(String uuid) {
        Gson gson = new Gson();
        ApiResponse apiResponse = getApiResponse(String.format(getNameResource,uuid).replace("-", ""));
        if (apiResponse.getHttpStatus() == HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IllegalArgumentException("The given uuid was not found by Mojang API.");
        }
        return gson.fromJson(apiResponse.getContent(), PlayerInfo.class).getName();
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

    private static ApiResponse getApiResponse(String resource) {
        try {
            String responseContent;

            URL url = new URL(resource);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setDoInput(true);

            http.connect();

            if (http.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                return new ApiResponse("", http.getResponseCode());
            }

            try (InputStream is = http.getInputStream()) {
                responseContent = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            }

            return new ApiResponse(responseContent, http.getResponseCode());
        } catch (IOException ignored) {
            throw new IllegalArgumentException("The given resource string is not a valid URL.");
        }
    }

    private static class ApiResponse {
        private String content;
        private int httpStatus;

        public ApiResponse(String content, int httpStatus) {
            setContent(content);
            setHttpStatus(httpStatus);
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
        }
    }

}
