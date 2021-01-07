/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

/**
 *
 * @author Xusk
 */
public class GameLogic {

    public static int gameOverTimer = 300,
            teamID = 1,
            timer = 0;
    public static Team winnerTeam = Team.derelict;
    public static boolean once = false,
            gameOver = false;

    public static void update() {
        if (gameOver == true) {
            timer++;
            if (once) {
                gameOver(winnerTeam);
                once = false;
            }
            
            if (timer > gameOverTimer) {
                timer = 0;
                gameOver = false;
                Lobby.go();
            }
        } else {
            once = true;
        }
        if (Main.shardedPlayers < 1) {
            gameOver = true;
            winnerTeam = Team.blue;
        } else if (Main.bluePlayers < 1) {
            gameOver = true;
            winnerTeam = Team.sharded;
        }
    }
    
    public static void gameOver(Team team) {
        String text = "Winner Team is: ";
        if (team == Team.sharded) {
            text += "[orange]Orange";
        } else if (team == Team.blue) {
            text += "[blue]Blue";
        } else /* no Winner Team*/ {
            text = "[gray]No Winner Team";
        }
        Call.infoMessage(text);
    }
    
    public static void start(float sx, float sy, float bx, float by) /* sharded, blue */ {
        for (Player player : Groups.player) {
            // Get Data From Hash Map
            PlayerData data = Main.data.get(player);
            // Team Changer 
            teamID = -teamID;
            // Pick Team
            Team team = teamID > 0 ? Team.sharded : Team.blue;
            // Create Unit
            Unit unit = Type.get(data.unit).create(team);
            // Set Unit to core position
            unit.set(unit.team() == Team.sharded ? sx : bx, unit.team() == Team.sharded ? sy : by);
            // Add Unit
            unit.add();
            // Set Unit To The Player
            player.team(team);
            player.unit(unit);
        }
    }
}
