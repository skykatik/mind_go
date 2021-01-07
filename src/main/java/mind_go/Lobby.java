/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.maps.Map;

/**
 *
 * @author Xusk
 */
public class Lobby {
    
    public static boolean inLobby = false;
    
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
}
