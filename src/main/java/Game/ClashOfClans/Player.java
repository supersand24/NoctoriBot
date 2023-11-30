package Game.ClashOfClans;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.LocalDateTime;

public class Player {

    //League league
    //BuilderBaseLeague builderBaseLeague
    //PlayerClan playerClan
    String role;
    String warPreference;
    int attackWins;
    int defenseWins;
    int versusTrophies;
    int bestVersusTrophies;
    int townHallLevel;
    int townHallWeaponLevel;
    int versusBattleWins;
    //PlayerLegendStatus legendStatus
    //PlayerItemLevelList troops
    //PlayerItemLevelList heroes
    //PlayerItemLevelList spells
    //LabelList labels
    String tag;
    String name;
    int expLevel;
    int trophies;
    int bestTrophies;
    int donations;
    int donationsReceived;
    int builderHallLevel;
    int builderBaseTrophies;
    int bestBuilderBaseTrophies;
    int warStars;
    //PlayerAchievementProgressList achievements;
    int clanCapitalContributions;
    //PlayerHouse playerHouse;

    //Getters
    public String getRole() {
        return role;
    }

    public String getWarPreference() {
        return warPreference;
    }

    public int getAttackWins() {
        return attackWins;
    }

    public int getDefenseWins() {
        return defenseWins;
    }

    public int getVersusTrophies() {
        return versusTrophies;
    }

    public int getBestVersusTrophies() {
        return bestVersusTrophies;
    }

    public int getTownHallLevel() {
        return townHallLevel;
    }

    public int getTownHallWeaponLevel() {
        return townHallWeaponLevel;
    }

    public int getVersusBattleWins() {
        return versusBattleWins;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public int getExpLevel() {
        return expLevel;
    }

    public int getTrophies() {
        return trophies;
    }

    public int getBestTrophies() {
        return bestTrophies;
    }

    public int getDonations() {
        return donations;
    }

    public int getDonationsReceived() {
        return donationsReceived;
    }

    public int getBuilderHallLevel() {
        return builderHallLevel;
    }

    public int getBuilderBaseTrophies() {
        return builderBaseTrophies;
    }

    public int getBestBuilderBaseTrophies() {
        return bestBuilderBaseTrophies;
    }

    public int getWarStars() {
        return warStars;
    }

    public int getClanCapitalContributions() {
        return clanCapitalContributions;
    }

    // Setters
    void setRole(String role) {
        this.role = role;
    }

    void setWarPreference(String warPreference) {
        this.warPreference = warPreference;
    }

    void setAttackWins(int attackWins) {
        this.attackWins = attackWins;
    }

    void setDefenseWins(int defenseWins) {
        this.defenseWins = defenseWins;
    }

    void setVersusTrophies(int versusTrophies) {
        this.versusTrophies = versusTrophies;
    }

    void setBestVersusTrophies(int bestVersusTrophies) {
        this.bestVersusTrophies = bestVersusTrophies;
    }

    void setTownHallLevel(int townHallLevel) {
        this.townHallLevel = townHallLevel;
    }

    void setTownHallWeaponLevel(int townHallWeaponLevel) {
        this.townHallWeaponLevel = townHallWeaponLevel;
    }

    void setVersusBattleWins(int versusBattleWins) {
        this.versusBattleWins = versusBattleWins;
    }

    void setTag(String tag) {
        this.tag = tag;
    }

    void setName(String name) {
        this.name = name;
    }

    void setExpLevel(int expLevel) {
        this.expLevel = expLevel;
    }

    void setTrophies(int trophies) {
        this.trophies = trophies;
    }

    void setBestTrophies(int bestTrophies) {
        this.bestTrophies = bestTrophies;
    }

    void setDonations(int donations) {
        this.donations = donations;
    }

    void setDonationsReceived(int donationsReceived) {
        this.donationsReceived = donationsReceived;
    }

    void setBuilderHallLevel(int builderHallLevel) {
        this.builderHallLevel = builderHallLevel;
    }

    void setBuilderBaseTrophies(int builderBaseTrophies) {
        this.builderBaseTrophies = builderBaseTrophies;
    }

    void setBestBuilderBaseTrophies(int bestBuilderBaseTrophies) {
        this.bestBuilderBaseTrophies = bestBuilderBaseTrophies;
    }

    void setWarStars(int warStars) {
        this.warStars = warStars;
    }

    void setClanCapitalContributions(int clanCapitalContributions) {
        this.clanCapitalContributions = clanCapitalContributions;
    }

    public MessageEmbed toEmbed() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(getName());

        embed.addField("Town Hall Level", String.valueOf(getTownHallLevel()), true);
        embed.addField("Builder Hall Level", String.valueOf(getBuilderHallLevel()), true);
        embed.addField("EXP Level", String.valueOf(getExpLevel()), true);

        embed.addField("Trophies", String.valueOf(getTrophies()), true);
        embed.addField("Builder Hall Trophies", String.valueOf(getBuilderBaseTrophies()), true);
        embed.addBlankField(true);

        return embed.build();
    }


}
