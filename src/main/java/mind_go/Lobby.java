package mind_go;

import Events.EventState;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;

import static mind_go.Main.bundle;
import static mindustry.Vars.state;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Legsc;
import mindustry.gen.Mechc;
import mindustry.gen.Payloadc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.gen.WaterMovec;
import mindustry.maps.Map;
import mindustry.world.blocks.payloads.BuildPayload;

public class Lobby {

    public static boolean inLobby = false;
    public static Seq<Room> rooms;
    public static Map nextMap;

    public static void init() {
        nextMap = loadRandomMap();
        rooms = new Seq<>();
        rooms.add(new Room(Class.Main, bundle.get("room.main"), 8, 29)); // centre left
        rooms.add(new Room(Class.Support, bundle.get("room.support"), 50, 29)); // centre right
        rooms.add(new Room(Class.Naval, bundle.get("room.naval"), 17, 5)); // bottom left
        rooms.add(new Room(Class.Spiders, bundle.get("room.spider"), 41, 5)); // bottom right
        rooms.add(new Room(Class.Air, bundle.get("room.air"), 17, 53)); // top left
        rooms.add(new Room(Class.AirSupport, bundle.get("room.airsupport"), 41, 53)); // right top
    }

    public static void update() {
        for (Player player : Groups.player) {
            Class unit = EventState.get("onlys", "water_only_") ? Class.Naval : EventState.get("onlys", "air_only_") ? Class.Air : Class.Main;
            String text = bundle.get("lobby.nopick");
            for (Room room : rooms) {
                if (unit == room.classa && room.active) {
                    Main.data.get(player).unit = unit;
                    text = bundle.get("lobby.default") + room.name;
                }
                if (room.check(player) && room.active) /* Check Player In Room */ {
                    text = bundle.get("lobby.pick") + room.name;
                    Main.data.get(player).unit = room.classa;
                }
            }
            // Show how much time to start
            text += bundle.get("lobby.timer") + ((Main.lobbyTimer - Main.timer) / 60);
            Call.setHudText(player.con(), text);
        }

        for (Room room : rooms) {
            room.update();
        }
    }

    public static void go() {
        Log.debug(bundle.get("debug.lobby"));

        // Set LobbyState to in Lobby
        Main.timer = 0;
        PlayerData.resetValues();
        Lobby.inLobby = true;

        // Update GameTier
        Type.oldTier = Type.tier;
        Type.tier = Type.changeTier();

        // Events Region
        EventState.generate(Mathf.random(4));

        // Switched To Night?
        Main.rules.lighting = false;
        Main.rules.enemyLights = true;

        // Events Region End
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

        Call.setRules(Vars.state.rules);

    }

    public static void out() {
        Log.debug(bundle.get("debug.lobbyx2"));

        // Set Lobby State
        Main.timer = 0;
        Lobby.inLobby = false;

        // Day Night Cycle
        if (EventState.get("weather", "cycle")) {
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

    public static Map loadRandomMap() {
        // Get Random Map
        Map map = Vars.maps.getNextMap(Gamemode.survival, Vars.state.map);
        // Try To Load Map Again If Map Name Equals Shop
        if (map != null && (map.name().equals("lobby") || map.name().equals("Lobby"))) {
            // Haha Let's GO Start Again
            //System.out.println("FUCK LOBBY");
            return loadRandomMap();
        }
        if (EventState.get("gamemode", "meat")) {
            if (map.width > 100 || map.height > 100) {
                return loadRandomMap();
            }
        } else if (!EventState.get("onlys", "free_for_all_")) {
            if (Type.tier < 3 && (map.width > 100 || map.height > 100)) {
                return loadRandomMap();
            } else if (Type.tier >= 3 && (map.width < 100 || map.height < 100)) {
                return loadRandomMap();
            }
        }

        return map;
    }

    public static void showShopText(Player player) {
        float centreX = Vars.world.width() / 2 * Vars.tilesize;
        float centreY = Vars.world.height() / 2 * Vars.tilesize;

        String text = bundle.format("lobby.nmap", nextMap.name())
                + bundle.format("lobby.author", nextMap.author())
                + bundle.format("lobby.mapsize", nextMap.width, nextMap.height);

        if (EventState.get("onlys", "free_for_all_")) {
            EventState.replace("onlys", "ground_only_", true);
        }

        for (String only : EventState.events[0]) {
            if (nextMap.tags.get(only).equals("true")) {
                switch (only) {
                    case "water_only_":
                        EventState.replace("onlys", "water_only_", true);
                        break;
                    case "ground_only_":
                        EventState.replace("onlys", "ground_only_", true);
                        break;
                    case "free_for_all_":
                        EventState.replace("onlys", "free_for_all_", true);
                        EventState.replace("gamemode", "meat", false);
                        EventState.replace("gamemode", "boss", false);
                        break;
                    case "air_only_":
                        EventState.replace("onlys", "air_only_", true);
                        break;
                }
            }
        }
        // Events Bundle
        for (int i = 0; i < EventState.categ.length; i++) {
            String cate = EventState.categ[i];
            for (String even : EventState.events[i]) {
                if (EventState.get(cate, even)) {
                    text += bundle.get("event." + cate + "." + even);
                }
            }
        }

        Call.label(player.con, text, 99999, centreX, centreY);
        for (Room room : rooms) /* show text in centre room */ {
            String textt = room.active ? "" : bundle.get("lobby.disable"); // that only for WaterRooms xd
            if (room.classa == Class.Air || room.classa == Class.AirSupport) {
                textt += bundle.get("lobby.live");
            }
            Call.label(player.con, room.name + textt, 99999, room.centreX, room.centreY - Vars.tilesize * 4.8f);
        }
    }

    public static void spawnUnits() {
        for (Room room : rooms) {
            room.active = true;
            Unit unit = Type.get(room.classa).create(Team.sharded);
            unit.set(room.centreX, room.centreY);

            // If Map has Water
            if (unit instanceof WaterMovec && nextMap.tags.getBool("hasLiquid")) {
                unit.tileOn().setFloorNet(Blocks.water);
            } else if (unit instanceof WaterMovec) {
                room.active = false;
                continue;
            }

            // Event Water|Air|Ground Only Region
            // Water Only
            if ((room.classa == Class.Naval) && nextMap.tags.getBool("water_only_")) {
                room.active = false;
                continue;
            }

            // Ground Only
            if ((room.classa == Class.Air || room.classa == Class.AirSupport) && (nextMap.tags.getBool("ground_only_") || nextMap.tags.getBool("free_for_all_"))) {
                room.active = false;
                continue;
            }

            // Air Only
            if ((room.classa != Class.Air || room.classa != Class.AirSupport) && nextMap.tags.getBool("air_only_")) {
                room.active = false;
                continue;
            }

            // Event Water|Air|Ground Only Region End
            // Unit Region
            if (Type.tier == 0 && room.classa == Class.AirSupport) /* Mono With Thorium Reactor */ {
                unit.type = UnitTypes.mono;
                unit.addItem(Items.thorium, unit.type.itemCapacity);

                Payloadc s = (Payloadc) unit;
                s.addPayload(new BuildPayload(Blocks.thoriumReactor, unit.team));
            }

            if (Type.tier == 0 && room.classa == Class.Spiders) /* Crawler With Blast Compound */ {
                unit.addItem(Items.blastCompound, unit.type.itemCapacity);
            }

            unit.add();
            room.unit = unit;
        }
    }
}
