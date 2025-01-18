package lmx;

import battlecode.common.*;
import java.util.Random;


public class RobotPlayer {
    //------------------------------------------------------------------------------//
    // Constants
    //------------------------------------------------------------------------------//
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

    public enum Behavior {
        // -- Theses are role for units --
        SOLDIER,
        SOLDIER_ATTACK,
        MOPPER,
        SPLASHER,
        TOWER,

        // -- Theses can be used for HeuristicPath --
        REFILL,
        WRONG_RUINS,
        WRONG_SRP,
        TOWER_MICRO,
        NONE
    }

    public static Behavior getDefaultBehavior(){
        return switch(rc.getType()){
            case UnitType.SOLDIER -> Behavior.SOLDIER;
            case UnitType.MOPPER -> Behavior.MOPPER;
            case UnitType.SPLASHER -> Behavior.SPLASHER;
            default -> Behavior.TOWER;
        };
    }

    //------------------------------------------------------------------------------//
    // Robot state
    //------------------------------------------------------------------------------//
    static RobotController rc;
    static int roundNum;
    static boolean isRefilling = false;
    static boolean isFillingRuin = false;
    static boolean inTowerRange = false;
    static int turnsAlive = 0;
    static Behavior behavior = Behavior.NONE; // Will be init when we have rc
    public static MapLocation[] locationHistory = new MapLocation[8];

    //------------------------------------------------------------------------------//
    // Game info
    //------------------------------------------------------------------------------//
    static int mapWidth;
    static int mapHeight;
    static int mx;  // max of mapWidth and mapHeight
    static MapLocation mapCenter;

    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;

    static MapLocation[] quadrantCenters = new MapLocation[4];
    static MapLocation[] quadrantCorners = new MapLocation[4];
    static int[] roundsSpentInQuadrant = new int[4];
    static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map

    //------------------------------------------------------------------------------//
    // Sensing
    //------------------------------------------------------------------------------//
    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyTiles;
    static MapLocation nearestPaintTower;  // can be money/defense tower if we haven't see a paint tower yet
    static MapLocation nearestEnemyTower;
    static MapLocation nearestEmptyTile;  // not used (update: we use it now for full fill)
    static MapLocation nearestEnemyPaint;

    static MapInfo curRuin;
    static boolean nearestPaintTowerIsPaintTower = false;
    static MapLocation nearestWrongInRuin;

    static MapLocation curSRP;
    static boolean isFillingSRP = false;
    static MapLocation nearestWrongInSRP;

    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;
    static boolean[][] nearbyAlliesMask = new boolean[5][5];  // 5x5 area centered around robot
    static boolean[][] nearbyEnemyMask = new boolean[5][5];


    //------------------------------------------------------------------------------//
    // Behavior
    //------------------------------------------------------------------------------//
    static int siegePhase;
    static int mopperPhase;
    static int fullFillPhase;
    static int attackBasePhase;
    static int selfDestructPhase = 300;
    static int numSpawnedUnits = 0;

    static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    static int selfDestructPaintThreshold = 50;

    static int startPaintingFloorTowerNum = 4;  // don't paint floor before this to conserve paint

    static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind
    static int reservePaint = 100;
    static int reserveChips = 1700;



    public static void run(RobotController r) throws GameActionException {
        Debug.init();
        Debug.print(0, "Init RobotPlayer.");

        //------------------------------------------------------------------------------//
        // Init infos
        //------------------------------------------------------------------------------//
        rc = r;
        behavior = getDefaultBehavior();
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
        attackBasePhase = (int)(mx * 3);
        reservePaintPhase = (int)(mx * 1.5);
        if (mx < 30) {
            attackBasePhase = 0;  // may be beneficial to send immediately on small maps
        }

        // Attackers waves each 10 turns
        if (behavior == Behavior.SOLDIER && rc.getRoundNum() >= attackBasePhase) {
            if ((rc.getRoundNum() / 10) % 3 == 0) {
                behavior = Behavior.SOLDIER_ATTACK;
            }
        }

        // ---- Init Behavior ----
        switch (behavior) {
            case Behavior.SOLDIER:
                AttackBase.init();
                break;
        }

        while (true) {
            try {
                Debug.print("Init turn : " + rc.getLocation() + " " + rc.getType().name());
                turnsAlive++;
                roundNum = rc.getRoundNum();

                roundsSpentInQuadrant[Utils.currentQuadrant()]++;

                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();

                if (!rc.getType().isTowerType())
                    ImpureUtils.updateNearestPaintTower();

                // ---- Play by role ----
                switch (behavior) {
                    case Behavior.SOLDIER_ATTACK:
                        AttackBase.run();
                        break;
                    case Behavior.SOLDIER:
                        Soldiers.run();
                        break;
                    case Behavior.MOPPER:
                        Moppers.run();
                        break;

                        // case SPLASHER:
                        // runSplasher();
                        // break;
                    case TOWER:
                        Towers.run();
                        break;
                    default:
                        Debug.print( "No case for behavior : " + behavior.name());
                }
                Debug.print("End turn.");
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                if (roundNum != rc.getRoundNum()) {
                    Debug.print("~~~ Went over bytecode limit!! " + rc.getType() + ", role: " + behavior);
                    rc.setIndicatorLine(new MapLocation(0, 0), rc.getLocation(), 255, 0, 0);
                }
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }
    }
}
