package remake;

import battlecode.common.*;

import java.util.Random;

class UnitFuncs extends RobotPlayer {
    static final Random rng = new Random(0);

    static RobotController rc;
    static UnitType bunny_t;

    static Direction direction;
    static MapLocation target;

    static MapLocation nearestPaintTower = null;


    static void init(RobotController r) throws GameActionException {
        rc = r;
        bunny_t = rc.getType();
    }

    /** SOLDIER */
    static void runSoldier() throws GameActionException {
        System.out.println("Running soldier");

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();


        for (RobotInfo robot : nearbyRobots){
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER
                    || robot.getType() == UnitType.LEVEL_TWO_PAINT_TOWER
                    || robot.getType() == UnitType.LEVEL_THREE_PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }

        int mypaint = rc.getPaint();
        if (mypaint < 40) {
            // we should path to nearestPaintTower
            if (nearestPaintTower != null) {
                int amt = bunny_t.paintCapacity - mypaint;
                if (rc.canTransferPaint(nearestPaintTower, -1 * amt)) {
                    rc.transferPaint(nearestPaintTower, -1 * amt);
                }
                target = nearestPaintTower;
            }
        } else {
            // set regualr target
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