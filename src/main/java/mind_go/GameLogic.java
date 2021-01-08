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
        if (gameOver == true) /* Timer for non instant translate to the lobby */{
            timer++;
            if (once) /* show message with winner team */ {
                gameOver(winnerTeam);
                once = false;
            }

            if (timer > gameOverTimer && !Lobby.inLobby) /* go to lobby when time out */ {
                timer = 0;
                gameOver = false;
                Lobby.go();
            }
        } else /* set when not gameOver */ {
            once = true;
        }
        // Set to 0 commands players
        int shardedPlayers = 0,
                bluePlayers = 0;
        
        // Get how many units live and add teamPoint
        for (Unit unit : Groups.unit) {
            if (unit.health >= 0) {
                if (unit.team() == Team.sharded) {
                    shardedPlayers++;
                } else if (unit.team() == Team.blue) {
                    bluePlayers++;
                }
            }
            if (unit.isFlying()) {
                unit.damagePierce(unit.maxHealth / 1000 / 1.5f);
            }
        }
        if (Main.debug) /* debug, print counter */ {
            System.out.println(bluePlayers + " : " + shardedPlayers);
        }
        // Game State
        if (shardedPlayers <= 0) {
            gameOver = true;
            winnerTeam = Team.blue;
        } else if (bluePlayers <= 0) {
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

    /**
     * 
     * @param sx sharded core x
     * @param sy sharded core y
     * @param bx blue core x
     * @param by blue core y
     */
    public static void start(float sx, float sy, float bx, float by) {
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
            data.unita = unit;
            player.team(team);
            player.unit(unit);
        }
    }
}
