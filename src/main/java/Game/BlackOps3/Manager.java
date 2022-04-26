package Game.BlackOps3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Manager {

    private final static Logger log = LoggerFactory.getLogger(Manager.class);
    private final static HashMap<Record, Map> leaderboards = new HashMap<>();

    public enum Map {
        NACHT_DER_UNTOTEN("nachtDerUntoten",false),
        VERRUCKT("verruckt",false),
        SHI_NO_NUMA("shiNoNuma",false),
        THE_GIANT("theGiant",false),
        DER_RIESE_DECLASSIFIED("derRieseDeclassified",true),
        KINO_DER_TOTEN("kinoDerToten",false),
        ASCENSION("ascension",false),
        SHANGRI_LA("shangriLa",false),
        MOON("moon",false),
        TOWN_REIMAGINED("townReimagined",true),
        FARM("farm",true),
        DAYBREAK("daybreak",true),
        DIE_RISE_SLIQUIFIER_FLOOR("dieRiseSliquifierFloor",true),
        DIE_RISE_ROOF("dieRiseRoof",true),
        BURIED_CHALLENGE("buriedChallenge",true),
        ORIGINS("origins",false),
        SHADOWS_OF_EVIL("shadowsOfEvil",false),
        DER_EISENDRACHE("derEisendrache",false),
        ZETSUBOU_NO_SHIMA("zetsubouNoShima",false),
        GOROD_KROVI("gorodKrovi",false),
        REVELATIONS("revelations",false),
        NACHT_DER_AGONIE("nachtDerAgonie",true),
        KOWLOON("kowloon",true),
        LEVIATHAN("leviathan",true),
        KYASSURU("kyassuru",true),
        GRIT("grit",true),
        COVE("cove",true),
        BLOCKADE("blockade",true);

        private final String fileName;
        private final boolean moddedMap;

        Map(String fileName, boolean moddedMap) {
            this.fileName = fileName;
            this.moddedMap = moddedMap;
        }

        public String getFileName() {
            return fileName + ".records";
        }

        public boolean getModdedMap() {
            return moddedMap;
        }

    }

    public enum Mod {
        VANILLA(0),
        ZE_LITE(1),
        ZE_COMPLETE(2),
        ALL_AROUND_ENHANCEMENT(3);

        private final int index;

        Mod(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static void parseData(Map map) {
        try {
            List<String> rawRecords = Files.readAllLines(Paths.get("leaderboards/blackOps3/" + map.getFileName()));

            for (String rawRecord : rawRecords) {
                System.out.println(rawRecord);
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
                        leaderboards.put(
                                new Record(map, mod,
                                        Integer.parseInt(splitRecord[0]),
                                        players
                                ),
                                map
                        );
                    }
                    case ZE_LITE, ZE_COMPLETE -> {
                        String[] roundSet = splitRecord[0].split("-");
                        leaderboards.put(
                                new Record(map, mod,
                                        Integer.parseInt(roundSet[0]),
                                        Integer.parseInt(roundSet[1]),
                                        players
                                ),
                                map
                        );
                    }
                }
            }

            System.out.println(leaderboards);

        } catch (NoSuchFileException e) {
            log.error("Could not find the " + map.getFileName() + " file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mod getModFromRaw(String str) {
        for (Mod mod : Mod.values()) {
            if (str.startsWith(String.valueOf(mod.index))) {
                return mod;
            }
        }
        return Mod.VANILLA;
    }

}
