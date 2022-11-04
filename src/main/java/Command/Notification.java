package Command;

import Bot.Var;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class Notification {

    public Notification(User user, MessageChannelUnion channel) {
        StringBuilder string = new StringBuilder();
        string.append("Toggled Notification to ");
        if (Var.toggleNotification(user)) string.append("off."); else string.append("on.");
        channel.sendMessage(string).queue();
    }

}
