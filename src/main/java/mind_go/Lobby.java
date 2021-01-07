package mind_go;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import static mindustry.Vars.state;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.maps.Map;

public class Lobby {

    public static boolean inLobby = false;
    public static Seq<Room> rooms;
    public static Map nextMap;
    public static void init() {
        nextMap = loadRandomMap();
        rooms = new Seq();
        rooms.add(new Room(Class.Main, "[#dba463]|Basic|Type|", 5, 18));
        rooms.add(new Room(Class.Spiders, "[#bc4a9b]|Spider|Type|", 18, 5));
        rooms.add(new Room(Class.Support, "[#9cdb43]|Support|Type|", 31, 18));
    }

    public static void update() {
        for (Player player : Groups.player) {
            String text = "You are pick: [accent]Nothing";
            for (Room room : rooms) {
                if (room.check(player)) /* Check Player In Room */ {
                    text = "You are pick: [accent]" + room.name;
                    Main.data.get(player).unit = room.classa;
                }
            }
            // Show how much time to start
            text += "\n[white]Time to start: [accent]" + (int) ((Main.lobbyTimer - Main.timer) / 60);
            Call.setHudText(text);
        }
        
        for (Room room : rooms) {
            room.update();
        }
    }

    public static void go() {
        // Set LobbyState to in Lobby
        Lobby.inLobby = true;

        // Add Players In 'players' Seq
        Seq<Player> players = new Seq<>();

        for (Player p : Groups.player) {
            players.add(p);
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
        // Set Lobby State
        Lobby.inLobby = false;
        
        // Update GameTier
        Type.tier = Mathf.random(0, 4);
        
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
    }

    public static Map loadRandomMap(Map oldMap) {
        // Get Random Map
        Map map = Vars.maps.getNextMap(Gamemode.survival, Vars.state.map);
        // Try To Load Map Again If Map Name Equals Shop
        if (map != null && map.name().equals("lobby")) {
            // Haha Let's GO Start Again
            System.out.println("FUCK LOBBY");
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
        Call.label("[white]Next Map is: [accent]" + nextMap.name() + "\n[white]Author is: [accent]" + nextMap.author(), 99999, centreX, centreY);
        for (Room room : rooms) /* show text in centre room */ {
            Call.label(player.con, room.name, 99999, room.centreX, room.centreY);
        }
    }
    
    public static void spawnUnits() {
        for (Room room : rooms) {
            Unit unit = Type.get(room.classa).create(Team.sharded);
            unit.set(room.centreX, room.centreY);
            unit.add();
            room.unit = unit;
        }
    }
}
