package Game.Minecraft;

import Bot.Var;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class Username {

    public Username(Member member, Message message, String[] arguments) {
        if (arguments.length > 1) {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() > 0) {
                User user = mentionedUsers.get(0);
                String username = Var.getMinecraftUsername(user);
                if (username.isEmpty()) {
                    message.reply(user.getName() + " does not have a Minecraft Username set, they can do so my using `n!username <INSERT USERNAME>`").queue();
                } else {
                    message.reply(user.getName() + " Minecraft Username is `" + username + "`").queue();
                }
            } else {
                Var.setMinecraftUsername(member.getUser(), arguments[1]);
                message.reply("Set `" + arguments[1] + "` to be your Minecraft Username.").queue();
            }
        } else {
            message.reply("Not enough arguments!").queue();
        }
    }

}
