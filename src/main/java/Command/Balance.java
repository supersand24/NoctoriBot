package Command;

import Bot.Var;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class Balance {

    public Balance(User user, MessageChannelUnion channel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bank Account");
        embed.setAuthor(user.getName(),user.getEffectiveAvatarUrl(),user.getEffectiveAvatarUrl());
        embed.addField("Noctori Bucks", "$" + Var.getMoney(user), false);
        channel.sendMessageEmbeds(embed.build()).queue();
    }

}
