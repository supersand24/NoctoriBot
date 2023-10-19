package Game.Minecraft;

import Bot.Var;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class Username {

    public Username(Member member, Message message, String[] arguments) {
        if (arguments.length > 1) {
            List<User> mentionedUsers = message.getMentions().getUsers();
            if (mentionedUsers.size() > 0) {
                User user = mentionedUsers.get(0);
                String username = ServerAPI.getUsername(Var.getMinecraftUUID(user));
                if (username.isEmpty()) {
                    message.reply(user.getName() + " does not have a Minecraft Username set, they can do so my using `n!username <INSERT USERNAME>`").mentionRepliedUser(false).queue();
                } else {
                    message.reply(user.getName() + " Minecraft Username is `" + username + "`").mentionRepliedUser(false).queue();
                }
            } else {
                String uuid = ServerAPI.getUUID(arguments[1]);
                if (uuid != null) {
                    Var.setMinecraftUUID(member.getUser(), uuid);
                    message.reply("Set `" + arguments[1] + "` to be your Minecraft Username.").mentionRepliedUser(false).queue();
                } else {
                    message.reply("Could not find a Minecraft Account under that Username!").mentionRepliedUser(false).queue();
                }
            }
        } else {
            message.reply("Not enough arguments!").mentionRepliedUser(false).queue();
        }
    }

}
