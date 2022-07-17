package Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class ProfileHelp {

    public ProfileHelp(Message message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Profile Help");
        embed.setDescription("Use `n!setprofile` and follow with comma seperated keywords.  You can put in any order you wish.");
        embed.addField("Possible Keywords", """
                days
                weeks
                months
                years
                """, true);
        embed.addField("Examples", """
                `n!setprofile days`
                `n!setprofile months,days`
                `n!setprofile years,weeks,months`
                """,true);
        message.replyEmbeds(embed.build()).queue();
    }

}
