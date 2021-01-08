package mind_go;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import static mindustry.Vars.state;
import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Legsc;
import mindustry.gen.Mechc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.gen.WaterMovec;
import mindustry.maps.Map;

public class Lobby {

    public static boolean inLobby = false;
    public static Seq<Room> rooms;
    public static Map nextMap;

    public static void init() {
        nextMap = loadRandomMap();
        rooms = new Seq();
        rooms.add(new Room(Class.Main, "[#dba463]|Basic|Type|", 8, 29)); // centre left
        rooms.add(new Room(Class.Support, "[#9cdb43]|Support|Type|", 50, 29)); // centre right
        rooms.add(new Room(Class.Naval, "[sky]|Naval|Type|", 17, 5)); // bottom left
        rooms.add(new Room(Class.Spiders, "[#bc4a9b]|Spider|Type|", 41, 5)); // bottom right
        rooms.add(new Room(Class.Air, "[#a6fcdb]|Air|Type|", 17, 53)); // top left
        rooms.add(new Room(Class.AirSupport, "[#92dcba]|AirSupport|Type|", 41, 53)); // right top
    }

    public static void update() {
        for (Player player : Groups.player) {
            String text = "You pick: [accent]Nothing";
            for (Room room : rooms) {
                if (room.check(player) && room.active) /* Check Player In Room */ {
                    text = "You pick: [accent]" + room.name;
                    Main.data.get(player).unit = room.classa;
                }
            }
            // Show how much time to start
            text += "\n[white]Time to start: [accent]" + (int) ((Main.lobbyTimer - Main.timer) / 60);
            Call.setHudText(player.con(), text);
        }

        for (Room room : rooms) {
            room.update();
        }
    }

    public static void go() {
        // DEBUG
        if (Main.debug) {
            System.out.println("GO in Lobby");
        }

        // Set LobbyState to in Lobby
        Main.timer = 0;
        Lobby.inLobby = true;

        // Update GameTier
        Type.tier = Mathf.random(0, 4);

        // Switched To Night?
        Main.rules.lighting = false;
        Main.rules.enemyLights = true;
        Main.cycle = Mathf.random(100) > 10;

        // Mines
        Main.mines = Mathf.random(100) > 70;

        // Add Players In 'players' Seq
        Seq<Player> players = new Seq<>();

        for (Player p : Groups.player) {
            players.add(p);
            Main.data.get(p).unita = null;
            p.team(Team.sharded);
            p.clearUnit();
        }

        // Logic Reset Start
        Vars.logic.reset();

        // World Load Start
        Call.worldDataBegin();
        Vars.world.loadMap(Vars.maps.byName("lobby"));
        // World Load End

        // Rules Load
        Vars.state.rules = Main.rules.copy();

        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }

    }

    public static void out() {
        // DEBUG
        if (Main.debug) {
            System.out.println("Out From Lobby");
        }
        // Set Lobby State
        Main.timer = 0;
        Lobby.inLobby = false;

        // Day Night Cycle
        if (!Main.cycle) {
            Main.rules.lighting = true;
            Main.rules.ambientLight = new Color(0, 0, 0, 1);
            Main.rules.enemyLights = false;
        } else {
            Main.rules.lighting = false;
            Main.rules.ambientLight = new Color(1, 1, 1, 1);
            Main.rules.enemyLights = true;
        }

        // Write Players Array
        Seq<Player> players = new Seq<>();

        for (Player p : Groups.player) {
            players.add(p);
            p.clearUnit();
        }

        // Logic Reset Start
        Vars.logic.reset();

        // World Data Start
        Call.worldDataBegin();
        Vars.world.loadMap(nextMap);
        nextMap = loadRandomMap();
        state.rules = Main.rules.copy();
        // World Data End

        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }

        // Null All Units
        for (Room room : rooms) {
            room.unit = null;
        }
        Call.setRules(Main.rules);
    }

    public static Map loadRandomMap(Map oldMap) {
        // Get Random Map
        Map map = Vars.maps.getNextMap(Gamemode.survival, Vars.state.map);
        // Try To Load Map Again If Map Name Equals Shop
        if (map != null && map.name().equals("lobby")) {
            // Haha Let's GO Start Again
            //System.out.println("FUCK LOBBY");
            return loadRandomMap(map);
        }
        return map;
    }

    public static Map loadRandomMap() {
        return loadRandomMap(Vars.state.map);
    }

    public static void showShopText(Player player) {
        float centreX = Vars.world.width() / 2 * Vars.tilesize;
        float centreY = Vars.world.height() / 2 * Vars.tilesize;
        String text = "[white]Next Map is: [accent]" + nextMap.name() + "\n[white]Author is: [accent]" + nextMap.author();
        if (!Main.cycle) {
            text += "\nNight Now";
        }
        if (Main.mines) {
            text += "\n[gray]|Mines in this round|";
        }

        Call.label(player.con, text, 99999, centreX, centreY);
        for (Room room : rooms) /* show text in centre room */ {
            String textt = room.active ? "" : "\n[red]Disabled [white]: on this map"; // that only for WaterRooms xd
            if (room.classa == Class.Air || room.classa == Class.AirSupport) {
                textt += "\n[crimson]|Unit Live|\n|100 secs|";
            }
            Call.label(player.con, room.name + textt, 99999, room.centreX, room.centreY - Vars.tilesize * 4.8f);
        }
    }

    public static void spawnUnits() {
        for (Room room : rooms) {
            room.active = true;
            Unit unit = Type.get(room.classa).create(Team.sharded);
            unit.set(room.centreX, room.centreY);

            //Water Units Only
            if (unit instanceof WaterMovec && nextMap.tags.get("hasLiquid").equals("true")) {
                unit.tileOn().setFloorNet(Blocks.water);
            } else if (unit instanceof WaterMovec) {
                room.active = false;
                continue;
            }

            if ((unit instanceof Mechc || unit instanceof Legsc) && nextMap.tags.get("waterOnly").equals("true")) {
                room.active = false;
                continue;
            }

            unit.add();
            room.unit = unit;
        }
    }
}
