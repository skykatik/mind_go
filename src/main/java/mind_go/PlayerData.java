package mind_go;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

public class PlayerData {
    public static PlayerData boss;
    public Unit unita;
    public boolean isBoss = false;
    Class unit = Class.Main;
    Player player;

    public PlayerData(Player player) {
        this.player = player;
    }
    
    public static void resetValues() {
        boss = null;
        for (Player player : Groups.player) {
            Main.data.get(player).isBoss = false;
        }
    }
}
