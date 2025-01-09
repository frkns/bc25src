package remake;

import battlecode.common.*;
import remake.fast.*;

import java.util.Random;

class UnitFuncs extends RobotPlayer {
    static final Random rng = new Random(0);

    static RobotController rc;
    static UnitType bunnyType;


    static MapLocation spawnTowerLocation;  // location of the tower that spawned me
    static Direction spawnDirection;

    static MapLocation target;
    // paint refill
    static MapLocation nearestPaintTower = null;
    static FastLocSet paintTowersLocs = new FastLocSet();
    static double lowPaintPercentage = 0.5;
    static boolean lowPaint = false;

    static void init(RobotController r) throws GameActionException {
        rc = r;
        bunnyType = rc.getType();
        for (RobotInfo robot : rc.senseNearbyRobots(2)) {
            if (robot.getType().isTowerType()) {
                // nearestPaintTower = robot.getLocation();
                spawnTowerLocation = robot.getLocation();
                spawnDirection = rc.getLocation().directionTo(spawnTowerLocation).opposite();

                if (PHASE == 1) {
                    Direction dirToMove = spawnDirection;
                    target = rc.getLocation().translate(dirToMove.dx * WIDTH, dirToMove.dy * HEIGHT);
                }
            }
        }
    }

    /** SOLDIER */
    static void refillPaint(RobotInfo[] nearbyRobots) throws GameActionException{
        nearestPaintTower = null; // Assume PaintTower could have been destroyed
        for (RobotInfo robot : nearbyRobots){
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER
                        || robot.getType() == UnitType.LEVEL_TWO_PAINT_TOWER
                        || robot.getType() == UnitType.LEVEL_THREE_PAINT_TOWER) {
                    if (nearestPaintTower != null && rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
            int myPaint = rc.getPaint();
            if (nearestPaintTower != null) {
                int amt = bunnyType.paintCapacity - myPaint;
                if (rc.canTransferPaint(nearestPaintTower, -1 * amt)) {
                    rc.transferPaint(nearestPaintTower, -1 * amt);
                }
            }
            PathFinder.move(nearestPaintTower);
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("Running soldier");

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();


        if (PHASE == 1) {
            refillPaint(nearbyRobots);
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