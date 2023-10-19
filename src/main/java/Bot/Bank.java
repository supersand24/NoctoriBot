package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Bank {

    private final static Logger log = LoggerFactory.getLogger(Bank.class);

    private final static int PAY_PER_MONTH = 5;
    private final static int PAY_SERVER_BOOSTER = 70;
    private final static int PAY_SERVER_BOOSTER_PER_MONTH = 2;

    public static void daily(Member member) {
        log.debug("Local Date now " + LocalDate.now());
        log.debug("Read from DB " + Var.getDailyClaimed(member));
        if ( LocalDate.now().compareTo(Var.getDailyClaimed(member)) > 0) {
            log.debug("true");
        } else {
            log.debug("false");
        }

        if ( LocalDate.now().compareTo(Var.getDailyClaimed(member)) > 0) {
            log.info(member.getEffectiveName() + " has logged in for the day.");
            anniversary(member);
            User user = member.getUser();
            Var.updateDailyClaimed(member);
            if (Var.getPaymentNotification(user)) {
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

        Var.addMoney(member,total);
        return embed.build();
    }

    public static void payMember(int amount, Member from, Member to) {
        int fromAccount =  Var.getMoney(from);
        int toAccount = Var.getMoney(to);
        if (fromAccount >= amount) {
            fromAccount -= amount; toAccount += amount;
            Var.setMoney(from, fromAccount);
            Var.setMoney(to, toAccount);
            from.getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("You received " + amount + " Noctori Bucks from " + from.getEffectiveName() +".\nYour new balance is " + Var.getMoney(to) + " Noctori Bucks.")
            );
        } else {
            from.getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("You tried to pay " + to.getEffectiveName() + " " + amount + " Noctori Bucks, but you do not have enough.\nYour account has " + Var.getMoney(from) + " Noctori Bucks." ).queue()
            );
        }
    }

    public static void anniversary(Member member) {
        int yearsJoined = OffsetDateTime.now().getYear() - member.getTimeJoined().getYear();
        Role role = getYearRole(yearsJoined);
        if (!member.getRoles().contains(role)) {
            log.info("Updating " + member.getEffectiveName() + " Yearly Role.");
            Main.getNoctori().addRoleToMember(member,role).queue();
            for (int i = 0; i < yearsJoined; i++) {
                Role removeRole = getYearRole(i);
                log.debug("Removing " + removeRole.getName() + " Role.");
                Main.getNoctori().removeRoleFromMember(member,removeRole).queue();
            }
        }
    }

    private static Role getYearRole(int year) {
        StringBuilder string = new StringBuilder();
        string.append(year).append(" Year"); if (year != 1) string.append("s");
        return Main.getNoctori().getRolesByName(string.toString(),true).get(0);
    }

}
