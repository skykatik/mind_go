/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Events;

import arc.math.Mathf;
import java.util.HashMap;
import mind_go.Type;

/**
 *
 * @author Xusk
 */
public class EventState {
    public static HashMap<String, Boolean> map = new HashMap<>();
    
    // FOR INITIALIZE ON SERVER START
    public static String[] onlys = new String[] {
        "water_only_", "ground_only_", "air_only_"
    };
    
    // init events
    public static String[] events = new String[] {
        "water_only_", "ground_only_", "air_only_",
        
        "cycle", "rain",
        
        "mines", "lava", 
        
        "meat", "boss", "freeforall"
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
            
            for (String only : onlys) {
                if (event.equals(only)) continue;
            }
            
            if (event.equals("meat") && EventState.map.get("boss")) continue;
            
            if (Mathf.random(0, 100) > 70) {
                current++;
                map.replace(event, true);
            }
            
            if (current >= maxEvents) return;
        }
        
        if (map.get("meat")) Type.tier = 4;
        
        if (map.get("boss")) Type.tier = Mathf.random(0, 3);
    }
}
