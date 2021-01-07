/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import mindustry.Vars;
import mindustry.gen.Player;

/**
 *
 * @author Xusk
 */
public class Room {
    public final static int ROOM_SIZE = 10;
    float x, y, endX, endY;
    Class classa = Class.Main;
    String name = "null";
    
    public Room(Class class1, String name, int x, int y, int endX, int endY) {
        this.classa = class1;
        this.x = x * Vars.tilesize;
        this.y = y * Vars.tilesize;
        this.endX = endX * Vars.tilesize;
        this.endY = endY * Vars.tilesize;
    }
    
    public Room(Class class1, String name, int x, int y, int roomSize) {
        this(class1, name, x, y, x + roomSize, y + roomSize);
    }
    
    public Room(Class class1, String name, int x, int y) {
        this(class1, name, x, y, ROOM_SIZE);
    }
    
    public boolean check(Player player) {
        return player.x > x && player.y > y && player.x < endX && player.y < endY;
    }
}
