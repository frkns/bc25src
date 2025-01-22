package architecture;

import architecture.Actions.*;
import architecture.Tools.AuxConstants;
import architecture.Tools.Comms;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    // Constants and Utilities
    public static final int dx8[] = {0, 1, 1, 1, 0, -1, -1, -1};
    public static final int dy8[] = {-1, -1, 0, 1, 1, 1, 0, -1};

    public static final Random rng = new Random();
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    public static final Direction[] directions4 = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };

    public enum Action {
        ACTION_ATTACK_RUSH,
        ACTION_ATTACK_WAVE,
        ACTION_ATTACK_MICRO,
        ACTION_SPLASH,

        ACTION_FILL_RUINS,
        ACTION_COMPLETE_TOWER,

        ACTION_MARK_SRP,
        ACTION_FILL_SRP,
        ACTION_COMPLETE_SRP,

        ACTION_EXPLORE,
        ACTION_GET_PAINT,
        ACTION_REMOVE_ENEMY_PAINT, ACTION_WAITING_FOR_ACTION
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
        ROLE_SOLDIER_ATTACK_RUSH, ROLE_SPLASHER
    }

    // General States
    public static Action action = Action.ACTION_WAITING_FOR_ACTION;
    public static Role role;
    public static int turnsAlive = 0;

    // Map Size
    public static RobotController rc;
    public static int roundNum;
    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation mapCenter;


    // Location, Nearest
    public static MapInfo curRuin;
    public static MapLocation curSRP;

    public static MapInfo[] nearbyTiles;
    public static RobotInfo[] nearbyRobots;
    public static MapLocation[] nearbyRuins;

    public static MapLocation nearestPaintTower;  // misnomer, can be money/defense tower if we haven't see a paint tower yet
    public static MapLocation nearestEmptyTile;  // not used (update: we use it now for full fill)
    public static MapLocation nearestEmptyRuin;
    public static MapLocation nearestEnemyPaint;
    public static MapLocation nearestEnemyRobot;  // non-tower
    public static MapLocation nearestEnemyTower;
    public static MapLocation sndNearestEnemyTower;  // if there is a second one
    public static MapLocation spawnTowerLocation;

    public static RobotInfo nearestEnemyRobotInfo;
    public static UnitType nearestEnemyTowerType;  //
    public static boolean nearestPaintTowerIsPaintTower = false;
    public static UnitType sndNearestEnemyTowerType;
    public static int nearbyFriendlyRobots;
    public static int nearbyEnemyRobots;

    public static MapLocation fstTowerTarget;  // what tower is our tower telling us to attack?
    public static boolean fstTowerTargetIsDefense;
    public static MapLocation sndTowerTarget;  // what tower is our tower telling us to attack?
    public static boolean sndTowerTargetIsDefense;


    // Phase
    public static int siegePhase;
    public static int mopperPhase;
    public static int splasherPhase;
    public static int fullFillPhase;
    public static int attackBasePhase;
    public static int alwaysBuildDefenseTowerPhase;
    public static int reservePaintPhase;  // it is really bad to reserve paint in the first few rounds because we'll fall behind
    public static int reservePaint = 100;
    public static int reserveChips = 1800;
    public static int startPaintingFloorTowerNum = 4;  // don't paint floor before this to conserve paint
    public static int strictFollowBuildOrderNumTowers = 4;
    public static int reserveMorePaintPhase;
    public static int reserveMorePaint = 500;


    // Self destruction (not sure if self destructing is worth it, needs more testing)
    public static int selfDestructPhase = 300;
    public static int selfDestructFriendlyRobotsThreshold = 20;  // > this to self destruct
    public static int selfDestructEnemyRobotsThreshold = 5;  // < this to self destruct
    public static int selfDestructPaintThreshold = 50;


    // History of location
    public static MapLocation avgClump;  // will eventually get rid of this one, in favor of 5x5 bool map
    public static boolean[][] nearbyAlliesMask;  // 5x5 area centered around robot
    public static boolean[][] nearbyEnemiesMask;
    public static MapLocation[] quadrantCenters = new MapLocation[4];
    public static MapLocation[] locationHistory = new MapLocation[8];


    // Patterns
    public static int mx;  // max of mapWidth and mapHeight
    public static boolean[][] paintPattern;
    public static boolean[][] moneyPattern;
    public static boolean[][] defensePattern;

    // PathFinding
    public static boolean wallAdjacent = false;  // might not use this maybe bugnav potential
    public static int wallRounds = 0;
    public static int sqDistanceToTargetOnWallTouch = (int) 2e9;


    // Others
    public static UnitType spawnTowerType;

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

        if (rc.getType() == UnitType.SOLDIER && spawnTowerType == UnitType.LEVEL_ONE_PAINT_TOWER && rc.getRoundNum() < 10) {
            role = Role.ROLE_SOLDIER_ATTACK_RUSH;
        }


        // Init actions
        Debug.println("Init Actions.");
        ActionCompleteTower.init();
        ActionAttackWave.init();
        ActionMarkSRP.init();

        switch (role) {
            case Role.ROLE_SOLDIER_ATTACK_RUSH:
                ActionAttackRush.init();

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
                    //------------------------------------------------------------------------------//
                    // Attack soldier
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_SOLDIER_ATTACK_RUSH:
                        ActionAttackRush.run();   // Explore map to rush tower, kill only one tower.
                        // + ROLE_SOLDIER_ATTACK

                    case Role.ROLE_SOLDIER_ATTACK:
                        ActionGetPaintWhenLow.run();

                        // Main behavior
                        ActionCompleteTower.run();
                        ActionAttackMicro.run();  // Just attack when see a tower
                        ActionAttackWave.run();

                        // If nothing else to do.
                        ActionFillRuin.run();
                        ActionCompleteTower.run();
                        ActionCompleteSRP.run();
                        ActionFillSRP.run();
                        break;


                    //------------------------------------------------------------------------------//
                    // Soldier
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_SOLDIER:
                        // Survive
                        ActionGetPaintWhenLow.run();

                        // Build tower
                        ActionCompleteTower.run();
                        ActionFillRuin.run();

                        // Build SRP
                        ActionCompleteSRP.run();
                        ActionFillSRP.run();
                        ActionMarkSRP.run();

                        // Attack if nothing else to do.
                        if (rc.getRoundNum() > siegePhase && turnsAlive > 10) {
                            // Rush for tower after 10 turns alive
                            ActionAttackWave.run();

                            // We don't want soldier to stay in attack rush mode.
                            if(action == Action.ACTION_ATTACK_RUSH) {
                                action = Action.ACTION_WAITING_FOR_ACTION;
                            }
                        }
                        ActionAttackMicro.run();

                        // Basic
                        ActionPaintUnder.run();

                        // End of turn update.
                        ActionMarkSRP.updateScores();
                        break;


                    //------------------------------------------------------------------------------//
                    // Mopper
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_MOPPER:
                        ActionGetPaintWhenLow.run();

                        // Tower
                        ActionCompleteTower.run();
                        ActionFillRuin.run();

                        // SRP
                        ActionCompleteSRP.run();
                        ActionFillSRP.run();
                        ActionMarkSRP.run();

                        // Basic
                        ActionRemoveEnemyPaint.run();

                        // End of turn update.
                        ActionMarkSRP.updateScores();
                        break;


                    //------------------------------------------------------------------------------//
                    // Splasher
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_SPLASHER:
                        ActionGetPaintWhenLow.run();

                        // Can validate pattern but not complete
                        ActionCompleteTower.run();
                        ActionCompleteSRP.run();

                        // Main behavior
                        ActionSplash.run();
                        break;


                    //------------------------------------------------------------------------------//
                    // Tower
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_TOWER:
                        // Default has ref_best
                        Towers.run();
                        break;

                    default:
                        Debug.println("No role correspondign to " + role.name());

                }
                Debug.println("End of actions     : with " + action.name());

                if (action == Action.ACTION_WAITING_FOR_ACTION) {
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

            if (rc.getRoundNum() > 200) {
                rc.resign();
            }

        }
    }
}
