package mind_go;

import arc.graphics.Color;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Nulls;
import mindustry.gen.Player;
import mindustry.gen.Unit;

public class Room {

    public final static int ROOM_SIZE = 14;
    int roomSize;
    float x, y, endX, endY, centreX, centreY;
    
    Unit unit;
    Class classa = Class.Main;
    String name = "null";

    public Room(Class class1, String name, int x, int y, int endX, int endY) {
        this.name = name;
        this.classa = class1;
        this.x = x * Vars.tilesize;
        this.y = y * Vars.tilesize;
        this.endX = endX * Vars.tilesize;
        this.endY = endY * Vars.tilesize;
        this.centreX = this.x + (this.endX - this.x) / 2;
        this.centreY = this.y + (this.endY - this.y) / 2;
        this.roomSize = endX - x;
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

    public void update() {
        if (unit != null) {
            if (unit.isPlayer()) {
                unit.getPlayer().lastReadUnit = Nulls.unit;
                unit.getPlayer().unit(Nulls.unit);
            }
        }
    }
    
    public void debugDraw() {
        for (int xx = (int) x / Vars.tilesize; xx < (int) endX / Vars.tilesize; xx++) {
            for (int yy = (int) y / Vars.tilesize; yy < (int) endY / Vars.tilesize; yy++) {
                Call.createBullet(Bullets.fragExplosive, Team.sharded, xx * Vars.tilesize, yy * Vars.tilesize, 0, 0, 0, 9999);
            }
        }
    }
}
