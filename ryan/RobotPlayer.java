package ryan;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    //region Constants and Configuration
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

    static int startPaintingFloorTowerNum = 4;
    static int selfDestructFriendlyRobotsThreshold = 20;
    static int selfDestructEnemyRobotsThreshold = 5;
    static int selfDestructPaintThreshold = 50;
    //endregion

    //region Game State Variables
    static RobotController rc;
    static int roundNum;
    static int mapWidth;
    static int mapHeight;
    static MapLocation mapCenter;
    static int turnsAlive = 0;
    static int numSpawnedUnits = 0;
    //endregion

    //region Map and Location Tracking
    public static MapLocation[] locationHistory = new MapLocation[8];
    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;
    static MapLocation[] quadrantCenters = new MapLocation[4];
    static MapLocation[] quadrantCorners = new MapLocation[4];
    static int[] roundsSpentInQuadrant = new int[4];
    //endregion

    //region Nearby Info
    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyTiles;
    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;
    static boolean inTowerRange = false;
    //endregion

    //region Strategy Phase Variables
    static int siegePhase;
    static int mopperPhase;
    static int splasherPhase;
    static int selfDestructPhase = 300;
    static int fullFillPhase;
    static int attackBasePhase;
    static int reservePaintPhase;
    static int role = 0;
    //endregion

    //region Resource Management
    static boolean isRefilling = false;
    static int reservePaint = 100;
    static int reserveChips = 1700;
    //endregion

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

    static int mx;  // max of mapWidth and mapHeight

    static MapLocation avgClump;

    private static void initializeGame(RobotController r) throws GameActionException {
        rc = r;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        mapCenter = new MapLocation(mapWidth/2, mapHeight/2);
        quadrantCenters[0] = new MapLocation(3*mapWidth/4, 3*mapHeight/4);
        quadrantCenters[1] = new MapLocation(1*mapWidth/4, 3*mapHeight/4);
        quadrantCenters[2] = new MapLocation(1*mapWidth/4, 1*mapHeight/4);
        quadrantCenters[3] = new MapLocation(3*mapWidth/4, 1*mapHeight/4);
        quadrantCorners[0] = new MapLocation(mapWidth-1, mapHeight-1);
        quadrantCorners[1] = new MapLocation(0, mapHeight-1);
        quadrantCorners[2] = new MapLocation(0, 0);
        quadrantCorners[3] = new MapLocation(mapWidth-1, 0);

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

        mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium
        siegePhase = (int)(mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (int)(mx * 3);
        mopperPhase = (int)(mx * 4);
        splasherPhase = (int)(mx * 4);
        attackBasePhase = (int)(mx * 3);
        reservePaintPhase = (int)(mx * 1.5);
        if (mx < 30) {
            attackBasePhase = 0;  // may be beneficial to send immediately on small maps
        }
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
    }

    private static void updateGameState() throws GameActionException {
        turnsAlive++;
        roundNum = rc.getRoundNum();
        roundsSpentInQuadrant[Utils.currentQuadrant()]++;
        locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
        nearbyRobots = rc.senseNearbyRobots();
        nearbyTiles = rc.senseNearbyMapInfos();
    }

    private static void executeUnitLogic() throws GameActionException {
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
            case SPLASHER: runSplasher(); break;
            default: runTower(); break;
        }
    }

    public static void run(RobotController r) throws GameActionException {
        initializeGame(r);
        Debug.init();

        while (true) {
            try {
                updateGameState();
                if (!rc.getType().isTowerType()) {
                    ImpureUtils.updateNearestPaintTower();
                }
                executeUnitLogic();
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                if (roundNum != rc.getRoundNum()) {
                    System.out.println("~~~ Went over bytecode limit!! " + rc.getType() + ", role: " + role);
                    rc.setIndicatorLine(new MapLocation(0, 0), rc.getLocation(), 255, 0, 0);
                }
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    public static void runTower() throws GameActionException {
        Towers.run();
    }

    public static void runSoldier() throws GameActionException {
        Soldiers.run();
    }

    public static void runMopper() throws GameActionException {
        Moppers.run();
    }

    public static void runSplasher() throws GameActionException{
        Splashers.run();
    }
}
