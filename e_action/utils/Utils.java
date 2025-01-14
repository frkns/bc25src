package e_action.utils;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import e_action.knowledge._Info;

public class Utils{
    public static RobotController rc;

    public enum towerType {
        PAINT_TOWER,
        MONEY_TOWER,
        DEFENSE_TOWER
    }

    public static towerType getTowerType(UnitType unit) {
        return switch (unit) {
            case LEVEL_ONE_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_TWO_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_THREE_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_ONE_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_TWO_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_THREE_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_ONE_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            case LEVEL_TWO_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            case LEVEL_THREE_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            default -> null;
        };
    }


    public static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

}
