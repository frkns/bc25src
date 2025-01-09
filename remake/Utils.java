package remake;

import java.util.Random;
import battlecode.common.*;


class Utils extends RobotPlayer {
    static RobotController rc;

    static Random rng;

    // constants that vary by game
    static int roundNumber;
    static int MAP_WIDTH;
    static int MAP_HEIGHT;
    static int MAP_AREA;
    static MapLocation SPAWN_LOCATION;
    static boolean DEBUG_FAIL_FAST = false;

    static void init(RobotController r) throws GameActionException{
        rc = r;
        rng = new Random(rc.getRoundNum() * 1007 + rc.getID() * 1009);

        MAP_HEIGHT = rc.getMapHeight();
        MAP_WIDTH = rc.getMapWidth();
        MAP_AREA = MAP_HEIGHT * MAP_WIDTH;
        SPAWN_LOCATION = rc.getLocation();
    }

    static MapLocation mirror(MapLocation loc) {
        return new MapLocation(MAP_WIDTH - loc.x - 1, MAP_HEIGHT - loc.y - 1);
    }

    static int explorationBoundary = 5;

    static boolean outOfExplorationBounds(MapLocation loc) {
        return loc.x - explorationBoundary < 0 || loc.y - explorationBoundary < 0
            || loc.x + explorationBoundary >= MAP_WIDTH || loc.y + explorationBoundary >= MAP_HEIGHT;
    }
    static MapLocation getRandomInBoundLocation() {
        // should be within [explorationBoundary+1, MAP_WIDTH-explorationBoundary-1]
        MapLocation res =  new MapLocation(rng.nextInt(MAP_WIDTH - 2*explorationBoundary - 1) + explorationBoundary + 1,
                               rng.nextInt(MAP_HEIGHT - 2*explorationBoundary - 1) + explorationBoundary + 1);

        return res;
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


    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

}
