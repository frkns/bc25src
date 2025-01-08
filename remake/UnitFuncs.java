package remake;

import battlecode.common.*;

import java.util.Random;

class UnitFuncs extends RobotPlayer {
    static final Random rng = new Random(0);

    static RobotController rc;
    static UnitType bunny_t;
    static Direction direction;
    static MapLocation target;

    static void init(RobotController r) throws GameActionException {
        rc = r;
        bunny_t = rc.getType();
    }

    /** SOLDIER */
    static void runSoldier() throws GameActionException {
        System.out.println("Running soldier");
        MapLocation testLoc = new MapLocation(3, 3);
        PathFinder.move(testLoc);
        int mypaint = rc.getPaint();
        if (mypaint < 40) {

        }
        if (PHASE == 1) {

        }
    }

    //** MOPPER */
    static void runMopper() throws GameActionException {
        System.out.println("Running mopper");
    }


    //** SPLASHER */
    static void runSplasher() throws GameActionException {
        System.out.println("Running splasher");
    }


    static MapInfo findNearbyRuin(RobotController rc, MapInfo[] nearbyTiles) throws GameActionException {
        return null;
    }
    static UnitType chooseTowerType(RobotController rc, MapLocation targetLoc) throws GameActionException {
        return null;
    }
    static void createTowerPattern(RobotController rc, MapLocation targetLoc) throws GameActionException {
    }
}