package mind_go;

import Events.EventState;
import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.*;
import arc.util.io.Streams;
import com.google.gson.*;
import components.Bundle;
import components.Config;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.storage.CoreBlock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static mindustry.Vars.*;

public class Main extends Plugin {

    public static Config config;
    public static Bundle bundle;

    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static int afterLoadTimer = 150,
            timer = 0,
            lobbyTimer = 60 * 60 / 2,
            gameTimer = 60 * 60 * 5;

    public static HashMap<Player, PlayerData> data = new HashMap<>();

    public static boolean debug = false,
            loaded = false,
            timerSeted = false,
            worldLoaded = false;

    public static Rules rules = new Rules();

    public Main() {

        Fi cfg = dataDirectory.child("config.json");
        if (!cfg.exists()) {
            cfg.writeString(gson.toJson(config = new Config()));
            Log.info("Config created");
        } else {
            config = gson.fromJson(cfg.reader(), Config.class);
        }

        bundle = new Bundle();

        try {
            Streams.copy(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("Lobby.msav")),
                    customMapDirectory.child("Lobby.msav").write(false));
        } catch (IOException e) {
            Log.err(e);
        }
    }

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
        rules.ambientLight = new Color(1, 1, 1, 1);
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
                            Log.info(bundle.get("game.timeout"));
                        }

                        Lobby.go();
                        timer = 0;
                    }

                    for (Player player : Groups.player) /* Set Hud To Players */ {
                        String text;
                        int health = (int) (100 - ((player.unit().maxHealth - player.unit().health) / (player.unit().maxHealth / 100)));
                        text = bundle.get("game.timer") + (int) ((gameTimer - timer) / 60) + bundle.get("game.health") + health + "%";
                        if (health < 6) {
                            player.unit().kill();
                        }
                        if (player.unit() instanceof Mechc && player.unit().isFlying()) /* If unit fly then they get Danger Hud */ {
                            text += bundle.get("game.fly");
                        }
                        Call.setHudText(player.con, text);
                    }

                    if (timerSeted && timer > afterLoadTimer + 20) {
                        GameLogic.update();

                        // return player again in unit if it try to out from unit
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
            Log.info(bundle.get("server.start"));

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

    // TODO: do something with that lol
    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {

        handler.register("reload-cfg", bundle.get("commands.cfg.description"), args -> {
            config = gson.fromJson(dataDirectory.child("config.json").readString(), Config.class);
            Log.info(bundle.get("commands.cfg.result"));
        });

        handler.register("start", bundle.get("commands.start.description"), args -> {
            if (!state.is(GameState.State.menu)) {
                Log.err("commands.start.error");
                return;
            }

            logic.reset();
            state.rules = rules.copy();
            logic.play();
            Vars.netServer.openServer();
            Lobby.go();
            Log.info(bundle.get("commands.start.successful"));
        });

        handler.register("stats", bundle.get("commands.info.description"), args -> {
            Log.info(bundle.get("commands.info.map") + Vars.state.map.name()
                    + bundle.get("commands.info.tier") + Type.tier
                    + bundle.get("commands.info.data") + data.size()
                    + bundle.get("commands.info.rooms") + Lobby.rooms.size
                    + bundle.get("commands.info.units") + Groups.unit.size()
                    + bundle.get("commands.info.inlobby") + Lobby.inLobby
                    + bundle.get("commands.info.nmap") + Lobby.nextMap.name()
            );
        });

        handler.register("cycle", bundle.get("commands.cycle.description"), args -> {
            EventState.cycle = !EventState.cycle;
            Log.info(bundle.get("commands.cycle.switch") + EventState.cycle);
        });

        handler.register("mines", bundle.get("commands.mines.description"), args -> {
            EventState.mines = !EventState.mines;
            Log.info(bundle.get("commands.mines.switch") + EventState.mines);
        });

        handler.register("debug", bundle.get("commands.debug.description"), args -> {
            debug = !debug;
            Log.info(bundle.get("commands.debug.start"));
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
                tile.setNet(Mathf.random(0, 100) > 30 ? Mathf.random(0, 100) > 30 ? Blocks.thoriumWall : Blocks.surgeWall : Blocks.plastaniumWall, Team.get(947), 0); // My love Number :�
            }
            if (EventState.mines) {
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
        UnitTypes.mono.health = 1000f;
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
