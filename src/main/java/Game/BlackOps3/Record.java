package Game.BlackOps3;

import Bot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;

public class Record {

    Manager.Map map;
    Manager.Mod mod;
    int startRound = 1;
    int endRound;
    Player[] players;

    boolean easterEgg = false;

    public Record(Manager.Map map, Manager.Mod mod, int round, Player[] players) {
        this.map = map;
        this.mod = mod;
        this.endRound = round;
        this.players = players;

        getPlayerWithHighestScore();
        getPlayerWithHighestKills();
        getPlayerWithHighestHeadshots();
        getPlayerWithHighestRevives();
        getPlayerWithLowestDowns();
    }

    public Record(Manager.Map map, Manager.Mod mod, int startRound, int endRound, Player[] players) {
        this.map = map;
        this.mod = mod;
        this.startRound = startRound;
        this.endRound = endRound;
        this.players = players;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Manager.Map getMap() {
        return map;
    }

    private void getPlayerWithHighestScore() {
        Player highest = null;
        for (Player player : getPlayers()) {
            if (highest == null || player.getScore() > highest.getScore()) highest = player;
        }
        assert highest != null;
        highest.mostScore = true;
    }

    private void getPlayerWithHighestKills() {
        Player highest = null;
        for (Player player : getPlayers()) {
            if (highest == null || player.getKills() > highest.getKills()) highest = player;
        }
        assert highest != null;
        highest.mostKills = true;
    }

    private void getPlayerWithHighestHeadshots() {
        Player highest = null;
        for (Player player : getPlayers()) {
            if (highest == null || player.getHeadshots() > highest.getHeadshots()) highest = player;
        }
        assert highest != null;
        highest.mostHeadshots = true;
    }

    private void getPlayerWithHighestRevives() {
        Player highest = null;
        for (Player player : getPlayers()) {
            if (highest == null || player.getRevives() > highest.getRevives()) highest = player;
        }
        assert highest != null;
        highest.mostRevives = true;
    }

    private void getPlayerWithLowestDowns() {
        Player highest = null;
        for (Player player : getPlayers()) {
            if (highest == null || (player.getDowns() < highest.getDowns() && player.getDowns() >= 0)) highest = player;
        }
        assert highest != null;
        highest.leastDowns = true;
    }

    public void payPlayers() {
        for (Player player : getPlayers()) {
            EmbedBuilder embed = player.generatePayment(getPlayers().length);
            embed.setFooter(getMap().getMapName());
            Main.getNoctori().getTextChannelById(884994683166662706L).sendMessageEmbeds(
                    embed.build()
            ).queue();
        }
    }

    public MessageEmbed getAsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Black Ops 3 Zombies");
        embed.setTitle(map.getMapName());
        StringBuilder desc = new StringBuilder();
        desc.append("Survived");
        if (startRound != 1) {
            desc.append(" from Round ").append(startRound);
        }
        desc.append(" to Round ").append(endRound);
        if (mod != Manager.Mod.VANILLA) {
            desc.append("\nUsing ").append(mod.getName()).append(" Mod");
        }
        embed.setDescription(desc);
        for (Player player : players) {
            embed.addField(
                    player.getMember().getEffectiveName(),
                    player.toString(),
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
                ", round=" + endRound +
                ", players=" + Arrays.toString(players) +
                ", easterEgg=" + easterEgg +
                '}';
    }
}
