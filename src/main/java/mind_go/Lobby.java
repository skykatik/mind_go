package mind_go;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

public class Lobby {

    public static boolean inLobby = false;
    public static Seq<Room> rooms;

    public static void init() {
        rooms = new Seq();
        rooms.add(new Room(Class.Main, "[#dba463]MainType", 5, 18));
        rooms.add(new Room(Class.Spiders, "[#bc4a9b]SpiderType", 18, 5));
        rooms.add(new Room(Class.Support, "[#9cdb43]SupportType", 31, 18));
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
            text += "\n[white]Time to start: [accent]" + (int) ((Main.gameTimer - Main.timer) / 60);
            Call.setHudText(text);
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
        loadRandomMap();
        // World Data End

        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
    }

    public static void loadRandomMap() {
        // Get Random Map
        Vars.world.loadMap(Vars.maps.getNextMap(Gamemode.survival, Vars.state.map), Vars.state.rules);
        // Try To Load Map Again If Map Name Equals Shop
        if (Vars.state.map.name().equals("lobby")) {
            // Haha Let's GO Start Again
            loadRandomMap();
        }
    }

    public static void showShopText(Player player) {
        if (player.team().core() != null) {
            Call.label(player.con, "", 99999, player.team().core().tileX(), player.team().core().tileY());
        }

        for (Room room : rooms) /* show text in centre room */ {
            Call.label(player.con, room.name, 99999, room.centreX, room.centreY);
        }
    }
    
    public static void spawnUnits() {
        for (Room room : rooms) {
            
        }
    }
}
