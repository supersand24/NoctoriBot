package Game.BlackOps3;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard {

    private final Manager.Map map;
    private List<Record> records = new ArrayList<>();

    Leaderboard(Manager.Map map) {
        this.map = map;
        this.records = records;
    }

    public Manager.Map getMap() {
        return map;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void addRecord(Record record) {
        records.add(record);
    }

    public MessageEmbed getAsEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(getMap().getMapName());
        for (Record record : records) {
            embed.addField("Round " + record.round, record.getPlayerNames(), true);
        }
        return embed.build();
    }
}
