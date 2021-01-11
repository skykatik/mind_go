/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import Events.EventState;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Mechc;
import mindustry.gen.Nulls;
import mindustry.gen.Player;
import mindustry.gen.Unit;

import static mind_go.Main.bundle;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.gen.Payloadc;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.BuildPayload;

/**
 *
 * @author Xusk
 */
public class GameLogic {

    public static int FREE_ID = 100;
    public static Player WinnerPlayer;
    public static int gameOverTimer = 300,
            teamID = 1,
            timer = 0;
    public static Team winnerTeam = Team.derelict;
    public static boolean onceGameOver = false,
            unitSpawned = false,
            gameOver = false;

    public static void update() {
        if (unitSpawned) {
            if (gameOver == true) /* Timer for non instant translate to the lobby */ {
                timer++;
                if (onceGameOver) /* show message with winner team */ {
                    gameOver(winnerTeam);
                    onceGameOver = false;
                }
                if (timer > gameOverTimer && !Lobby.inLobby) /* go to lobby when time out */ {
                    timer = 0;
                    gameOver = false;
                    unitSpawned = false;
                    if (Main.debug) {
                        Log.info(bundle.get("debug.logic"));
                    }
                    Lobby.go();
                }
            } else /* set when not gameOver */ {
                onceGameOver = true;
            }

            if (EventState.get("gamemode", "boss")) {
                if (PlayerData.boss != null) {
                    PlayerData.boss.player.unit().damagePierce(PlayerData.boss.player.unit().maxHealth / 1000 / 10f);
                }
            }
            if (Groups.unit.size() > 0) {
                Unit lastUnit = Nulls.unit;
                boolean end = true;
                Team lastTeam = Groups.unit.index(0).team;
                for (Unit unit : Groups.unit) {
                    Team team = unit.team;
                    if (unit.isFlying()) {
                        unit.damagePierce(unit.maxHealth / 1000 / (unit instanceof Mechc ? 4f : 12f));
                    }
                    if (unit instanceof Payloadc && unit.type == UnitTypes.mono) {
                        Payloadc s = (Payloadc) unit;
                        if (s.payloads().size <= 0) {
                            unit.damagePierce(unit.maxHealth / 1000 / 0.7f);
                        }
                    }
                    if (team != lastTeam) {
                        end = false;
                        lastUnit = unit;
                    }
                    lastTeam = unit.team;
                }

                if (end) {
                    winnerTeam = lastTeam;
                    gameOver = true;
                    if (lastUnit != null && lastUnit.isPlayer()) {
                        WinnerPlayer = lastUnit.getPlayer();
                    }
                }
            } else if (Groups.player.size() > 0) /* Team Counter */ {
                winnerTeam = Team.derelict;
                gameOver = true;
            }

        }

    }

    public static void gameOver(Team team) {
        String text;
        if (!EventState.get("onlys", "free_for_all_")) {
            text = bundle.get("game.win");
            if (team == Team.sharded) {
                text += bundle.get("game.team.sharded");
            } else if (team == Team.blue) {
                text += bundle.get("game.team.blue");
            } else /* no Winner Team*/ {
                text = bundle.get("game.nowin");
            }
        } else {
            text = bundle.get("game.winplayer");
            if (WinnerPlayer != null) {
                text += WinnerPlayer.name();
            }
        }
        // DEBUG
        if (Main.debug) {
            System.out.println(text);
        }

        Call.infoMessage(text);
    }

    public static void start(float sx, float sy, float bx, float by) {
        if (EventState.get("gamemode","boss")) {
            selectBoss();
        }
        for (Player player : Groups.player) {
            player.unit(Nulls.unit);
            // Team
            Team team;
            // Get Data From Hash Map
            PlayerData data = Main.data.get(player);
            if (!EventState.get("gamemode","boss")) {
                // Team Changer 
                teamID = -teamID;
                // Pick Team
                team = teamID > 0 ? Team.sharded : Team.blue;
            } else {
                if (data.isBoss) {
                    team = Team.blue;
                } else {
                    team = Team.sharded;
                }
            }

            // Create Unit
            Unit unit;
            if (!EventState.get("gamemode","boss")) {
                if (!EventState.get("onlys", "free_for_all_")) {
                    unit = Type.get(data.unit).create(team);
                } else {
                    unit = Type.get(data.unit).create(Team.sharded);
                    FREE_ID++;
                    if (FREE_ID > 999) FREE_ID = 100; // lol random
                }
            } else {
                if (data.isBoss) {
                    unit = Type.get(data.unit, Type.tier + 1).create(team);
                    unit.maxHealth = unit.type.health * (Groups.player.size() / 5);
                    unit.health = unit.maxHealth;
                } else {
                    unit = Type.get(data.unit).create(team);
                }
            }

            // Set Unit Position
            if (EventState.get("gamemode","boss")) {
                Tile tile = randomTile();
                unit.set(tile.drawx(), tile.drawy());
                
            } else {
                unit.set(unit.team() == Team.sharded ? sx : bx, unit.team() == Team.sharded ? sy : by);
            }

            // Add Thorium Reactor to mono
            if (Type.tier == 0 && data.unit == Class.AirSupport && !Main.data.get(player).isBoss) /* Mono With Thorium Reactor */ {
                unit.type = UnitTypes.mono;
                unit.addItem(Items.thorium, unit.type.itemCapacity);
                if (unit instanceof Payloadc) {
                    Payloadc s = (Payloadc) unit;
                    s.addPayload(new BuildPayload(Blocks.thoriumReactor, unit.team));
                }
            }
            // Add Blast Compound to crawler
            if (Type.tier == 0 && data.unit == Class.Spiders) /* Crawler With Blast Compound */ {
                unit.addItem(Items.blastCompound, unit.type.itemCapacity);
            }

            // Add Unit
            unit.add();

            if (EventState.get("onlys", "free_for_all_")) {
                player.team(Team.get(FREE_ID));
            } else {
                player.team(team);
            }
            // Set Unit To The Player
            data.unita = unit;
            player.unit(unit);
        }
        
        unitSpawned = true;
    }
    
    public static void start() {
        start(Main.sx, Main.sy, Main.bx, Main.by);
    }

    public static void selectBoss() {
        if (Groups.player.size() > 0) {
            Player p = Groups.player.index(Mathf.random(Groups.player.size() - 1));
            PlayerData dat = Main.data.get(p);
            dat.isBoss = true;
            PlayerData.boss = dat;
            Call.sendMessage(dat.player.name() + bundle.get("event.boss.fight"));
        }
    }
    
    public static Tile randomTile() {
        Tile tile = Vars.world.tile(Mathf.random(Vars.world.width(), Vars.world.height()), Mathf.random(Vars.world.width(), Vars.world.height()));
        if (tile == null) return randomTile();
        if (tile.build != null) return randomTile();
        if (tile.floor().isLiquid) return randomTile();
        return tile;
    }
}
