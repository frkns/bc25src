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
    
    
    public static MapLocation mirror(MapLocation loc) {
        return new MapLocation(_Info.MAP_WIDTH - loc.x - 1, _Info.MAP_HEIGHT - loc.y - 1);
    }

    public static MapLocation randomEnemyLocation(MapLocation spawnLoc) {
        MapLocation loc = mirror(spawnLoc);
        // add a small offset based on a % of HEIGHT and WIDTH
        int offsetX = (int)(_Info.MAP_WIDTH * 0.1);
        int offsetY = (int)(_Info.MAP_HEIGHT * 0.1);
        loc = new MapLocation(loc.x + _Info.rng.nextInt(offsetX) - offsetX/2, loc.y + _Info.rng.nextInt(offsetY) - offsetY/2);
        return new MapLocation(Math.min(Math.max(0, loc.x), _Info.MAP_WIDTH-1), Math.min(Math.max(0, loc.y), _Info.MAP_HEIGHT-1));
    }


    public static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

}
