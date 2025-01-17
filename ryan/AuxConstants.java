package ryan;

import battlecode.common.UnitType;


// all fields must be marked final
public class AuxConstants extends RobotPlayer {
    static final UnitType[] buildOrder = {
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
        UnitType.LEVEL_ONE_MONEY_TOWER,
        UnitType.LEVEL_ONE_MONEY_TOWER,
        UnitType.LEVEL_ONE_MONEY_TOWER,
        UnitType.LEVEL_ONE_MONEY_TOWER,
        UnitType.LEVEL_ONE_MONEY_TOWER,
        UnitType.LEVEL_ONE_PAINT_TOWER,
        UnitType.LEVEL_ONE_PAINT_TOWER,
        UnitType.LEVEL_ONE_PAINT_TOWER,
        UnitType.LEVEL_ONE_PAINT_TOWER,

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
}
