package Command;

import Bot.Var;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class SetProfile {

    public SetProfile(Message message) {
        String[] messageSplit = message.getContentStripped().split("\\s+");
        if (messageSplit.length > 1) {
            List<Integer> profileFields = new ArrayList<>();
            for (String keyword : messageSplit[1].split(",")) {
                switch (keyword) {
                    case "days" -> profileFields.add(0);
                    case "weeks" -> profileFields.add(1);
                    case "months" -> profileFields.add(2);
                    case "years" -> profileFields.add(3);
                }
            }
            Var.setProfileFields(message.getAuthor(), profileFields);
            Member member = message.getMember();
            if (member == null) {
                message.reply("Profile Updated").queue();
            } else {
                new Profile(message.getMember(), message.getChannel());
            }
            return;
        }
        new ProfileHelp(message);
    }

}
