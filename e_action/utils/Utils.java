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
            case 0 -> neighborsInMap[ORD_NORTHWEST];
            case 1 -> neighborsInMap[ORD_NORTH];
            case 2 -> neighborsInMap[ORD_NORTHEAST];

            case 10 -> neighborsInMap[ORD_WEST];
            case 11 -> neighborsInMap[ORD_CENTER];
            case 12 -> neighborsInMap[ORD_EAST];

            case 20 -> neighborsInMap[ORD_SOUTHWEST];
            case 21 -> neighborsInMap[ORD_SOUTH];
            case 22 -> neighborsInMap[ORD_SOUTHEAST];

            default -> neighborsInMap[ORD_NONE];
        };
    }

    public static String locToString(MapLocation loc){
        return "" + loc.x + loc.y;
    }

    public static boolean isLocationInRangeOfMopper(MapLocation mopperLoc, MapLocation targetLocation){
        int hash = (targetLocation.x - mopperLoc.x) * 1000 + (targetLocation.y - mopperLoc.y);
        // dx * 1000 + dy to avoid collision beetween x and y.
        // For exemple colision is possible with dx*100 + dy for (1, -50) and (0, -50)
        // generate by script/mopperRange.py

        switch(hash){
            case 1:
            case 2:
            case 3:
            case -1003:
            case -1002:
            case -1001:
            case -1000:
            case -999:
            case -998:
            case -997:
            case -2002:
            case -2001:
            case -2000:
            case -1999:
            case -1998:
            case -1997:
            case 2999:
            case 3000:
            case 3001:
            case 3002:
            case -3002:
            case -3001:
            case -3000:
            case -2999:
            case 1997:
            case 1998:
            case 1999:
            case 2000:
            case 2001:
            case 2002:
            case 997:
            case 998:
            case 999:
            case 1000:
            case 1001:
            case 1002:
            case 1003:
            case -1:
            case -3:
            case -2:
                return true;
            default: return false;
        }
    }


}
