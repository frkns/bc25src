package architecture;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    // Constants and Utilities
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

    enum Action {
        ACTION_SRP,
        ACTION_RUINS,
        ACTION_ATTACK_MICRO,
        ACTION_EXPLORE,
        ACTION_SPLASH, ACTION_COMPLETE_TOWER, ACTION_COMPLETE_SRP, ACTION_GET_PAINT, ACTION_ATTACK_RUSH, ACTION_WAITING_FOR_ACTION
    }

    ;

    public enum Heuristic {
        HEURISTIC_REFILL,
        HEURISTIC_WRONG_RUINS,
        HEURISTIC_WRONG_SRP,
        HEURISTIC_TOWER_MICRO,
        HEURISTIC_MOPPER, HEURISTIC_SOLDIER, EXPLORE, HEURISTIC_FASTEST
    }

    public enum Role {
        ROLE_SOLDIER,
        ROLE_SOLDIER_ATTACK,
        ROLE_MOPPER,
        ROLE_TOWER,
        ROLE_SPLASHER
    }

    // General States
    static Action action = Action.ACTION_WAITING_FOR_ACTION;
    static Role role;
    static boolean inTowerRange = false;
    static int turnsAlive = 0;

    // Map Size
    static RobotController rc;
    static int roundNum;
    static int mapWidth;
    static int mapHeight;
    static MapLocation mapCenter;


    // Location, Nearest
    static MapInfo curRuin;
    static MapLocation curSRP;

    static MapInfo[] nearbyTiles;
    static RobotInfo[] nearbyRobots;
    static MapLocation[] nearbyRuins;

    static MapLocation nearestPaintTower;  // misnomer, can be money/defense tower if we haven't see a paint tower yet
    static MapLocation nearestEmptyTile;  // not used (update: we use it now for full fill)
    static MapLocation nearestEmptyRuin;
    static MapLocation nearestEnemyPaint;
    static MapLocation nearestEnemyRobot;  // non-tower
    static MapLocation nearestEnemyTower;
    static MapLocation sndNearestEnemyTower;  // if there is a second one
    static MapLocation spawnTowerLocation;

    static RobotInfo nearestEnemyRobotInfo;
    static UnitType nearestEnemyTowerType;  //
    static boolean nearestPaintTowerIsPaintTower = false;
    static UnitType sndNearestEnemyTowerType;
    static int nearbyFriendlyRobots;
    static int nearbyEnemyRobots;

    static MapLocation fstTowerTarget;  // what tower is our tower telling us to attack?
    static boolean fstTowerTargetIsDefense;
    static MapLocation sndTowerTarget;  // what tower is our tower telling us to attack?
    static boolean sndTowerTargetIsDefense;


    // Phase
    static int siegePhase;
    static int mopperPhase;
    static int splasherPhase;
    static int fullFillPhase;
    static int attackBasePhase;
    static int alwaysBuildDefenseTowerPhase;
    static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind
    static int reservePaint = 100;
    static int reserveChips = 1800;
    static int startPaintingFloorTowerNum = 4;  // don't paint floor before this to conserve paint
    static int strictFollowBuildOrderNumTowers = 4;

    // Self destruction (not sure if self destructing is worth it, needs more testing)
    static int selfDestructPhase = 300;
    static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    static int selfDestructPaintThreshold = 50;


    // History of location
    static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map
    static boolean[][] nearbyAlliesMask;  // 5x5 area centered around robot
    static boolean[][] nearbyEnemiesMask;
    static MapLocation[] quadrantCenters = new MapLocation[4];
    public static MapLocation[] locationHistory = new MapLocation[8];


    // Patterns
    static int mx;  // max of mapWidth and mapHeight
    static boolean[][] paintPattern;
    static boolean[][] moneyPattern;
    static boolean[][] defensePattern;

    // PathFinding
    static boolean wallAdjacent = false;  // might not use this maybe bugnav potential
    static int wallRounds = 0;
    static int sqDistanceToTargetOnWallTouch = (int) 2e9;


    // Others
    static UnitType spawnTowerType;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        //------------------------------------------------------------------------------//
        // Init variables
        //------------------------------------------------------------------------------//
        Debug.println("Init Robot " + rc.getType().name() + " " + rc.getLocation());

        // Maps
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        mapCenter = new MapLocation(mapWidth / 2, mapHeight / 2);
        quadrantCenters[0] = new MapLocation(3 * mapWidth / 4, 3 * mapHeight / 4);
        quadrantCenters[1] = new MapLocation(1 * mapWidth / 4, 3 * mapHeight / 4);
        quadrantCenters[2] = new MapLocation(1 * mapWidth / 4, 1 * mapHeight / 4);
        quadrantCenters[3] = new MapLocation(3 * mapWidth / 4, 1 * mapHeight / 4);

        // Patterns
        paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);

        // Spawn location
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


        // Phases
        mx = Math.max(mapWidth, mapHeight);  // ~60 for huge ~35 for medium
        siegePhase = (int) (mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (int) (mx * 3);
        splasherPhase = (int) (mx * 2);
        mopperPhase = (int) (mx * 4);
        attackBasePhase = (int) (mx * 3);
        reservePaintPhase = (int) (mx * 1.5);
        alwaysBuildDefenseTowerPhase = (int) (mx * 8);
        if (mx < 36) {
            AuxConstants.buildOrder[3] = UnitType.LEVEL_ONE_PAINT_TOWER;
        }


        // Role
        role = switch (rc.getType()) {
            case UnitType.SOLDIER -> Role.ROLE_SOLDIER;
            case UnitType.MOPPER -> Role.ROLE_MOPPER;
            case UnitType.SPLASHER -> Role.ROLE_SPLASHER;
            default -> Role.ROLE_TOWER;
        };
        Debug.println("Get role : " + role.name());



        // Promoting to more specific role
        if (rc.getType() == UnitType.SOLDIER && rc.getRoundNum() >= attackBasePhase) {
            // we do divison by ~10 first because we want to send the attackers in "waves"
            if ((rc.getRoundNum() / 10) % 3 == 0) {
                role = Role.ROLE_SOLDIER_ATTACK;
            }
        }


        // Init actions
        Debug.println("Init Actions.");
        ActionAttackRush.initRush();
        switch (role) {
            case Role.ROLE_SOLDIER_ATTACK:
                ActionAttackRush.initRush();
                break;

            case Role.ROLE_SPLASHER:
                ActionSplash.init();
                break;
        }


        while (true) {
            try {
                // Init turn
                Debug.println("Init turn " + rc.getType() + " " + rc.getLocation());
                Comms.readAndUpdateTowerTargets(rc.getRoundNum() - 1);
                Comms.readAndUpdateTowerTargets(rc.getRoundNum());

                turnsAlive++;
                roundNum = rc.getRoundNum();

                // Update sensing
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();
                nearbyRuins = rc.senseNearbyRuins(-1);

                // Update impure
                ImpureUtils.updateNearestEnemyTower();
                ImpureUtils.updateNearestPaintTower();
                ImpureUtils.updateNearbyMask(true); // true -> Also update enemie mask
                ImpureUtils.updateNearestEmptyRuins();

                if (!rc.getType().isTowerType())
                    ImpureUtils.updateNearestPaintTower();


                // Plays actions
                Debug.println("Start of actions   : as " + role.name() + " " + action.name());
                switch (role) {
                    case Role.ROLE_SOLDIER_ATTACK:
                        ActionAttackRush.run();   // Explore map to rush tower
                        ActionCompleteTower.run();
                        ActionAttackMicro.run();  // Just attack when see a tower
                        ActionFillRuin.run();
                        ActionFillSRP.run();
                        ActionGetPaintWhenLow.run();
                        break;

                    case Role.ROLE_SOLDIER:
                        ActionCompleteTower.run();
                        ActionFillRuin.run();
                        ActionFillSRP.run();

                        if(rc.getRoundNum() > siegePhase && turnsAlive > 10){
                            ActionAttackRush.run();

                            // We don't want soldier to stay in attackrush mode.
                            switch (action){case Action.ACTION_ATTACK_RUSH: action = Action.ACTION_WAITING_FOR_ACTION;}
                        }


                        ActionAttackMicro.run();
                        ActionGetPaintWhenLow.run();
                        break;

                    case Role.ROLE_MOPPER:
                        ActionCompleteTower.run();
                        ActionFillRuin.run();
                        ActionFillSRP.run();
                        ActionGetPaintWhenLow.run();
                        break;

                    case Role.ROLE_SPLASHER:
                        ActionCompleteTower.run();
                        ActionSplash.run();
                        ActionGetPaintWhenLow.run();
                        break;

                    case Role.ROLE_TOWER:
                        Towers.run();
                        break;

                    default:
                        Debug.println("No role correspondign to " + role.name());
                }
                Debug.println("End of actions     : with " + action.name());

                if(action == Action.ACTION_WAITING_FOR_ACTION){
                    ActionExplore.run();
                }
                rc.setIndicatorString(role.name() + " - " + action.name());
                Debug.println("End of turn. " + Clock.getBytecodeNum() + " bytecodes used.\n\n\n");



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
        }
    }
}
