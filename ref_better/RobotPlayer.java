package ref_better;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    public static MapLocation[] locationHistory = new MapLocation[8];

    static final int dx8[] = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int dy8[] = {-1, -1, 0, 1, 1, 1, 0, -1};

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
    static final Direction[] directions4 = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST,
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
    static MapLocation[] nearbyRuins;
    static boolean nearestPaintTowerIsPaintTower = false;
    static MapLocation nearestPaintTower;  // misnomer, can be money/defense tower if we haven't see a paint tower yet
    static MapLocation nearestEmptyTile;  // not used (update: we use it now for full fill)
    static MapLocation nearestEnemyPaint;
    static MapLocation nearestEnemyRobot;  // non-tower
    static RobotInfo nearestEnemyRobotInfo;

    static MapLocation nearestEnemyTower;
    static UnitType nearestEnemyTowerType;  // base type

    static MapLocation sndNearestEnemyTower;  // if there is a second one
    static UnitType sndNearestEnemyTowerType;

    static MapInfo curRuin;
    static boolean isFillingRuin = false;
    static MapLocation nearestWrongInRuin;

    static MapLocation curSRP;
    static boolean isFillingSRP = false;
    static MapLocation nearestWrongInSRP;

    static int siegePhase;
    static int mopperPhase;
    static int fullFillPhase;
    static int attackBasePhase;
    static int alwaysBuildDefenseTowerPhase;

    // not sure if self destructing is worth it, needs more testing
    static int selfDestructPhase = 300;
    static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    static int selfDestructPaintThreshold = 50;

    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;

    static boolean inTowerRange = false;

    static int startPaintingFloorTowerNum = 4;  // don't paint floor before this to conserve paint

    static int role = 0;  // default = 0. can assign different roles to a type e.g. 1 = base attacker

    static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map

    static boolean[][] nearbyAlliesMask;  // 5x5 area centered around robot
    static boolean[][] nearbyEnemiesMask;

    // some of these are unused
    static MapLocation[] quadrantCenters = new MapLocation[4];
    // static MapLocation[] quadrantCorners = new MapLocation[4];
    // static int[] roundsSpentInQuadrant = new int[4];

    static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind
    static int reservePaint = 100;
    static int reserveChips = 1800;

    static int mx;  // max of mapWidth and mapHeight

    static boolean[][] paintPattern;
    static boolean[][] moneyPattern;
    static boolean[][] defensePattern;

    static boolean wallAdjacent = false;  // might not use this maybe bugnav potential
    static int wallRounds = 0;
    static int sqDistanceToTargetOnWallTouch = (int) 2e9;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        mapCenter = new MapLocation(mapWidth/2, mapHeight/2);
        quadrantCenters[0] = new MapLocation(3*mapWidth/4, 3*mapHeight/4);
        quadrantCenters[1] = new MapLocation(1*mapWidth/4, 3*mapHeight/4);
        quadrantCenters[2] = new MapLocation(1*mapWidth/4, 1*mapHeight/4);
        quadrantCenters[3] = new MapLocation(3*mapWidth/4, 1*mapHeight/4);
        // quadrantCorners[0] = new MapLocation(mapWidth-1, mapHeight-1);
        // quadrantCorners[1] = new MapLocation(0, mapHeight-1);
        // quadrantCorners[2] = new MapLocation(0, 0);
        // quadrantCorners[3] = new MapLocation(mapWidth-1, 0);

        paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);

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

        if (spawnTowerLocation == null)  // it is possible that spawn tower is destroyed in the middle of the turn
            spawnTowerLocation = rc.getLocation();


        mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium
        siegePhase = (int)(mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (int)(mx * 3);
        mopperPhase = (int)(mx * 4);
        attackBasePhase = (int)(mx * 3);
        reservePaintPhase = (int)(mx * 1.5);
        alwaysBuildDefenseTowerPhase = (int)(mx * 8);

        if (rc.getRoundNum() <= 3 && mx < 30) {
            if (spawnTowerType == UnitType.LEVEL_ONE_PAINT_TOWER) {
                role = 1;  // on small maps send 2 to their paint tower
            } else {
            }
        }

        // if (mx < 30) {
        //     attackBasePhase = 0;  // may be beneficial to send immediately on small maps
        // }

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

                // roundsSpentInQuadrant[Utils.currentQuadrant()]++;

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

    // public static void runSplasher(RobotController rc) throws GameActionException{
    //     Splashers.run();
    // }
}
