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

    public static void daily(User user) {
        if ( LocalDate.now().compareTo(Var.getDailyClaimed(user)) > 0) {
            log.info(user.getName() + " has logged in for the day.");
            Var.updateDailyClaimed(user);
            if (Var.getNotification(user)) {
                user.openPrivateChannel().queue(privateChannel -> {
                   privateChannel.sendMessageEmbeds( payDaily(Main.getNoctori().getMemberById(user.getId())) ).queue();
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
            embed.addField("Server Booster", "$" + PAY_SERVER_BOOSTER, true);
            Period timeBoosted = Period.between(
                    member.getTimeBoosted().toLocalDate(),
                    LocalDate.now()
            );
            total += Math.max(0,timeBoosted.toTotalMonths() * PAY_SERVER_BOOSTER_PER_MONTH);
            embed.addField("Months Server Boosted", "$" + PAY_SERVER_BOOSTER_PER_MONTH, true);
        }

        embed.setDescription("You were paid $" + total + " for being active in Noctori on " + LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) + ".");

        Var.addMoney(member.getUser(),total);
        return embed.build();
    }

}
