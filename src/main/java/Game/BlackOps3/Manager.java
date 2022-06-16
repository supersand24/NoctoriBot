package Game.BlackOps3;

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
        NACHT_DER_UNTOTEN(          "nachtDerUntoten",          "Nacht Der Untoten",            false),
        VERRUCKT(                   "verruckt",                 "Verruckt",                     false),
        SHI_NO_NUMA(                "shiNoNuma",                "Shi No Numa",                  false),
        THE_GIANT(                  "theGiant",                 "The Giant",                    false),
        DER_RIESE_DECLASSIFIED(     "derRieseDeclassified",     "Der Riese: Declassified",      true),
        KINO_DER_TOTEN(             "kinoDerToten",             "Kino Der Toten",               false),
        ASCENSION(                  "ascension",                "Ascension",                    false),
        SHANGRI_LA(                 "shangriLa",                "Shangri-La",                   false),
        MOON(                       "moon",                     "Moon",                         false),
        TOWN_REIMAGINED(            "townReimagined",           "Town Reimagined",              true),
        FARM(                       "farm",                     "Farm",                         true),
        DAYBREAK(                   "daybreak",                 "Daybreak",                     true),
        DIE_RISE_SLIQUIFIER_FLOOR(  "dieRiseSliquifierFloor",   "Die Rise (Sliquifier Floor)",  true),
        DIE_RISE_ROOF(              "dieRiseRoof",              "Die Rise (Roof)",              true),
        BURIED_CHALLENGE(           "buriedChallenge",          "Buried Challenge",             true),
        ASTORIA(                    "astoria",                  "Astoria",                      true),
        ORIGINS(                    "origins",                  "Origins",                      false),
        SHADOWS_OF_EVIL(            "shadowsOfEvil",            "Shadows of Evil",              false),
        DER_EISENDRACHE(            "derEisendrache",           "Der Eisendrache",              false),
        ZETSUBOU_NO_SHIMA(          "zetsubouNoShima",          "Zetsubou No Shima",            false),
        GOROD_KROVI(                "gorodKrovi",               "Gorod Krovi",                  false),
        REVELATIONS(                "revelations",              "Revelations",                  false),
        NACHT_DER_AGONIE(           "nachtDerAgonie",           "Nacht Der Agonie",             true),
        KOWLOON(                    "kowloon",                  "Kowloon",                      true),
        LEVIATHAN(                  "leviathan",                "Leviathan",                    true),
        KYASSURU(                   "kyassuru",                 "Kyassuru",                     true),
        GRIT(                       "grit",                     "Grit",                         true),
        COVE(                       "cove",                     "Cove",                         true),
        BLOCKADE(                   "blockade",                 "Blockade",                     true),
        VOID_EXPANSE(               "voidExpanse",              "Void Expanse",                 true),
        DOME_NUKETOWN_REIMAGINED(   "domeNuketownReimagined",   "Dome (Nuketown Reimagineed)",  true),
        DER_FUHRERBUNKER(           "derFuhrerbunker",          "Der Führerbunker",             true),
        SHUGEKI(                    "shugeki",                  "Shugeki",                      true),
        DEAD_HIGH(                  "deadHigh",                 "Dead High",                    true),
        SLOG(                       "slog",                     "Slog",                         true),
        GATEKEEPER(                 "gatekeeper",               "Gatekeeper",                   true);

        private final String fileName;
        private final String mapName;
        private final boolean moddedMap;

        Map(String fileName, String mapName, boolean moddedMap) {
            this.fileName = fileName;
            this.mapName = mapName;
            this.moddedMap = moddedMap;
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

}
