package Command;

import Bot.Var;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class Profile {

    public Profile(Member member, MessageChannel messageChannel) {
        //Save Variables
        User user = member.getUser();

        //Create the Builder
        EmbedBuilder embed = new EmbedBuilder();

        //Place Avatar Picture
        embed.setThumbnail(member.getAvatarUrl());

        //Insert Name
        embed.setTitle(member.getEffectiveName());
        if (!user.getName().equals(member.getEffectiveName())) embed.setDescription(user.getAsTag());

        //Server Booster
        if (member.isBoosting()) embed.setAuthor("SERVER BOOSTER!",
                "https://support.discord.com/hc/en-us/articles/360039337992-Server-Boosting-Buy-a-Level",
                "https://c.tenor.com/fgHKt1DNMdIAAAAi/nitro-discord.gif"
        );

        //Date Joined Timestamp
        LocalDate dateJoined = member.getTimeJoined().toLocalDate();
        embed.setFooter("Date Joined");
        embed.setTimestamp(OffsetDateTime.of(dateJoined, LocalTime.NOON, ZoneOffset.UTC));

        //Add various fields dependent on Users Preference
        for (int field : Var.getProfileFields(user)) {
            switch (field) {
                case 0 -> embed.addField("Days in Noctori",String.valueOf(ChronoUnit.DAYS.between(dateJoined,LocalDate.now())),true);
                case 1 -> embed.addField("Weeks in Noctori",String.valueOf(ChronoUnit.WEEKS.between(dateJoined,LocalDate.now())),true);
                case 2 -> embed.addField("Months in Noctori",String.valueOf(ChronoUnit.MONTHS.between(dateJoined,LocalDate.now())),true);
                case 3 -> embed.addField("Years in Noctori",String.valueOf(ChronoUnit.YEARS.between(dateJoined,LocalDate.now())),true);
                default -> embed.addBlankField(true);
            }
        }

        //Send Message
        messageChannel.sendMessageEmbeds(embed.build()).queue();
    }

}
