package architecture;

import architecture.Actions.*;
import architecture.Tools.AuxConstants;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import battlecode.common.*;
import gavin.fast.FastMath;

import java.util.Random;


public class RobotPlayer {
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
        ACTION_REMOVE_ENEMY_PAINT, ACTION_ATTACK_SWING, ACTION_WAIT_COMPLETE_TOWER, ACTION_WAITING_FOR_ACTION
    }

    public enum Role {
        ROLE_SOLDIER,
        ROLE_SOLDIER_ATTACK,
        ROLE_SOLDIER_ATTACK_RUSH,
        ROLE_MOPPER,
        ROLE_SPLASHER,
        ROLE_TOWER
    }

    // General States
    public static Action action = Action.ACTION_WAITING_FOR_ACTION;
    public static Role role;

    // Map Size
    public static RobotController rc;
    public static int roundNum;
    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation mapCenter;


    // Location, Nearest
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
    public static int reserveMorePaintPhase;
    public static int reserveMorePaint = 500;



    // History of location
    public static boolean[][] nearbyAlliesMask;  // 5x5 area centered around robot
    public static boolean[][] nearbyEnemiesMask;
    public static MapLocation[] locationHistory = new MapLocation[8];


    // Patterns
    public static int mx;  // max of mapWidth and mapHeight
    public static boolean[][] paintPattern;
    public static boolean[][] moneyPattern;
    public static boolean[][] defensePattern;


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
        siegePhase = (mx * 3);  // cast to int, will be useful for tuning later
        fullFillPhase = (mx * 3);
        splasherPhase = (mx * 2);
        mopperPhase = (mx * 4);
        attackBasePhase = (mx * 3);
        reservePaintPhase = (int) (mx * 1.5);
        alwaysBuildDefenseTowerPhase = (mx * 8);
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
        ActionCompleteTower.init();
        ActionAttackWave.init();
        ActionMarkSRP.init(); // Using ref_best on turns < 500.
        ActionExplore.init(); // Explore in direction of spawn

        switch (role) {
            case Role.ROLE_SOLDIER_ATTACK_RUSH:
                ActionAttackRush.init();

            case Role.ROLE_SPLASHER:
                ActionSplash.init();
                break;

            case Role.ROLE_MOPPER:
                // Defend against rush
                if(rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length != 0){
                    action = Action.ACTION_ATTACK_SWING;
                }
                break;
        }


        while (true) {
            try {
                // Init turn
                Debug.println("Init turn " + rc.getType() + " " + rc.getLocation());


                roundNum = rc.getRoundNum();

                // Update sensing
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();
                nearbyRobots = rc.senseNearbyRobots();
                nearbyTiles = rc.senseNearbyMapInfos();
                nearbyRuins = rc.senseNearbyRuins(-1);

                // Update impure
                ImpureUtils.updateNearbyUnits();
                ImpureUtils.updateNearestEnemyTower();
                ImpureUtils.updateNearestPaintTower();
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
                        ActionGetPaint.run();

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
                        ActionGetPaint.run();

                        // Build tower
                        ActionCompleteTower.run();
                        ActionFillRuin.run();

                        // Build SRP
                        ActionCompleteSRP.run();
                        ActionFillSRP.run();
                        ActionMarkSRP.run();

                        break;


                    //------------------------------------------------------------------------------//
                    // Mopper
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_MOPPER:
                        ActionGetPaint.run();

                        // Tower
                        ActionCompleteTower.run();

                        action = Action.ACTION_WAITING_FOR_ACTION;
                        if(rc.getPaint() < 30 || FastMath.rand256() < 126){
                            ActionAttackSwing.run();
                            ActionFillRuin.run();
                        }else{
                            ActionFillRuin.run();
                            ActionAttackSwing.run();
                        }


                        // SRP
                        ActionCompleteSRP.run();
                        ActionFillSRP.run();
                        ActionMarkSRP.run();

                        // Basic
                        ActionRemoveEnemyPaint.run();
                        ActionGivePaint.run();

                        // End of turn update.
                        ActionMarkSRP.updateScores();
                        break;


                    //------------------------------------------------------------------------------//
                    // Splasher
                    //------------------------------------------------------------------------------//
                    case Role.ROLE_SPLASHER:
                        ActionGetPaint.run();

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
                        Debug.println("No role corresponding to " + role.name());
                }

                Debug.println("End of actions     : with " + action.name());
                Debug.println("Bytecodes : " + Clock.getBytecodeNum());

                if (action == Action.ACTION_WAITING_FOR_ACTION) {
                    ActionExplore.run();
                    ActionPaintUnder.run();
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

            if (rc.getRoundNum() > 1000) {
                rc.resign();
            }
        }
    }
}
