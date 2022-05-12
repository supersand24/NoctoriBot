package Game.BlackOps3;

import Bot.Main;
import net.dv8tion.jda.api.entities.Member;

public class Player {

    Member member;
    long memberId;
    int score;
    int kills;
    int headshots;
    int downs;
    int revives;

    int roundSurvivedTo;

    public Player(String raw) {
        String[] rawSplit = raw.split(",");
        this.memberId   =     Long.parseLong(   rawSplit[0]         );
        try { this.score      =   Integer.parseInt(     rawSplit[1] + "0"   ); } catch (NumberFormatException e) { this.score = -1; }
        try { this.kills      =   Integer.parseInt(     rawSplit[2]         ); } catch (NumberFormatException e) { this.kills = -1; }
        try { this.downs      =   Integer.parseInt(     rawSplit[3]         ); } catch (NumberFormatException e) { this.downs = -1; }
        try { this.revives    =   Integer.parseInt(     rawSplit[4]         ); } catch (NumberFormatException e) { this.revives = -1; }
        try { this.headshots  =   Integer.parseInt(     rawSplit[5]         ); } catch (NumberFormatException e) { this.headshots = -1; }
        if (rawSplit.length > 6) {
            this.roundSurvivedTo = Integer.parseInt(rawSplit[6]);
        }
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
