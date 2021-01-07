package mind_go;

import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class Type {

    public static int tier = 1;
    public static ObjectMap<Class, UnitType[]> map = new ObjectMap<>();

    public static void init() {
        map.put(Class.Main, new UnitType[]{UnitTypes.dagger, UnitTypes.mace, UnitTypes.fortress, UnitTypes.scepter, UnitTypes.reign});
        map.put(Class.Spiders, new UnitType[]{UnitTypes.crawler, UnitTypes.atrax, UnitTypes.spiroct, UnitTypes.arkyid, UnitTypes.toxopid});
        map.put(Class.Support, new UnitType[]{UnitTypes.nova, UnitTypes.pulsar, UnitTypes.quasar, UnitTypes.vela, UnitTypes.corvus});
    }

    public static UnitType get(Class class1) {
        return map.get(class1)[tier];
    }
}
