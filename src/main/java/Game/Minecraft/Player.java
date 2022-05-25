package Game.Minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Player {

    String uuid;
    String username;
    float x;
    float y;
    float z;
    String dimension;

    Player(String uuid, String username, JsonElement xyz, String dimension) {
        this.uuid = uuid;
        this.username = username;
        JsonArray array = xyz.getAsJsonArray();
        this.x = array.get(0).getAsFloat();
        this.y = array.get(1).getAsFloat();
        this.z = array.get(2).getAsFloat();
        this.dimension = dimension;
    }

    public String getUsername() {
        return username;
    }
}
