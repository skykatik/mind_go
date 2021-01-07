package mind_go;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class Lobby {
    
    public static boolean inLobby = false;
    public static Seq<Room> rooms;
    
    public static void init() {
        rooms = new Seq();
        rooms.add(new Room(Class.Main, "MainType", 10, 10));
    }
    
    public static void update() {
        for (Player player : Groups.player) {
            String text = "You are pick: [accent]Nothing";
            for (Room room : rooms) {
                if (room.check(player)) /* Check Player In Room */ {
                    text = "You are pick: " + room.name;
                }
            }
            Call.setHudText(text);
        }
    }
    
    public static void go() {
        // Set LobbyState to in Lobby
        Lobby.inLobby = true;
        
        // Add Players In 'players' Seq
        Seq<Player> players = new Seq<>();
        
        for(Player p : Groups.player) {
            players.add(p);
            p.clearUnit();
        }
        
        // Logic Reset Start
        Vars.logic.reset();
        
        // World Load Start
        Call.worldDataBegin();
        Vars.world.loadMap(Vars.maps.byName("shop"));
        // World Load End
        
        // Logic Reset End
        Vars.logic.play();
        
        // Send World Data To All Players
        for(Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
        
        
    }
    
    public static void out() {
        Lobby.inLobby = false;
        
        // Write Players Array
        Seq<Player> players = new Seq<>();
        
        for(Player p : Groups.player) {
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
        for(Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
    }
    
    public static void loadRandomMap() {
        // Get Random Map
        Vars.world.loadMap(Vars.maps.getNextMap(Gamemode.survival, Vars.state.map), Vars.state.rules);
        // Try To Load Map Again If Map Name Equals Shop
        if (Vars.state.map.name().equals("shop")) {
            // Haha Let's GO Start Again
            loadRandomMap();
        }
    }
    
    public static void showShopText(Player player) {
        Call.label(player.con, "", 99999, player.team().core().tileX(), player.team().core().tileY());
        
        for (Room room : rooms) {
            Call.label(player.con, room.name, 99999, room.centreX, room.centreY);
        }
    }
}
