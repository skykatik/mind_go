package mind_go;

import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.type.UnitType;

public class PlayerData {
    UnitType unit = UnitTypes.dagger;
    Player player;
    
    public PlayerData(Player player) {
        this.player = player;
    }
}
