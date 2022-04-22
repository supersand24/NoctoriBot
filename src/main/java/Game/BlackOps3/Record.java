package Game.BlackOps3;

import java.util.Arrays;

public class Record {

    Manager.Map map;
    Manager.Mod mod;
    int round;
    Player[] players;

    boolean easterEgg = false;

    public Record(Manager.Map map, Manager.Mod mod, int round, Player[] players) {
        this.map = map;
        this.mod = mod;
        this.round = round;
        this.players = players;
    }

    @Override
    public String toString() {
        return "Record{" +
                "map='" + map + '\'' +
                ", round=" + round +
                ", players=" + Arrays.toString(players) +
                ", easterEgg=" + easterEgg +
                '}';
    }
}
