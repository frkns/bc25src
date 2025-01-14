package heuristic_test;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    public static MapLocation[] locationHistory = new MapLocation[8];

    static final Random rng = new Random();
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;

    static RobotController rc;
    static int roundNum;
    static int mapWidth;
    static int mapHeight;

    static boolean isRefilling = false;

    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyTiles;
    static MapLocation nearestPaintTower;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType().isTowerType()) {
                    if (spawnTowerLocation == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(spawnTowerLocation)) {
                        spawnTowerLocation = robot.getLocation();
                        spawnTowerType = robot.getType().getBaseType();
                    }
                }
            }
        }

        while (true) {
            try {
                roundNum = rc.getRoundNum();

                // update stuff
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();

                if (!rc.getType().isTowerType())
                for (RobotInfo robot : nearbyRobots) {
                    if (robot.getTeam() == rc.getTeam()) {
                        if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                            if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(nearestPaintTower)) {
                                nearestPaintTower = robot.getLocation();
                            }
                        }
                    }
                }

                switch (rc.getType()) {
                    case SOLDIER: runSoldier(); break;
                    // case MOPPER: runMopper(); break;
                    // case SPLASHER: runSplasher();
                    default: runTower(); break;
                }

            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                if (roundNum != rc.getRoundNum())
                    System.out.println("~~~ Went over bytecode limit!! ~~~");
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    static int numSpawnedUnits = 0;
    public static void runTower() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        // four main dirs
        if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.WEST) {
            nextLoc = nextLoc.add(dir);
        }


        if (numSpawnedUnits == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER,nextLoc);
            numSpawnedUnits++;
        }

        RobotInfo[] robots = rc.senseNearbyRobots();

        for (RobotInfo robot  : robots ) {
            if (robot.getTeam() == rc.getTeam()) {
                // our team
            }
            else {
                // their team
                if(rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                }
            }
        }

    }


    public static void runSoldier() throws GameActionException {
        Soldiers.run();
    }

    // public static void runMopper() throws GameActionException{
    //     Moppers.run();
    // }

    // public static void runSplasher(RobotController rc) throws GameActionException{
    //     Splashers.run();
    // }
}
