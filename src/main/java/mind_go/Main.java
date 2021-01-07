/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import arc.Events;
import arc.util.CommandHandler;
import mindustry.game.EventType;
import mindustry.mod.Plugin;
/**
 *
 * @author Xusk
 */
public class Main extends Plugin {

    @Override
    public void init() {
        super.init();
        
        // Update Trigger
        Events.on(EventType.Trigger.update.getClass(), event -> {
        
        });
        
        // World Load
        Events.on(EventType.WorldLoadEvent.class, event -> {
            
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
