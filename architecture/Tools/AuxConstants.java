package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.UnitType;


// all fields must be marked final
public class AuxConstants extends RobotPlayer {

    public static UnitType[] buildOrder = {
            UnitType.LEVEL_ONE_MONEY_TOWER,  // two starting towers - there could be more, assume 2 for now
            UnitType.LEVEL_ONE_PAINT_TOWER,

            UnitType.LEVEL_ONE_MONEY_TOWER,  // try to have contiguous blocks to avoid repainting of patterns
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,

            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
    };

    // both paint and money towers have the same health, as of v2.0.0
    static final int[] paintMoneyTowerHealth = {1000, 1500, 2000};
    static final int[] defenseTowerHealth = {2000, 2500, 3000};
}
