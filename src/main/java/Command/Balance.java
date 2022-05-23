package Command;

import Bot.Var;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class Balance {

    public Balance(User user, MessageChannel channel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bank Account");
        embed.setAuthor(user.getName(),user.getEffectiveAvatarUrl(),user.getEffectiveAvatarUrl());
        embed.addField("Noctori Bucks", "$" + Var.getMoney(user), false);
        channel.sendMessageEmbeds(embed.build()).queue();
    }

}
