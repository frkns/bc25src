package gavin531_PM_MST;

import java.util.Random;
import battlecode.common.*;



public class Utils {
    private static RobotController rc;

    static Random rng;

    // constants that vary by game
    static int roundNumber;
    static int MAP_WIDTH;
    static int MAP_HEIGHT;
    static int MAP_AREA;
    static MapLocation SPAWN_LOCATION;

    // vertical symmetry
    static MapLocation verticalMirror(MapLocation loc) {
        return new MapLocation(loc.x, MAP_HEIGHT - loc.y - 1);
    }

    // horizontal symmetry
    static MapLocation horizontalMirror(MapLocation loc) {
        return new MapLocation(MAP_WIDTH - loc.x - 1, loc.y);
    }

    static MapLocation mirror(MapLocation loc) {  // rotational
        return new MapLocation(MAP_WIDTH - loc.x - 1, MAP_HEIGHT - loc.y - 1);
    }

    static MapLocation randomEnemyLocation() {
        // return new MapLocation(rng.nextInt(MAP_WIDTH-1), rng.nextInt(MAP_HEIGHT-1));
        MapLocation loc = mirror(SPAWN_LOCATION);
        // add a small offset based on a % of HEIGHT and WIDTH
        int offsetX = (int)(MAP_WIDTH * 0.1);
        int offsetY = (int)(MAP_HEIGHT * 0.1);
        loc = new MapLocation(loc.x + rng.nextInt(offsetX) - offsetX/2, loc.y + rng.nextInt(offsetY) - offsetY/2);
        return new MapLocation(Math.min(Math.max(0, loc.x), MAP_WIDTH-1), Math.min(Math.max(0, loc.y), MAP_HEIGHT-1));
    }

    static void init(RobotController r) {
        rc = r;
        rng = new Random(rc.getRoundNum() * 1007 + rc.getID() * 1009);

        MAP_HEIGHT = rc.getMapHeight();
        MAP_WIDTH = rc.getMapWidth();
        MAP_AREA = MAP_HEIGHT * MAP_WIDTH;
        SPAWN_LOCATION = rc.getLocation();
    }




    // other stuffs that i copied

    public static boolean isInMap(MapLocation loc) {
        return (loc.x >= 0 && loc.x < MAP_WIDTH && loc.y >= 0 && loc.y < MAP_HEIGHT);
    }

    // misc
    public static boolean isBitOne(int value, int LSBpos) {
        return (((value >> LSBpos) & 1) == 1);
    }

    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
}
