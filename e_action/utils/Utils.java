package e_action.utils;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import templateBot.Robot;

public class Utils{
    static RobotController rc;
    static int width;
    static int height;
    static MapLocation LocNortWest;
    static MapLocation LocStouhEast;

    public static final Direction[] directions = {
            Direction.CENTER,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int ORD_CENTER = 0;
    static final int ORD_NORTH = 1;
    static final int ORD_NORTHEAST =2;
    static final int ORD_EAST =3;
    static final int ORD_SOUTHEAST =4;
    static final int ORD_SOUTH =5;
    static final int ORD_SOUTHWEST =6;
    static final int ORD_WEST =7;
    static final int ORD_NORTHWEST =8;
    static final int ORD_NONE =9;

    static boolean[][] neighborsInMap = {
            {true, true, true, true, true, true, true, true, true}, // CENTER
            {true, false, false, true, true, true, true, true, false}, // NORTH
            {true, false, false, false, true, true, false, true, false}, // NORTHEAST
            {true, true, false, false, false, true, true, true, true}, // EAST
            {true, true, false, false, false, false, false, true, true}, // SOUTHEAST
            {true, true, true, true, false, false, false, true, true}, // SOUTH
            {true, true, true, true, false, false, false, false, false}, // SOUTHWEST
            {true, true, true, true, true, true, false, false, false}, // WEST
            {true, false, false, true, true, true, false, false, false}, // NORTHWEST
            {true, false, false, false, false, false, false, false, false}, // ERROR
    };
    static int ERROR = 10;

    public static void init(){
        rc = Robot.rc;
        width = Robot.rc.getMapWidth();
        height = Robot.rc.getMapHeight();

        LocNortWest = new MapLocation(0, 0);
        LocStouhEast = new MapLocation(width, height);
    }

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


    /**
     * Return an array boolean[] response where response[direction.ordinal()] = true if and only if, loc.add(direction) is in map
     * */
    public static boolean[] getIsExistingNeighbors(MapLocation loc){

        /*
        * Let's take the location(1, 0), we have from :
        * 1) LocNortEast.directionTo(loc) : →
        * 2) LocStouhEst.directionTo(loc) : ↖
        * Then location should be in the first row (according to 1) and not in upper right corner (2 is not
        * */
        return switch(LocNortWest.directionTo(loc).ordinal() * 10 + LocStouhEast.directionTo(loc).ordinal()){
            case ORD_CENTER * 10 + ORD_NORTHWEST -> neighborsInMap[ORD_NORTHWEST];

            case ORD_SOUTH * 10 + ORD_WEST -> neighborsInMap[ORD_NORTHWEST];
            case ORD_SOUTH * 10 + ORD_NORTHWEST -> neighborsInMap[ORD_WEST];

            case ORD_EAST * 10 + ORD_NORTH -> neighborsInMap[ORD_NORTHEAST];
            case ORD_EAST * 10 + ORD_NORTHWEST -> neighborsInMap[ORD_NORTH];

            case ORD_SOUTHEAST * 10 + ORD_NORTHWEST -> neighborsInMap[ORD_CENTER];
            case ORD_SOUTHEAST * 10 + ORD_CENTER -> neighborsInMap[ORD_SOUTHEAST];
            case ORD_SOUTHEAST * 10 + ORD_EAST -> neighborsInMap[ORD_SOUTH];
            case ORD_SOUTHEAST * 10 + ORD_NORTH -> neighborsInMap[ORD_EAST];


            default -> neighborsInMap[ORD_NONE];
        };
    }

}
