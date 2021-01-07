/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mind_go;

import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

/**
 *
 * @author Xusk
 */
public class Type {
    public static int tier = 1;
    
    public static UnitType get(Class class1) {
        switch (class1) {
            case Main: /* Region Ground Attack */
                switch (tier) {
                    case 1:
                        return UnitTypes.dagger;
                    case 2:
                        return UnitTypes.mace;
                    case 3:
                        return UnitTypes.fortress;
                    case 4:
                        return UnitTypes.scepter;
                    case 5:
                        return UnitTypes.reign;
                    default: 
                        return UnitTypes.dagger;
                }
            case Support: /* Ground Support */
                switch (tier) {
                    case 1:
                        return UnitTypes.nova;
                    case 2:
                        return UnitTypes.pulsar;
                    case 3:
                        return UnitTypes.quasar;
                    case 4:
                        return UnitTypes.vela;
                    case 5:
                        return UnitTypes.corvus;
                    default: 
                        return UnitTypes.nova;
                }
            case Spiders: /* Ground Legs */
                switch (tier) {
                    case 1:
                        return UnitTypes.crawler;
                    case 2:
                        return UnitTypes.atrax;
                    case 3:
                        return UnitTypes.spiroct;
                    case 4:
                        return UnitTypes.arkyid;
                    case 5:
                        return UnitTypes.toxopid;
                    default:
                        return UnitTypes.crawler;
                }
            default:
                return UnitTypes.dagger;
        }
    }
}
