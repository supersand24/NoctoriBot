package Game.BlackOps3;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

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

    public Record(Manager.Map map, Manager.Mod mod, int startRound, int endRound, Player[] players) {
        this.map = map;
        this.mod = mod;
        this.round = endRound;
        this.players = players;
    }

    public MessageEmbed getAsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(map.toString());
        embed.setDescription(
                "Survived to Round " + round + "\n" + mod
        );
        for (Player player : players) {
            embed.addField(
                    String.valueOf(player.getMemberId()),
                            player.getScore() + " Score\n" +
                            player.getKills() + " Kills\n" +
                            player.getHeadshots() + " Headshots\n" +
                            player.getDowns() + " Downs\n" +
                            player.getRevives() + " Revives",
                    true
            );
        }
        return embed.build();
    }

    public String getPlayerNames() {
        StringBuilder str = new StringBuilder();
        for (Player player : players) {
            str.append(player.getMember().getEffectiveName()).append("\n");
        }
        return str.toString();
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
