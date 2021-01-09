package components;

import arc.util.Strings;
import mindustry.gen.Player;

import static mind_go.Main.bundle;

public abstract class Info {

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(bundle.format(key, values));
    }

    public static void format(Player player, String key, Object... values) {
        player.sendMessage(Strings.format(key, values));
    }
}
