package Game.BlackOps3;

import Bot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class Player {

    Member member;
    long memberId;
    int score;
    int kills;
    int headshots;
    int downs;
    int revives;

    boolean mostScore = false;
    boolean mostKills = false;
    boolean mostHeadshots = false;
    boolean mostRevives = false;
    boolean leastDowns = false;

    int roundSurvivedTo;

    int payment;

    private final static int PAYMENT_ROUND_SURVIVED = 4;
    private final static int PAYMENT_ROUND_SURVIVED_PER_PLAYER = 1;
    private final static int PAYMENT_SCORE = 115;
    private final static int PAYMENT_SCORE_PER_PLAYER = 5;
    private final static int PAYMENT_KILLS = 8;
    private final static int PAYMENT_KILLS_PER_PLAYER = 2;
    private final static int PAYMENT_HEADSHOTS = 8;
    private final static int PAYMENT_HEADSHOTS_PER_PLAYER = 1;
    private final static int AWARD_MOST_KILLS = 10;
    private final static int AWARD_MOST_HEADSHOTS = 20;
    private final static int AWARD_MOST_SCORE = 10;
    private final static int AWARD_MOST_REVIVES = 5;
    private final static int AWARD_LEAST_DOWNS = 15;

    public Player(String raw) {
        String[] rawSplit = raw.split(",");
        this.memberId   =     Long.parseLong(   rawSplit[0]         );
        try { this.score      =   Integer.parseInt(     rawSplit[1] + "0"   ); } catch (NumberFormatException e) { this.score = -1; }
        try { this.kills      =   Integer.parseInt(     rawSplit[2]         ); } catch (NumberFormatException e) { this.kills = -1; }
        try { this.downs      =   Integer.parseInt(     rawSplit[3]         ); } catch (NumberFormatException e) { this.downs = -1; }
        try { this.revives    =   Integer.parseInt(     rawSplit[4]         ); } catch (NumberFormatException e) { this.revives = -1; }
        try { this.headshots  =   Integer.parseInt(     rawSplit[5]         ); } catch (NumberFormatException e) { this.headshots = -1; }
        if (rawSplit.length > 6) this.roundSurvivedTo = Integer.parseInt(rawSplit[6]);
    }

    public Member getMember() {
        if (member == null) {
            member = Main.getNoctori().getMemberById(memberId);
        }
        return member;
    }

    public long getMemberId() {
        return memberId;
    }

    public int getScore() {
        return score;
    }

    public int getKills() {
        return kills;
    }

    public int getHeadshots() {
        return headshots;
    }

    public int getDowns() {
        return downs;
    }

    public int getRevives() {
        return revives;
    }

    public EmbedBuilder generatePayment(int numPlayers) {
        EmbedBuilder embed = new EmbedBuilder();
        getMember();
        embed.setTitle("Black Ops 3 Zombies");
        if (member != null) {
            embed.setAuthor(member.getEffectiveName(),member.getEffectiveAvatarUrl(),member.getEffectiveAvatarUrl());
            embed.addField("Rounds Survived", "$" + generatePaymentRoundsSurvived(numPlayers), true);
            if (score > 0) embed.addField("Score", "$" + generatePaymentScore(numPlayers), true);
            if (kills > 0) embed.addField("Kills", "$" + generatePaymentKills(numPlayers), true);
            if (headshots > 0) embed.addField("Headshots", "$" + generatePaymentHeadshots(numPlayers), true);
            if (mostScore) embed.addField("Most Score\nAward", "$" + generateAwardMostScore(numPlayers),true);
            if (mostKills) embed.addField("Most Kills\nAward", "$" + generateAwardMostKills(numPlayers),true);
            if (mostHeadshots) embed.addField("Most Headshots\nAward", "$" + generateAwardMostHeadshots(numPlayers),true);
            if (mostRevives) embed.addField("Most Revives\nAward", "$" + generateAwardMostRevives(numPlayers),true);
            if (leastDowns) embed.addField("Least Downs\nAward", "$" + generateAwardLeastDowns(numPlayers),true);
        }
        embed.setDescription("Received $" + payment + "\nfor playing a game");
        return embed;
    }

    public int genericPayment(int pay) {
        payment += pay;
        return pay;
    }

    public int generatePaymentRoundsSurvived(int numPlayers) {
        int pay = (roundSurvivedTo * (PAYMENT_ROUND_SURVIVED + (PAYMENT_ROUND_SURVIVED_PER_PLAYER * numPlayers)));
        payment += pay;
        return pay;
    }

    public int generatePaymentScore(int numPlayers) {
        int pay = (getScore()/(PAYMENT_SCORE + PAYMENT_SCORE_PER_PLAYER * numPlayers));
        return genericPayment(pay);
    }

    public int generatePaymentKills(int numPlayers) {
        int pay = (getKills()/(PAYMENT_KILLS + (PAYMENT_KILLS_PER_PLAYER * numPlayers)));
        return genericPayment(pay);
    }

    public int generatePaymentHeadshots(int numPlayers) {
        int pay = (getHeadshots()/(PAYMENT_HEADSHOTS + PAYMENT_HEADSHOTS_PER_PLAYER * numPlayers));
        return genericPayment(pay);
    }

    public int generateAwardMostScore(int numPlayers) {
        int pay = (AWARD_MOST_SCORE + (AWARD_MOST_SCORE * numPlayers));
        return genericPayment(pay);
    }

    public int generateAwardMostKills(int numPlayers) {
        int pay = (AWARD_MOST_KILLS + (AWARD_MOST_KILLS * numPlayers));
        return genericPayment(pay);
    }

    public int generateAwardMostHeadshots(int numPlayers) {
        int pay = (AWARD_MOST_HEADSHOTS + (AWARD_MOST_HEADSHOTS * numPlayers));
        return genericPayment(pay);
    }

    public int generateAwardMostRevives(int numPlayers) {
        int pay = (AWARD_MOST_REVIVES + (AWARD_MOST_REVIVES * numPlayers));
        return genericPayment(pay);
    }

    public int generateAwardLeastDowns(int numPlayers) {
        int pay = (AWARD_LEAST_DOWNS + (AWARD_LEAST_DOWNS * numPlayers));
        return genericPayment(pay);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        if (score <= -1) string.append("?"); else string.append(score);
        string.append(" Score\n");

        if (kills <= -1) string.append("?"); else string.append(kills);
        string.append(" Kill"); if (kills != 1) string.append("s"); string.append("\n");

        if (headshots <= -1) string.append("?"); else string.append(headshots);
        string.append(" Headshot"); if (headshots != 1) string.append("s"); string.append("\n");

        if (downs <= -1) string.append("?"); else string.append(downs);
        string.append(" Down"); if (downs != 1) string.append("s"); string.append("\n");

        if (revives <= -1) string.append("?"); else string.append(revives);
        string.append(" Revive"); if (revives != 1) string.append("s"); string.append("\n");

        if (roundSurvivedTo != 0) {
            string.append("Quit on Round ").append(roundSurvivedTo);
        }

        return string.toString();

    }
}
