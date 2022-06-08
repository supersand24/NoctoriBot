package Game.Minecraft;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class GetOnlinePlayers {

    public GetOnlinePlayers(Message message) {
        StringBuilder string = new StringBuilder();
        List<Player> players = ServerAPI.getOnlinePlayers();
        string.append("**Online Players ").append(players.size()).append("/8**\n");
        for (Player player : players) {
            string.append(player.getUsername()).append("\n");
        }
        message.reply(string.toString()).mentionRepliedUser(false).queue();
    }

}
