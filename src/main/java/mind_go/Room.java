package mind_go;

import mindustry.Vars;
import mindustry.gen.Player;

public class Room {
    public final static int ROOM_SIZE = 10;
    float x, y, endX, endY, centreX, centreY;
    Class classa = Class.Main;
    String name = "null";
    
    public Room(Class class1, String name, int x, int y, int endX, int endY) {
        this.classa = class1;
        this.x = x * Vars.tilesize;
        this.y = y * Vars.tilesize;
        this.endX = endX * Vars.tilesize;
        this.endY = endY * Vars.tilesize;
        this.centreX = x + (endX - x) / 2;
        this.centreY = y + (endY - y) / 2;
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
