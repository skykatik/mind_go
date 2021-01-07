package mind_go;

import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class Type {

    public static int tier = 1;
    public static ObjectMap<Class, UnitType[]> map = new ObjectMap<>();

    public static void init() {
        UnitType[] ground, legs, support;
        ground = new UnitType[5];
        ground[0] = UnitTypes.dagger;
        ground[1] = UnitTypes.mace;
        ground[2] = UnitTypes.fortress;
        ground[3] = UnitTypes.scepter;
        ground[4] = UnitTypes.reign;

        legs = new UnitType[5];
        legs[0] = UnitTypes.crawler;
        legs[1] = UnitTypes.atrax;
        legs[2] = UnitTypes.spiroct;
        legs[3] = UnitTypes.arkyid;
        legs[4] = UnitTypes.toxopid;

        support = new UnitType[5];
        support[0] = UnitTypes.nova;
        support[1] = UnitTypes.pulsar;
        support[2] = UnitTypes.quasar;
        support[3] = UnitTypes.vela;
        support[4] = UnitTypes.corvus;

        map.put(Class.Main, ground);
        map.put(Class.Spiders, legs);
        map.put(Class.Support, support);
    }

    public static UnitType get(Class class1) {
        return map.get(class1)[tier];
    }
}
