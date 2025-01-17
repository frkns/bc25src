package e.utils;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e.knowledge._Info;

import java.util.Random;

public class Mirror {

    static Random rng;

    // constants that vary by game
    static int roundNumber;
    static int MAP_WIDTH;
    static int MAP_HEIGHT;
    static int MAP_AREA;
    static MapLocation SPAWN_LOCATION;

    public static RobotController rc = _Info.rc;

    // vertical symmetry
    public static MapLocation verticalMirror(MapLocation loc) {
        return new MapLocation(loc.x, MAP_HEIGHT - loc.y - 1);
    }

    // horizontal symmetry
    public static MapLocation horizontalMirror(MapLocation loc) {
        return new MapLocation(MAP_WIDTH - loc.x - 1, loc.y);
    }

    public static MapLocation mirror(MapLocation loc) {  // rotational
        return new MapLocation(MAP_WIDTH - loc.x - 1, MAP_HEIGHT - loc.y - 1);
    }

    public static MapLocation randomEnemyLocation() {
        // return new MapLocation(rng.nextInt(MAP_WIDTH-1), rng.nextInt(MAP_HEIGHT-1));
        MapLocation loc = mirror(SPAWN_LOCATION);
        // add a small offset based on a % of HEIGHT and WIDTH
        int offsetX = (int)(MAP_WIDTH * 0.1);
        int offsetY = (int)(MAP_HEIGHT * 0.1);
        loc = new MapLocation(loc.x + rng.nextInt(offsetX) - offsetX/2, loc.y + rng.nextInt(offsetY) - offsetY/2);
        return new MapLocation(Math.min(Math.max(0, loc.x), MAP_WIDTH-1), Math.min(Math.max(0, loc.y), MAP_HEIGHT-1));
    }

    public static void init() {

        rng = new Random(rc.getRoundNum() * 1007 + rc.getID() * 1009);

        MAP_HEIGHT = rc.getMapHeight();
        MAP_WIDTH = rc.getMapWidth();
        MAP_AREA = MAP_HEIGHT * MAP_WIDTH;
        SPAWN_LOCATION = rc.getLocation();
    }
}

