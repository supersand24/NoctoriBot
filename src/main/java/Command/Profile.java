package Command;

import Bot.Main;
import Bot.Var;
import Game.BlackOps3.Manager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Profile {

    public static void sendProfile(User user, MessageChannel messageChannel) {
        //Find Member
        Member member = Main.getNoctori().getMemberById(user.getId());
        if (member != null) {

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

            //Set Color
            embed.setColor(member.getColorRaw());

            //Add various fields dependent on Users Preference
            for (int field : Var.getProfileFields(user)) {
                embed.addField(makeField(member,field));
            }

            //Send Message
            messageChannel.sendMessageEmbeds(embed.build()).queue();
            return;
        }
        messageChannel.sendMessage("Unable to find Member in Server.").queue();
    }

    public static MessageEmbed.Field makeField(Member member, int field) {
        LocalDate dateJoined = member.getTimeJoined().toLocalDate();
        switch (field) {
            case 0 -> {
                return new MessageEmbed.Field("Days in Noctori", String.valueOf(ChronoUnit.DAYS.between(dateJoined, LocalDate.now())), true);
            }
            case 1 -> {
                return new MessageEmbed.Field("Weeks in Noctori", String.valueOf(ChronoUnit.WEEKS.between(dateJoined, LocalDate.now())), true);
            }
            case 2 -> {
                return new MessageEmbed.Field("Months in Noctori", String.valueOf(ChronoUnit.MONTHS.between(dateJoined, LocalDate.now())), true);
            }
            case 3 -> {
                return new MessageEmbed.Field("Years in Noctori", String.valueOf(ChronoUnit.YEARS.between(dateJoined, LocalDate.now())), true);
            }
            default -> {
                if (field >= 100 && field < 200) {
                    for (Manager.Map map : Manager.Map.values()) {
                        if (field == map.getId() + 100) {
                            return Manager.getProfileField(member.getUser(), map);
                        }
                    }
                }
                return new MessageEmbed.Field("", "", true);
            }
        }
    }

    public static void help(Message message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Profile Help");
        embed.setDescription("Use `n!setprofile` and follow with comma separated keywords.  You can put in any order you wish.");
        embed.addField("Possible Keywords", """
                days
                weeks
                months
                years
                bo3z:<MAPNAME>
                """, true);
        embed.addField("Examples", """
                `n!setprofile days`
                `n!setprofile months,days`
                `n!setprofile years,weeks,months`
                `n!setprofile weeks,bo3z:nachtDerUntoten`
                `n!setprofile bo3z:kinoDerToten,bo3z:origins`
                """,true);
        message.replyEmbeds(embed.build()).queue();
    }

    public static void set(Message message) {
        String[] messageSplit = message.getContentStripped().split("\\s+");
        if (messageSplit.length > 1) {
            List<Integer> profileFields = new ArrayList<>();
            for (String keyword : messageSplit[1].split(",")) {
                if (keyword.startsWith("bo3z:")) {
                    for (Manager.Map map : Manager.Map.values()) {
                        if (map.getFileName().contains(keyword.substring("bo3z:".length()))) {
                            profileFields.add(100 + map.getId());
                        }
                    }
                }
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
                sendProfile(message.getAuthor(), message.getChannel());
            }
            return;
        }
        help(message);
    }

}

