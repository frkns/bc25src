package e_action.utils;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import e_action.Robot;

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
        int type = 0;
        if(loc.x > 0){
            if(loc.x == width){
                type += 2;
            }else{
                type += 1;
            }
        }

        if(loc.y > 0){
            if(loc.y == height){
                type += 20;
            }else{
                type += 10;
            }
        }

        return switch(type){
            case 00 -> neighborsInMap[ORD_NORTHWEST];
            case 01 -> neighborsInMap[ORD_NORTH];
            case 02 -> neighborsInMap[ORD_NORTHEAST];

            case 10 -> neighborsInMap[ORD_WEST];
            case 11 -> neighborsInMap[ORD_CENTER];
            case 12 -> neighborsInMap[ORD_EAST];

            case 20 -> neighborsInMap[ORD_SOUTHWEST];
            case 21 -> neighborsInMap[ORD_SOUTH];
            case 22 -> neighborsInMap[ORD_SOUTHEAST];

            default -> neighborsInMap[ORD_NONE];
        };
    }
}
