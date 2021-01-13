/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Events;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Log;
import java.util.HashMap;
import mind_go.Main;
import mind_go.Type;

/**
 *
 * @author Xusk
 */
public class EventState {

    public static HashMap<String, HashMap> category = new HashMap<>();
    public static HashMap<String, Boolean> categoryEnabled = new HashMap<>();
    // init events    
    public static String[] categ = new String[]{
        "onlys", "weather", "floors", "gamemode"
    };

    public static String[][] events = new String[][]{
        { // Onlys
            "water_only_", "ground_only_", "air_only_", "free_for_all_", "bomb_defense_", "base_defense_",
        },
        { // Weather
            "cycle", "rain", "snow"
        },
        { // Floors
            "lava", "mines"
        },
        { // GameMode
            "boss", "meat"
        }
    };
    public static boolean weather = false,
            floors = false,
            gamemode = false;

    public static void init() {
        for (int i = 0; i < categ.length; i++) {
            String cate = categ[i];
            HashMap<String, Boolean> hash = new HashMap<>();
            for (String string : events[i]) {
                hash.put(string, false);
            }
            category.put(cate, hash);
            categoryEnabled.put(cate, false);
        }
    }

    public static void generate(int maxEvents) {
        int current = 0;

        for (int i = 0; i < categ.length; i++) {
            for (String evnt : events[i]) {
                category.get(categ[i]).replace(evnt, false);
            }
            categoryEnabled.replace(categ[i], false);
        }

        for (int i = 0; i < categ.length; i++) {
            String catego = categ[i];
            if (i == 0) continue;
            if (catego.equals("gamemode") && get("onlys", "free_for_all_")) continue;
            for (String even : events[i]) {
                if (current <= maxEvents && !categoryEnabled.get(catego) && Mathf.random(0, 100) > 60) {
                    category.get(catego).replace(even, true);
                    categoryEnabled.replace(catego, true);
                    current++;
                }
            }
        }

        if (get("gamemode", "meat")) {
            Type.tier = Mathf.random(3, 4);
        }

        if (get("gamemode", "boss")) {
            Type.tier = Mathf.random(0, 3);
        }
        
        if (get("onlys", "free_for_all_")) {
            EventState.replace("onlys", "ground_only_", true);
        }
        
        if (get("weather", "cycle")) {
            Main.rules.lighting = true;
            Main.rules.enemyLights = false;
            Main.rules.ambientLight = new Color(0, 0, 0, Mathf.random(0.8f, 1f));
        }
    }

    public static boolean get(String category, String name) {
        return (boolean) EventState.category.get(category).get(name);
    }

    public static void put(String category, String name, Boolean what) {
        EventState.category.get(category).put(name, what);
    }
    
    public static void replace(String category, String name, Boolean what) {
        EventState.category.get(category).replace(name, what);
    }
}
