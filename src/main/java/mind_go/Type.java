package mind_go;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class Type {
    public static int oldTier = 0;
    public static int tier = 1;
    public static ObjectMap<Class, UnitType[]> map = ObjectMap.of(
            Class.Main, new UnitType[]{UnitTypes.dagger, UnitTypes.mace, UnitTypes.fortress, UnitTypes.scepter, UnitTypes.reign},
            Class.Spiders, new UnitType[]{UnitTypes.crawler, UnitTypes.atrax, UnitTypes.spiroct, UnitTypes.arkyid, UnitTypes.toxopid},
            Class.Support, new UnitType[]{UnitTypes.nova, UnitTypes.pulsar, UnitTypes.quasar, UnitTypes.vela, UnitTypes.corvus},
            Class.Naval, new UnitType[]{UnitTypes.risso, UnitTypes.minke, UnitTypes.bryde, UnitTypes.sei, UnitTypes.omura},
            Class.Air, new UnitType[]{UnitTypes.flare, UnitTypes.horizon, UnitTypes.zenith, UnitTypes.antumbra, UnitTypes.eclipse},
            // mono, oct unplayable bruh
            Class.AirSupport, new UnitType[]{UnitTypes.quad, UnitTypes.poly, UnitTypes.mega, UnitTypes.quad, UnitTypes.oct}
    );

    public static UnitType get(Class class1) {
        return map.get(class1)[tier];
    }

    public static UnitType get(Class class1, int tier) {
        if (tier < 0) return map.get(class1)[0];
        if (tier > 4) return map.get(class1)[4];
        return map.get(class1)[tier];
    }

    public static int changeTier() /* return not repeated tier */ {
        int ttier = Mathf.random(0, 4);
        if (ttier == oldTier) return changeTier();
        return ttier;
    }
}
