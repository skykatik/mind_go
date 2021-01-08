package mind_go;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.CommandHandler;
import arc.util.Log;
import java.util.HashMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.logic;
import static mindustry.Vars.state;
import mindustry.content.UnitTypes;
import mindustry.gen.Mechc;
import mindustry.maps.Map;

public class Main extends Plugin {

    public static int afterLoadTimer = 150,
            timer = 0,
            lobbyTimer = 60 * 60 / 2,
            gameTimer = 60 * 60 * 5;

    public static HashMap<Player, PlayerData> data = new HashMap<>();

    public static boolean debug = false,
            loaded = false,
            mines = true,
            cycle = true,
            timerSeted = false,
            worldLoaded = false;

    public static Rules rules = new Rules();

    @Override
    public void init() {
        // Some Stuff Init
        super.init();
        Lobby.init();
        Type.init();
        initStats();

        // Rules Stuff Here
        for (Block block : Vars.content.blocks()) {
            rules.bannedBlocks.add(block);
        }
        rules.lighting = true;
        rules.ambientLight = new Color(1,1,1,1);
        rules.waves = true;
        rules.waveTimer = false;
        rules.blockDamageMultiplier = 0.1f;
        rules.blockHealthMultiplier = 4f;
        rules.canGameOver = false;

        // Rules Stuff Initialize
        Call.setRules(Vars.state.rules);
        // Rules Stuff End

        // Update Trigger
        Events.on(EventType.Trigger.update.getClass(), event -> {
            // Timer oh no
            timer++;
            // Once after load event, when player join they are have a "connection state", and have chance 50% for not show a Call.label 
            if (loaded) {
                afterLoadTimer--;
                if (afterLoadTimer <= 0) {
                    if (Lobby.inLobby) /* Lobby Once */ {
                        Lobby.spawnUnits();
                        Groups.player.each(player -> /* Show Text To Player */ {
                                    Lobby.showShopText(player);
                                });
                        if (debug) /* Debug Stuff */ {
                            debug();
                        }
                    } else /* Game Once */ {
                        initGame();
                    }
                    afterLoadTimer = 150;
                    loaded = false;
                    timerSeted = true;
                }
            }

            // Update here
            if (worldLoaded) {
                if (Lobby.inLobby) /* Lobby Logic */ {
                    Lobby.update();
                    if (timer > lobbyTimer) {
                        Lobby.out();
                        timer = 0;
                    }
                } else /* Game Logic */ {
                    if (timer > gameTimer && !GameLogic.gameOver) /* Go to Lobby when Timer out*/ {

                        // DEBUG
                        if (debug) {
                            System.out.println("Time Out, switch to the Lobby");
                        }

                        Lobby.go();
                        timer = 0;
                    }

                    for (Player player : Groups.player) /* Set Hud To Players */ {
                        String text;
                        int health = (int) (100 - ((player.unit().maxHealth - player.unit().health) / (player.unit().maxHealth / 100)));
                        text = "Game end in: " + (int) ((gameTimer - timer) / 60) + "\nYour Health is: [red]" + health + "%";
                        if (health < 6) {
                            player.unit().kill();
                        }
                        if (player.unit() instanceof Mechc && player.unit().isFlying()) /* If unit fly then they get Danger Hud */ {
                            text += "\n[crimson]Fly = Dead";
                        }
                        Call.setHudText(player.con, text);
                    }

                    if (timerSeted && timer > afterLoadTimer + 20) {
                        GameLogic.update();

                        Groups.player.each(player -> {
                            PlayerData date = data.get(player);
                            if (date.unita != null && date.unita.dead == true) {
                                date.unita = null;
                            }
                            if (date.unita != null && !date.unita.isPlayer()) {
                                player.unit(date.unita);
                            }
                        });
                    }
                }
            }
        });

        // World Load
        Events.on(EventType.WorldLoadEvent.class, event -> {
            clearOre();

            // SetDefaultVariable
            timerSeted = false;
            loaded = true;
            worldLoaded = true;
        });

        // Player Join
        Events.on(EventType.PlayerJoin.class, event -> {
            // Add PlayerData To The Server
            data.put(event.player, new PlayerData(event.player));

            if (Lobby.inLobby) /* show text */ {
                Lobby.showShopText(event.player);
            } else {
                for (Player player : Groups.player) {
                    PlayerData date = data.get(player);
                    if (date.unita != null && date.unita.health > 0 && date.player.name != null && date.player.name.equals(player.name)) {
                        player.unit(date.unita);
                    }
                }
            }
        });

        // Player unjoin
        Events.on(EventType.PlayerLeave.class, event -> {
            data.remove(event.player);
        });

        // Server Load
        Events.on(EventType.ServerLoadEvent.class, event -> {
            Lobby.go();
            Vars.netServer.openServer();
            System.out.println("SERVER IN PLUGIN CONTROL UHAHAHA\nServer Started not write /HOST");

            // set tup maps with liquids to create WaterMovec units
            for (Map map : Vars.maps.all()) {
                if (map.name().startsWith("water_")) {
                    map.tags.put("hasLiquid", "true");
                } else {
                    map.tags.put("hasLiquid", "false");
                }

                if (map.name().startsWith("water_only_")) {
                    map.tags.put("waterOnly", "true");
                } else {
                    map.tags.put("waterOnly", "false");
                }
            }
        });

    }

    // TODO: do sothing with that lol
    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("start", "START THIS FUCKING GAME AAAAAAA", args -> {
            if (!state.is(GameState.State.menu)) {
                Log.err("SHUT UP THE SERVER UHHHHHHH");
                return;
            }

            logic.reset();
            state.rules = rules.copy();
            logic.play();
            Vars.netServer.openServer();

            Lobby.go();
            System.out.println("Let's go");
        });

        handler.register("info", "get some information from stats", args -> {
            System.out.println("Current Map: " + Vars.state.map.name()
                    + "\nCurrent Tier: " + Type.tier
                    + "\nPlayerData's: " + data.size()
                    + "\nRooms In Lobby: " + Lobby.rooms.size
                    + "\nUnits: " + Groups.unit.size()
                    + "\nIn Lobby: " + Lobby.inLobby
                    + "\nNext Map: " + Lobby.nextMap.name()
            );
        });

        handler.register("cycle", "enable/disable day/night cycle", args -> {
            cycle = !cycle;
            System.out.println("switched to: " + cycle);
        });
        
        handler.register("mines", "enable/disable mines on map", args -> {
            mines = !mines;
            System.out.println("switched to: " + mines);
        });
        
        handler.register("debug", "fuck me", args -> {
            debug = !debug;
        });
    }

    @Override
    public void loadContent() {

    }

    public void initGame() {
        float sx = 0, sy = 0, bx = 0, by = 0;

        for (CoreBlock.CoreBuild core : Team.blue.cores()) /* destroy blue cores */ {
            bx = core.x;
            by = core.y;
            core.kill();
        }

        for (CoreBlock.CoreBuild core : Team.sharded.cores()) /* destory sharded cores */ {
            sx = core.x;
            sy = core.y;
            core.kill();
        }

        for (Tile tile : Vars.world.tiles) /* place walls on floor */ {
            if (tile.floor() == (Floor) Blocks.metalFloor5) {
                tile.setNet(Mathf.random(0, 100) > 80 ? Mathf.random(0,100) > 80 ? Blocks.thoriumWall : Blocks.surgeWall : Blocks.plastaniumWall, Team.get(947), 0); // My love Number :Ç
            }
            if (mines) {
                if (tile.block() == Blocks.air && Mathf.random(100) > 98) {
                    tile.setNet(Blocks.shockMine, Team.get(947), 0);
                }
            }
            // Set map to water if map has a water
            if (tile.floor().isLiquid) {
                Vars.state.map.tags.put("hasLiquid", "true");
            }
        }
        GameLogic.start(sx, sy, bx, by);
    }

    public void clearOre() {
        for (Tile tile : Vars.world.tiles) {
            if (tile.overlay() instanceof OreBlock) /* clear ores */ {
                tile.setOverlay(Blocks.air);
            }
        }
    }

    public void initStats() {
        // Init Unit Variables
        UnitTypes.dagger.health = 140 * 2;
        UnitTypes.crawler.health = 180 * 2.5f;
        UnitTypes.mace.health = 500 * 1.7f;
        UnitTypes.fortress.health = 790 * 2;
        UnitTypes.corvus.health = 18000 * 2;
        UnitTypes.toxopid.health = 22000 / 1.5f;

        // Remove Unit Abilities
        UnitTypes.nova.abilities.clear();
        UnitTypes.pulsar.abilities.clear();
        UnitTypes.quasar.abilities.clear();
        UnitTypes.omura.abilities.clear();
    }

    public void debug() {
        for (Room room : Lobby.rooms) /* Draw Debug Square With Bullets */ {
            room.debugDraw();
        }
    }
}
