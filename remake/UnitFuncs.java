package remake;

import battlecode.common.*;

import java.util.Random;

public class UnitFuncs extends RobotPlayer {

    static final Random rng = new Random(0);

    public static RobotController rc;
    public static UnitType bunny_t;

    public static Direction direction;
    public static MapLocation target;

    public static MapLocation nearestPaintTower = null;


    public static void init(RobotController r) throws GameActionException {
        rc = r;
        bunny_t = rc.getType();
    }

    /** SOLDIER */
    public static void runSoldier() throws GameActionException {
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
    public static void runMopper() throws GameActionException {
        System.out.println("Running mopper");
    }


    //** SPLASHER */
    public static void runSplasher() throws GameActionException {
        System.out.println("Running splasher");
    }


    private static MapInfo findNearbyRuin(RobotController rc, MapInfo[] nearbyTiles) throws GameActionException {
        return null;
    }
    private static UnitType chooseTowerType(RobotController rc, MapLocation targetLoc) throws GameActionException {
        return null;
    }
    private static void createTowerPattern(RobotController rc, MapLocation targetLoc) throws GameActionException {
    }
}