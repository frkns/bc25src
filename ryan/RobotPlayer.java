package ryan;

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
    static int splasherPhase;
    static int fullFillPhase;
    static int attackBasePhase;
    static int fullAttackBasePhase;
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

    // static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map

    static boolean[][] nearbyAlliesMask;  // 5x5 area centered around robot
    static boolean[][] nearbyEnemiesMask;

    // some of these are unused
    static MapLocation[] quadrantCenters = new MapLocation[4];
    // static MapLocation[] quadrantCorners = new MapLocation[4];
    // static int[] roundsSpentInQuadrant = new int[4];

    static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind
    static int reservePaint = 100;
    static int reserveChips = 1800;
    static int reserveMorePaintPhase;
    static int reserveMorePaint = 500;

    static int mx;  // max of mapWidth and mapHeight

    static boolean[][] paintPattern;
    static boolean[][] moneyPattern;
    static boolean[][] defensePattern;

    static boolean wallAdjacent = false;  // might not use this maybe bugnav potential
    static int wallRounds = 0;
    static int sqDistanceToTargetOnWallTouch = (int) 2e9;

    static MapLocation fstTowerTarget;  // what tower is our tower telling us to attack?
    static boolean fstTowerTargetIsDefense;
    static MapLocation sndTowerTarget;  // what tower is our tower telling us to attack?
    static boolean sndTowerTargetIsDefense;

    static boolean visFstTowerTarget = false;
    static boolean visSndTowerTarget = false;

    // create an array of the 3 possible symetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];
    static int totalManDist;

    static int refillDistLimit = 40;  // don't refill if more than this number of manhattan units away from nearest paint tower

    static int birthRound;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        birthRound = rc.getRoundNum();
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

        nearbyRuins = rc.senseNearbyRuins(4);
        for (MapLocation ruinLoc : nearbyRuins) {
            if (!rc.canSenseRobotAtLocation(ruinLoc))
                continue;
            RobotInfo robot = rc.senseRobotAtLocation(ruinLoc);
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType().isTowerType()) {
                    // if (spawnTowerLocation == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(spawnTowerLocation)) {
                    spawnTowerLocation = robot.getLocation();
                    spawnTowerType = robot.getType().getBaseType();
                    // }
                    break;
                }
            }
        }

        if (spawnTowerLocation == null)  // it is possible that spawn tower is destroyed in the middle of the turn
            spawnTowerLocation = rc.getLocation();

        if (spawnTowerType == UnitType.LEVEL_ONE_MONEY_TOWER
                && rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, spawnTowerLocation)) {
            rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, spawnTowerLocation);
        }

        AttackBase.init();

        mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium
        siegePhase = (int)(mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (int)(mx * 3);
        mopperPhase = (int)(mx * 2);
        splasherPhase = (int)(mx * 3);
        attackBasePhase = (int)(mx * 3);
        fullAttackBasePhase = (int)(mx * 8);
        reservePaintPhase = (int)(mx * 1.5);
        reserveMorePaintPhase = (int)(mx * 10);
        alwaysBuildDefenseTowerPhase = (int)(mx * 10);


        if (rc.getType() == UnitType.SOLDIER) {
            if (rc.getRoundNum() <= 3) {
                System.out.println("total man distance for 3 syms : " + totalManDist);
                if (totalManDist < 50 || mx < 33) {
                    if (spawnTowerType == UnitType.LEVEL_ONE_PAINT_TOWER) {
                        role = 1;  // on small/med maps send 2 to their paint tower
                    } else if (totalManDist < 30) {
                        role = 1;  // send from money tower if really close
                    }
                } else if (spawnTowerType == UnitType.LEVEL_ONE_PAINT_TOWER && rc.getRoundNum() == 3) {
                    role = 2;
                }
            }
            if (role == 0 && Utils.isAttackingBase()) {
                role = 1;
            }
        }

        if (role == 2) {
            RuinDotter.init();
        }


        if(mx < 36) {
            AuxConstants.buildOrder[4] = UnitType.LEVEL_ONE_PAINT_TOWER;
        }

        while (true) {
            try {
                Comms.readAndUpdateTowerTargets(rc.getRoundNum() - 1);
                // Comms.readAndUpdateTowerTargets(rc.getRoundNum());

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
                            case 2:
                                RuinDotter.run();
                                break;
                            default: runSoldier();
                        }
                        break;
                    }
                    case MOPPER: runMopper(); break;
                    case SPLASHER: runSplasher(); break;
                    default: runTower(); break;
                }

                // should be ok not to update nearbyRobots because we only do one nearest enemy tower update anyways
                for (MapLocation tileLoc : rc.senseNearbyRuins(-1)) {
                    if (!rc.canSenseRobotAtLocation(tileLoc))
                        continue;
                    RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                    if (robot.getType().isTowerType() && robot.getTeam() == rc.getTeam()
                            && rc.canSendMessage(robot.getLocation())) {
                        Comms.reportToTower(robot.getLocation());
                        break;
                    }
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

    public static void runSplasher() throws GameActionException{
        Splashers.run();
    }
}
