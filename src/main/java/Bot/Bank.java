package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Bank {

    private final static Logger log = LoggerFactory.getLogger(Bank.class);

    private final static int PAY_PER_MONTH = 5;
    private final static int PAY_SERVER_BOOSTER = 70;
    private final static int PAY_SERVER_BOOSTER_PER_MONTH = 2;

    public static void daily(Member member) {
        if ( LocalDate.now().compareTo(Var.getDailyClaimed(member.getUser())) > 0) {
            log.info(member.getEffectiveName() + " has logged in for the day.");
            User user = member.getUser();
            Var.updateDailyClaimed(user);
            if (Var.getNotification(user)) {
                user.openPrivateChannel().queue(privateChannel -> {
                   privateChannel.sendMessageEmbeds( payDaily(member) ).queue();
                });
            }
        }
    }

    public static MessageEmbed payDaily(Member member) {
        int total = 0;
        long payTemp = 0;
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor(member.getEffectiveName(),member.getEffectiveAvatarUrl(),member.getEffectiveAvatarUrl());
        embed.setTitle("Pay Stub");
        embed.setFooter("Use n!notification to disable these.");

        //Months
        Period timeJoined = Period.between(
                member.getTimeJoined().toLocalDate(),
                LocalDate.now()
        );
        payTemp = Math.max(0,timeJoined.toTotalMonths() * PAY_PER_MONTH);
        total += payTemp;
        embed.addField("Months","$" + payTemp,true);

        //Server Boosting
        if (member.isBoosting()) {
            total += PAY_SERVER_BOOSTER;
            embed.addField("Server Booster", "$" + PAY_SERVER_BOOSTER, true);
            Period timeBoosted = Period.between(
                    member.getTimeBoosted().toLocalDate(),
                    LocalDate.now()
            );
            payTemp = Math.max(0,timeBoosted.toTotalMonths() * PAY_SERVER_BOOSTER_PER_MONTH);
            total += payTemp;
            embed.addField("Months Server Boosted", "$" + payTemp, true);
        }

        embed.setDescription("You were paid $" + total + " for being active in Noctori on " + LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) + ".");

        Var.addMoney(member.getUser(),total);
        return embed.build();
    }

}
