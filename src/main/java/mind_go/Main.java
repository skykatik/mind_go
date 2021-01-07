package mind_go;

import arc.Events;
import arc.util.CommandHandler;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;

public class Main extends Plugin {

    public static int afterLoadTimer = 100,
            timer = 0,
            lobbyTimer = 60 * 60 / 2,
            gameTimer = 60 * 60 * 5;
    boolean loaded = false,
            worldLoaded = false;

    @Override
    public void init() {
        // Some Stuff Init
        super.init();
        Lobby.init();
        Type.init();
        
        // Update Trigger
        Events.on(EventType.Trigger.update.getClass(), event -> {
            // Timer oh no
            timer++;
            // Once after load event, when player join they are have a "connection state", and have chance 50% for not show a Call.label 
            if (loaded) {
                afterLoadTimer--;
                if (afterLoadTimer <= 0) {
                    if (Lobby.inLobby) /* Lobby Once */ {
                        Groups.player.each(player -> {
                            Lobby.showShopText(player);
                        });
                    } else /* Game Once */ {
                        
                    }
                    afterLoadTimer = 100;
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
                    if (timer > gameTimer) /* Go to Lobby when Timer out*/ {
                        Lobby.go();
                        timer = 0;
                    }
                }
            }
        });

        // World Load
        Events.on(EventType.WorldLoadEvent.class, event -> {
            loaded = true;
            worldLoaded = true;
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

    }

    @Override
    public void loadContent() {

    }

}
