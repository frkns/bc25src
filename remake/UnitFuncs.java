package remake;

import battlecode.common.*;

import java.util.Random;

class UnitFuncs extends RobotPlayer {
    static final Random rng = new Random(0);

    static RobotController rc;
    static UnitType bunnyType;

    // static Direction direction;
    static MapLocation spawnTowerLocation;  // location of the tower that spawned me
    static Direction spawnDirection;

    static MapLocation target;

    static MapLocation nearestPaintTower = null;


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
                int amt = bunnyType.paintCapacity - mypaint;
                if (rc.canTransferPaint(nearestPaintTower, -1 * amt)) {
                    rc.transferPaint(nearestPaintTower, -1 * amt);
                }
                target = nearestPaintTower;
            }
        } else {
            // move away from spawn tower
            // if (target == null) {
            //     Direction dirToMove = spawnDirection;
            //     target = rc.getLocation().translate(dirToMove.dx * 100, dirToMove.dy * 100);
            // }

            Direction curTargetDir = rc.getLocation().directionTo(target);
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            int dx = curTargetDir.dx;
            int dy = curTargetDir.dy;
            int triesLeft = 10;
            int boundary = 5;
            while (triesLeft-- > 0 &&
                   x + dx + boundary >= WIDTH || x + dx - boundary < 0 ||
                   y + dy + boundary >= HEIGHT || y + dy - boundary < 0) {
                curTargetDir = curTargetDir.rotateLeft();
                dx = curTargetDir.dx;
                dy = curTargetDir.dy;
            }
            if (triesLeft == 0) {
                System.out.println("Couldn't find a valid target");
            }
            target = new MapLocation(x + dx * 10, y + dy * 10);

        }

        PathFinder.move(target);

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