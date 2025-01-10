package e_action.utils;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import e_action.Robot;

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
        return new MapLocation(Robot.MAP_WIDTH - loc.x - 1, Robot.MAP_HEIGHT - loc.y - 1);
    }

    public static int explorationBoundary = 5;
    public static boolean outOfExplorationBounds(MapLocation loc) {
        return loc.x - explorationBoundary < 0 || loc.y - explorationBoundary < 0
            || loc.x + explorationBoundary >= Robot.MAP_WIDTH || loc.y + explorationBoundary >= Robot.MAP_HEIGHT;
    }

    public static MapLocation getRandomInBoundLocation() {
        // should be within [explorationBoundary+1, Robot.MAP_WIDTH-explorationBoundary-1]
        return new MapLocation(Robot.rng.nextInt(Robot.MAP_WIDTH - 2*explorationBoundary - 1) + explorationBoundary + 1,
                               Robot.rng.nextInt(Robot.MAP_HEIGHT - 2*explorationBoundary - 1) + explorationBoundary + 1);

    }

    public static MapLocation randomEnemyLocation(MapLocation spawnLoc) {
        MapLocation loc = mirror(spawnLoc);
        // add a small offset based on a % of HEIGHT and WIDTH
        int offsetX = (int)(Robot.MAP_WIDTH * 0.1);
        int offsetY = (int)(Robot.MAP_HEIGHT * 0.1);
        loc = new MapLocation(loc.x + Robot.rng.nextInt(offsetX) - offsetX/2, loc.y + Robot.rng.nextInt(offsetY) - offsetY/2);
        return new MapLocation(Math.min(Math.max(0, loc.x), Robot.MAP_WIDTH-1), Math.min(Math.max(0, loc.y), Robot.MAP_HEIGHT-1));
    }


    public static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

}
