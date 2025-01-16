package kenny;

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
    static MapLocation mapCenter;

    static boolean isRefilling = false;

    static int turnsAlive = 0;

    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyTiles;
    static MapLocation nearestPaintTower;
    static MapLocation nearestEnemyTower;
    static MapLocation nearestEmptyTile;  // not used (update: we use it now for full fill)
    static MapLocation nearestEnemyPaint;

    static MapInfo curRuin;
    static boolean isFillingRuin = false;
    static MapLocation nearestWrongInRuin;

    static MapLocation curSRP;
    static boolean isFillingSRP = false;
    static MapLocation nearestWrongInSRP;

    static int siegePhase;
    static int mopperPhase;
    static int selfDestructPhase = 300;

    static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    static int selfDestructPaintThreshold = 50;

    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;

    static boolean inTowerRange = false;
    static int fullFillPhase;
    static int attackBasePhase;

    static int startPaintingFloorTowerNum = 5;  // don't want to paint floor before this to conserve paint

    static int role = 0;

    static MapLocation avgClump;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        mapCenter = new MapLocation(mapWidth/2, mapHeight/2);
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

        if (spawnTowerLocation == null)  // it is possible that is spawn tower is destroyed in the middle of the turn
            spawnTowerLocation = new MapLocation(0, 0);

        int mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium
        siegePhase = mx * 3;
        fullFillPhase = mx * 3;
        mopperPhase = mx * 4;
        attackBasePhase = mx * 4;
        if (rc.getType() == UnitType.SOLDIER && rc.getRoundNum() >= attackBasePhase) {
            // we do divison by ~10 first because we want to send the attackers in "waves"
            if ((rc.getRoundNum() / 10) % 3 == 0) {
                role = 1;
            }
        }

        switch (rc.getType()) {
            case SOLDIER: {
                switch (role) {
                    case 1:
                        AttackBase.init();
                        break;
                }
                break;
            }
        }

        while (true) {
            try {
                turnsAlive++;
                roundNum = rc.getRoundNum();

                // update stuff
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();

                if (!rc.getType().isTowerType())
                    ImpureUtils.updateNearestPaintTower();

                switch (rc.getType()) {
                    case SOLDIER: {
                        switch (role) {
                            case 1:
                                AttackBase.run();
                                break;
                            default: runSoldier();
                        }
                        break;
                    }
                    case MOPPER: runMopper(); break;
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
                if (roundNum != rc.getRoundNum()) {
                    System.out.println("~~~ Went over bytecode limit!! " + rc.getType());
                    rc.setIndicatorLine(new MapLocation(0, 0), rc.getLocation(), 255, 0, 0);
                }
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    static int numSpawnedUnits = 0;

    public static void runTower() throws GameActionException {
        Towers.run();
    }

    public static void runSoldier() throws GameActionException {
        Soldiers.run();
    }

    public static void runMopper() throws GameActionException {
        Moppers.run();
    }

    // public static void runSplasher(RobotController rc) throws GameActionException{
    //     Splashers.run();
    // }
}
