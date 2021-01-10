/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Events;

import arc.graphics.Color;
import arc.math.Mathf;
import java.util.HashMap;
import mind_go.Main;
import mind_go.Type;
import mindustry.Vars;
import mindustry.content.Weathers;
import mindustry.type.Weather;

/**
 *
 * @author Xusk
 */
public class EventState {
    public static HashMap<String, Boolean> map = new HashMap<>();
    
    // FOR INITIALIZE ON SERVER START
    public static String[] onlys = new String[] {
        "water_only_", "ground_only_", "air_only_", "free_for_all_"
    };
    
    // init events
    public static String[] events = new String[] {
        "water_only_", "ground_only_", "air_only_", "free_for_all_",
        
        "cycle", "rain",
        
        "mines", "lava", 
        
        "boss", "meat", "freeforall"
    };
    
    public static void init() {
        for (String event : events) {
            map.put(event, false);
        }
    }
    
    public static void generate(int maxEvents) {
        int current = 0;
        
        for (String event : events) {
            map.replace(event, false);
        }
        
        for (String event : events) {
            
            // Map Only Rules
            for (String only : onlys) {
                if (event.equals(only)) continue;
            }
            
            // Floors
            if (event.equals("lava") && map.get("mines")) continue;
            
            // Game Events
            if (event.equals("meat") && EventState.map.get("boss")) continue;
            
            if (Mathf.random(0, 100) > 70) {
                current++;
                map.replace(event, true);
            }
            
            if (current >= maxEvents) return;
        }
        
        if (map.get("meat")) Type.tier = 4;
        
        if (map.get("boss")) Type.tier = Mathf.random(0, 3);
        
        if (map.get("cycle")) {
            Main.rules.lighting = true;
            Main.rules.enemyLights = false;
            Main.rules.ambientLight = new Color(0, 0, 0, Mathf.random(0.7f, 1f));
        }
        
        if (map.get("rain")) {
            Main.rules.weather.add(new Weather.WeatherEntry(Weathers.rain));
        }
        
        map.replace("mines", true);
        
            
    }
}
