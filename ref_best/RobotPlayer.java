package ref_best;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    public static MapLocation[] locationHistory = new MapLocation[8];

    // ------ Constants ------
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

    // ------ Robot state ------
    static RobotController rc;
    static int roundNum;
    static boolean isRefilling = false;
    static boolean isFillingRuin = false;
    static boolean inTowerRange = false;
    static int turnsAlive = 0;
    static int role = 0;  // default = 0. can assign different roles to a type e.g. 1 = base attacker

    // ------ Map Information ------
    static int mapWidth;
    static int mapHeight;
    static int mx;  // max of mapWidth and mapHeight
    static MapLocation mapCenter;
    static MapLocation[] quadrantCenters = new MapLocation[4];
    static MapLocation[] quadrantCorners = new MapLocation[4];
    static int[] roundsSpentInQuadrant = new int[4];

    // ------ Tower specific ------
    static MapLocation targetEnemyTower; // Which enemy tower we are telling the bunnies to attack

    // ------ Spawn and Base Information ------
    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;
    static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map

    // ------ Sensing State ------
    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyTiles;
    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;
    static boolean[][] nearbyAlliesMask = new boolean[5][5];  // 5x5 area centered around robot
    static boolean[][] nearbyEnemyMask = new boolean[5][5];

    // ------ Target Locations ------
    static MapLocation nearestPaintSource;  // can be money/defense tower if we haven't seen a paint tower yet
    static boolean paintTowerFound = false;
    static MapLocation towerTargetEnemyTower; // The tower location that our towers are instructing the bunnies to attack
    static MapLocation nearestEnemyTower;
    static MapLocation nearestEmptyTile;  // used for full fill
    static MapLocation nearestEnemyPaint;

    // ------ Ruin Management ------
    static MapInfo curRuin;
    static MapLocation nearestWrongInRuin;
    static MapLocation curSRP;
    static boolean isFillingSRP = false;
    static MapLocation nearestWrongInSRP;

    // ------ Game Phases ------
    static int nonGreedyPhase;
    static int firstMopper;
    static int splasherPhase;
    static int siegePhase;
    static int mopperPhase;
    static int fullFillPhase;
    static int attackBasePhase;
    static int selfDestructPhase = 300;
    static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind

    // ------ Resource Management ------
    static int reservePaint = 100;
    static int reserveChips = 1700;
    static int startPaintingFloorTowerNum = 4;  // don't paint floor before this to conserve paint

    // ------ Self Destruct Thresholds ------
    static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    static int selfDestructPaintThreshold = 50;


    public static void run(RobotController r) throws GameActionException {
        // ---- Init infos ----
        rc = r;
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        mapCenter = new MapLocation(mapWidth / 2, mapHeight / 2);
        quadrantCenters[0] = new MapLocation(3 * mapWidth / 4, 3 * mapHeight / 4);
        quadrantCenters[1] = new MapLocation(1 * mapWidth / 4, 3 * mapHeight / 4);
        quadrantCenters[2] = new MapLocation(1 * mapWidth / 4, 1 * mapHeight / 4);
        quadrantCenters[3] = new MapLocation(3 * mapWidth / 4, 1 * mapHeight / 4);
        quadrantCorners[0] = new MapLocation(mapWidth - 1, mapHeight - 1);
        quadrantCorners[1] = new MapLocation(0, mapHeight - 1);
        quadrantCorners[2] = new MapLocation(0, 0);
        quadrantCorners[3] = new MapLocation(mapWidth - 1, 0);

        mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium

        firstMopper = (int) (mx * 2);
        splasherPhase = (int) (mx * 1.5);  // Start spawning splashers earlier than moppers
        mopperPhase = (int) (mx * 4);

        nonGreedyPhase = (int) (mx * 2);  // allow other units to complete ruins / upgrade towers if money capped
        siegePhase = (int) (mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (int) (mx * 3);
        attackBasePhase = (int) (mx * 3);
        reservePaintPhase = (int) (mx * 1.5);
        if (mx < 30) {
            attackBasePhase = 0;  // may be beneficial to send immediately on small maps
        }


        if (rc.getType().isRobotType()) {
            spawnTowerLocation = rc.senseNearbyRuins(4)[0]; // Only 1 ruin within a squaredRadius of 4 from the robot's spawn, and that ruin must be in the same location of the spawn tower
            if (rc.canSenseRobotAtLocation(spawnTowerLocation)) { // Either it is our tower, or the enemy destroyed and built on it in the same turn
                RobotInfo robot = rc.senseRobotAtLocation(spawnTowerLocation);
                if (robot.getTeam() == rc.getTeam()) { // If it is our tower, set the spawn tower type
                    spawnTowerType = robot.getType().getBaseType();
                }
            }
        }

        if (rc.getType() == UnitType.SOLDIER && rc.getRoundNum() >= attackBasePhase) {
            // we do divison by ~10 first because we want to send the attackers in "waves"
            if ((rc.getRoundNum() / 10) % 3 == 0) {
                role = 1;
            }
        }

        // ---- Init role ----
        switch (rc.getType()) {
            case SOLDIER:
                if (role == 1) {
                    AttackBase.init();
                }
                break;
            case UnitType.SPLASHER:
                Splashers.init();
                break;
        }

        nearbyRobots = rc.senseNearbyRobots();
        Communication.init(rc);

        while (true) {
            try {
                // ---- Init turn ----
                turnsAlive++;
                roundNum = rc.getRoundNum();
                Communication.initTurn();
                roundsSpentInQuadrant[Utils.currentQuadrant()]++;

                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();

                if (!rc.getType().isTowerType())
                    ImpureUtils.updateNearestPaintSource();

                // ---- Play by role ----
                switch (rc.getType()) {
                    case SOLDIER: {
                        switch (role) {
                            case 1:
                                AttackBase.run();
                                break;
                            default:
                                runSoldier();
                        }
                        break;
                    }
                    case MOPPER:
                        runMopper();
                        break;
                    case SPLASHER:
                        runSplasher();
                        break;
                    default:
                        runTower();
                        break;
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

    public static void runSplasher() throws GameActionException {
        Splashers.run();
    }
}
