package Game.BlackOps3;

import Bot.Main;
import net.dv8tion.jda.api.entities.Member;

public class Player {

    Member member;
    long memberId;
    int score = 0;
    int kills = 0;
    int headshots = 0;
    int downs = 0;
    int revives = 0;

    public Player(String raw) {
        String[] rawSplit = raw.split(",");
        this.memberId   =     Long.parseLong(   rawSplit[0]         );
        this.score      =   Integer.parseInt(   rawSplit[1] + "0"   );
        this.kills      =   Integer.parseInt(   rawSplit[2]         );
        this.downs      =   Integer.parseInt(   rawSplit[3]         );
        this.revives    =   Integer.parseInt(   rawSplit[4]         );
        this.headshots  =   Integer.parseInt(   rawSplit[5]         );
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
        string.append(score).append(" Score\n");
        string.append(kills).append(" Kill");
        if (kills != 1) string.append("s"); string.append("\n");
        string.append(headshots).append(" Headshot");
        if (headshots != 1) string.append("s"); string.append("\n");
        string.append(downs).append(" Down");
        if (downs != 1) string.append("s"); string.append("\n");
        string.append(getRevives()).append(" Revive");
        if (revives != 1) string.append("s"); string.append("\n");
        return string.toString();
    }
}
