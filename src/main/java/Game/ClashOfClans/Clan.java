package Game.ClashOfClans;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Clan {
    //WarLeague warLeague;
    //CapitalLeague capitalLeague;
    //List<ClanMember> memberList;
    String tag;
    String warFrequency;
    boolean isWarLogPublic;
    int clanLevel;
    int warWinStreak;
    int warWins;
    int warTies;
    int warLosses;
    int clanPoints;
    int requiredTownhallLevel;
    //Language chatLanguage;
    boolean isFamilyFriendly;
    int clanBuilderBasePoints;
    int clanVersusPoints;
    int clanCapitalPoints;
    int requiredTrophies;
    int requiredBuilderBaseTrophies;
    int requiredVersusTrophies;
    //List<Label> labels;
    String name;
    //Location location;
    String type;
    int members;
    String description;
    //ClanCapital clanCapital;
    HashMap<String, String> badgeUrls;

    // Getters
    public String getTag() {
        return tag;
    }

    public String getWarFrequency() {
        return warFrequency;
    }

    public boolean isWarLogPublic() {
        return isWarLogPublic;
    }

    public int getClanLevel() {
        return clanLevel;
    }

    public int getWarWinStreak() {
        return warWinStreak;
    }

    public int getWarWins() {
        return warWins;
    }

    public int getWarTies() {
        return warTies;
    }

    public int getWarLosses() {
        return warLosses;
    }

    public int getClanPoints() {
        return clanPoints;
    }

    public int getRequiredTownhallLevel() {
        return requiredTownhallLevel;
    }

    public boolean isFamilyFriendly() {
        return isFamilyFriendly;
    }

    public int getClanBuilderBasePoints() {
        return clanBuilderBasePoints;
    }

    public int getClanVersusPoints() {
        return clanVersusPoints;
    }

    public int getClanCapitalPoints() {
        return clanCapitalPoints;
    }

    public int getRequiredTrophies() {
        return requiredTrophies;
    }

    public int getRequiredBuilderBaseTrophies() {
        return requiredBuilderBaseTrophies;
    }

    public int getRequiredVersusTrophies() {
        return requiredVersusTrophies;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getMembers() {
        return members;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String, String> getBadgeUrls() {
        return badgeUrls;
    }

    // Setters
    void setTag(String tag) {
        this.tag = tag;
    }

    void setWarFrequency(String warFrequency) {
        this.warFrequency = warFrequency;
    }

    void setWarLogPublic(boolean warLogPublic) {
        isWarLogPublic = warLogPublic;
    }

    void setClanLevel(int clanLevel) {
        this.clanLevel = clanLevel;
    }

    void setWarWinStreak(int warWinStreak) {
        this.warWinStreak = warWinStreak;
    }

    void setWarWins(int warWins) {
        this.warWins = warWins;
    }

    void setWarTies(int warTies) {
        this.warTies = warTies;
    }

    void setWarLosses(int warLosses) {
        this.warLosses = warLosses;
    }

    void setClanPoints(int clanPoints) {
        this.clanPoints = clanPoints;
    }

    void setRequiredTownhallLevel(int requiredTownhallLevel) {
        this.requiredTownhallLevel = requiredTownhallLevel;
    }

    void setFamilyFriendly(boolean familyFriendly) {
        isFamilyFriendly = familyFriendly;
    }

    void setClanBuilderBasePoints(int clanBuilderBasePoints) {
        this.clanBuilderBasePoints = clanBuilderBasePoints;
    }

    void setClanVersusPoints(int clanVersusPoints) {
        this.clanVersusPoints = clanVersusPoints;
    }

    void setClanCapitalPoints(int clanCapitalPoints) {
        this.clanCapitalPoints = clanCapitalPoints;
    }

    void setRequiredTrophies(int requiredTrophies) {
        this.requiredTrophies = requiredTrophies;
    }

    void setRequiredBuilderBaseTrophies(int requiredBuilderBaseTrophies) {
        this.requiredBuilderBaseTrophies = requiredBuilderBaseTrophies;
    }

    void setRequiredVersusTrophies(int requiredVersusTrophies) {
        this.requiredVersusTrophies = requiredVersusTrophies;
    }

    void setName(String name) {
        this.name = name;
    }

    void setType(String type) {
        this.type = type;
    }

    void setMembers(int members) {
        this.members = members;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setBadgeUrls(HashMap<String, String> badgeUrls) {
        this.badgeUrls = badgeUrls;
    }

    public MessageEmbed toEmbed() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(getName());
        embed.setDescription(getDescription());
        embed.setThumbnail(getBadgeUrls().get("medium"));

        embed.addField("Clan Level", String.valueOf(getClanLevel()), true);
        embed.addField("Clan Points", String.valueOf(getClanPoints()), true);
        embed.addBlankField(true);

        embed.addField("War Wins", String.valueOf(getWarWins()), true);
        embed.addField("War Ties", String.valueOf(getWarTies()), true);
        embed.addField("War Losses", String.valueOf(getWarLosses()), true);

        return embed.build();
    }

}
