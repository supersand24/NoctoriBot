package Game.BlackOps3;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class Manager {

    private final static int steamCollectionID = 883750219;

    private final static Logger log = LoggerFactory.getLogger(Manager.class);

    public enum Map {
        NACHT_DER_UNTOTEN(          "nachtDerUntoten",          "Nacht Der Untoten",            false,  0),
        VERRUCKT(                   "verruckt",                 "Verruckt",                     false,  1),
        SHI_NO_NUMA(                "shiNoNuma",                "Shi No Numa",                  false,  2),
        THE_GIANT(                  "theGiant",                 "The Giant",                    false,  3),
        DER_RIESE_DECLASSIFIED(     "derRieseDeclassified",     "Der Riese: Declassified",      true,   4),
        KINO_DER_TOTEN(             "kinoDerToten",             "Kino Der Toten",               false,  5),
        ASCENSION(                  "ascension",                "Ascension",                    false,  6),
        SHANGRI_LA(                 "shangriLa",                "Shangri-La",                   false,  7),
        MOON(                       "moon",                     "Moon",                         false,  8),
        TOWN_REIMAGINED(            "townReimagined",           "Town Reimagined",              true,   9),
        FARM(                       "farm",                     "Farm",                         true,  10),
        DAYBREAK(                   "daybreak",                 "Daybreak",                     true,  11),
        DIE_RISE_SLIQUIFIER_FLOOR(  "dieRiseSliquifierFloor",   "Die Rise (Sliquifier Floor)",  true,  12),
        DIE_RISE_ROOF(              "dieRiseRoof",              "Die Rise (Roof)",              true,  13),
        BURIED_CHALLENGE(           "buriedChallenge",          "Buried Challenge",             true,  14),
        ASTORIA(                    "astoria",                  "Astoria",                      true,  15),
        ORIGINS(                    "origins",                  "Origins",                      false,  16),
        SHADOWS_OF_EVIL(            "shadowsOfEvil",            "Shadows of Evil",              false,  17),
        DER_EISENDRACHE(            "derEisendrache",           "Der Eisendrache",              false,  18),
        ZETSUBOU_NO_SHIMA(          "zetsubouNoShima",          "Zetsubou No Shima",            false,  19),
        GOROD_KROVI(                "gorodKrovi",               "Gorod Krovi",                  false,  20),
        REVELATIONS(                "revelations",              "Revelations",                  false,  21),
        NACHT_DER_AGONIE(           "nachtDerAgonie",           "Nacht Der Agonie",             true,  22),
        KOWLOON(                    "kowloon",                  "Kowloon",                      true,  23),
        LEVIATHAN(                  "leviathan",                "Leviathan",                    true,  24),
        KYASSURU(                   "kyassuru",                 "Kyassuru",                     true,  25),
        GRIT(                       "grit",                     "Grit",                         true,  26),
        COVE(                       "cove",                     "Cove",                         true,  27),
        BLOCKADE(                   "blockade",                 "Blockade",                     true,  28),
        VOID_EXPANSE(               "voidExpanse",              "Void Expanse",                 true,  29),
        DOME_NUKETOWN_REIMAGINED(   "domeNuketownReimagined",   "Dome (Nuketown Reimagineed)",  true,  30),
        DER_FUHRERBUNKER(           "derFuhrerbunker",          "Der Führerbunker",             true,  31),
        SHUGEKI(                    "shugeki",                  "Shugeki",                      true,  32),
        DEAD_HIGH(                  "deadHigh",                 "Dead High",                    true,  33),
        SLOG(                       "slog",                     "Slog",                         true,  34),
        GATEKEEPER(                 "gatekeeper",               "Gatekeeper",                   true,  35);

        private final String fileName;
        private final String mapName;
        private final boolean moddedMap;
        private final int id;

        Map(String fileName, String mapName, boolean moddedMap, int id) {
            this.fileName = fileName;
            this.mapName = mapName;
            this.moddedMap = moddedMap;
            this.id = id;
        }

        public String getFileName() {
            return fileName + ".records";
        }

        public String getMapName() {
            return mapName;
        }

        public boolean getModdedMap() {
            return moddedMap;
        }

        public int getId() {
            return id;
        }
    }

    public enum Mod {
        VANILLA(0, "Vanilla"),
        ZE_LITE(1, "Zombies Experience: Lite Edition"),
        ZE_COMPLETE(2, "Zombies Experience: Complete Edition"),
        ALL_AROUND_ENHANCEMENT(3, "All-around Enhancement");

        private final int index;
        private final String name;

        Mod(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }

    public static Leaderboard parseData(Map map) {
        Leaderboard leaderboard = new Leaderboard(map);
        try {
            List<String> rawRecords = Files.readAllLines(Paths.get("leaderboards/blackOps3/" + map.getFileName()));
            for (String rawRecord : rawRecords) {
                Mod mod = getModFromRaw(rawRecord);
                rawRecord = rawRecord.substring(2);
                Player[] players;

                String[] splitRecord = rawRecord.split(";");
                players = new Player[splitRecord.length - 1];
                for (int i = 1; i < splitRecord.length; i++) {
                    players[i - 1] = new Player(splitRecord[i]);
                }

                switch (mod) {
                    case VANILLA,ALL_AROUND_ENHANCEMENT -> {
                        leaderboard.addRecord(
                                new Record(map, mod,
                                        Integer.parseInt(splitRecord[0]),
                                        players
                                )
                        );
                    }
                    case ZE_LITE, ZE_COMPLETE -> {
                        String[] roundSet = splitRecord[0].split("-");
                        if (roundSet.length > 1) {
                            leaderboard.addRecord(
                                    new Record(map, mod,
                                            Integer.parseInt(roundSet[0]),
                                            Integer.parseInt(roundSet[1]),
                                            players
                                    )
                            );
                        } else {
                            leaderboard.addRecord(
                                    new Record(map, mod,
                                            Integer.parseInt(roundSet[0]),
                                            players
                                    )
                            );
                        }
                    }
                }
            }

        } catch (NoSuchFileException e) {
            log.error("Could not find the " + map.getFileName() + " file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    public static void sendMapLeaderboard(Message message) {
        String[] messageSplit = message.getContentStripped().split("\\s+");
        if (messageSplit.length > 1) {
            for (Map map : Map.values()) {
                for (int i = 1; i < messageSplit.length; i++) {
                    if (map.mapName.equalsIgnoreCase(messageSplit[i]) || map.fileName.equalsIgnoreCase(messageSplit[i])) {
                        message.getChannel().sendMessageEmbeds(parseData(map).getAsEmbed()).queue();
                        return;
                    }
                }
            }
        }
        message.reply("I don't see a map name.").queue();
    }

    public static Mod getModFromRaw(String str) {
        for (Mod mod : Mod.values()) {
            if (str.startsWith(String.valueOf(mod.index))) {
                return mod;
            }
        }
        return Mod.VANILLA;
    }

    public static String getSteamCollectionURL() {
        return "https://steamcommunity.com/sharedfiles/filedetails/?id=" + steamCollectionID;
    }

    public static MessageEmbed.Field getProfileField(User user, Map map) {
        String title = "Black Ops 3 Zombies\n"+ map.getMapName();
        String desc;
        int pos = parseData(map).getUserPosition(user);
        if (!(pos >= 20)) {
            switch (parseData(map).getUserPosition(user)) {
                case 1 -> desc = "1st Place\nRound Survived";
                case 2 -> desc = "2nd Place\nRound Survived";
                case 3 -> desc = "3rd Place\nRound Survived";
                default -> desc = pos + "th Place\nRound Survived";
            }
        } else {
            desc = "Out of the Top 20!";
        }
        return new MessageEmbed.Field(title,desc,true);
    }

}
