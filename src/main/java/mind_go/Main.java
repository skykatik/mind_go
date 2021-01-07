package mind_go;

import arc.Events;
import arc.util.CommandHandler;
import java.util.HashMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
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

public class Main extends Plugin {

    public static int afterLoadTimer = 150,
            timer = 0,
            lobbyTimer = 60 * 60 / 4,
            gameTimer = 60 * 60 / 4;
    public static HashMap<Player, PlayerData> data = new HashMap<>();
    boolean debug = false,
            loaded = false,
            worldLoaded = false;
    public static int shardedPlayers = 0, bluePlayers = 0;

    @Override
    public void init() {
        // Some Stuff Init
        super.init();
        Lobby.init();
        Type.init();

        // Rules Stuff Here
        for (Block block : Vars.content.blocks()) {
            Vars.state.rules.bannedBlocks.add(block);
        }

        Vars.state.rules.canGameOver = false;

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
                        Lobby.go();
                        timer = 0;
                    }

                    shardedPlayers = 0;
                    bluePlayers = 0;
                    for (Player player : Groups.player) /* Set Hud To Players */ {
                        int health = (int) (100 - ((player.unit().maxHealth - player.unit().health) / (player.unit().maxHealth / 100)));
                        Call.setHudText(player.con, "Game end in: " + (int) ((gameTimer - timer) / 60) + "\nYour Health is: [red]" + health + "%");
                        if (player.team() == Team.blue) {
                            bluePlayers++;
                        } else if (player.team() == Team.sharded) {
                            shardedPlayers++;
                        }
                    }
                    GameLogic.update();
                }
            }
        });

        // World Load
        Events.on(EventType.WorldLoadEvent.class, event -> {
            clearOre();

            // SetDefaultVariable
            loaded = true;
            worldLoaded = true;
        });

        // Player Join
        Events.on(EventType.PlayerJoin.class, event -> {
            // Add PlayerData To The Server
            data.put(event.player, new PlayerData(event.player));

            if (Lobby.inLobby) /* show text */ {
                Lobby.showShopText(event.player);
            }
        });

        // Player unjoin
        Events.on(EventType.PlayerLeave.class, event -> {
            data.remove(event.player);
        });

        // Server Load
        Events.on(EventType.ServerLoadEvent.class, event -> {
        });
    }

    // TODO: do sothing with that lol
    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("start", "START THIS FUCKING GAME AAAAAAA", args -> {
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
            );
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
                tile.setNet(Blocks.thoriumWall, Team.get(947), 0); // My love Number :Ç
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

    public void debug() {
        for (Room room : Lobby.rooms) /* Draw Debug Square With Bullets */{
            room.debugDraw();
        }
    }
}
