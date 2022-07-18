package Command;

import Bot.Var;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class Genshin {

    public Genshin(Member member, Message message, String[] arguments) {
        if (arguments.length > 1) {
            List<User> mentionedUsers = message.getMentions().getUsers();
            if (mentionedUsers.size() > 0) {
                User user = mentionedUsers.get(0);
                long uid = Var.getGenshinUid(user);
                if (uid == 0) {
                    message.reply(user.getName() + " does not have a UID set, they can do so my using `n!genshin <INSERT UID>`").queue();
                } else {
                    message.reply(user.getName() + " Genshin UID is `" + Var.getGenshinUid(user) + "`").queue();
                }
            } else {
                if (arguments[1].length() == 9) {
                    try {
                        Var.setGenshinUid(member.getUser(), Long.parseLong(arguments[1]));
                        message.reply("Set `" + arguments[1] + "` to be your UID.").queue();
                    } catch (NumberFormatException e) {
                        message.reply(arguments[1] + " is not an uid!").queue();
                    }
                } else {
                    message.reply("Does not look to be the right size.").queue();
                }
            }
        } else {
            message.reply("Not enough arguments!").queue();
        }
    }

}
