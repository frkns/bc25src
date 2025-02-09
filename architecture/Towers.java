package architecture;

import architecture.Tools.ImpureUtils;
import architecture.Tools.Utils;
import battlecode.common.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Towers extends RobotPlayer {

    static boolean spawnedFirstMopper = false;
    static int[] lastSummonDirection = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    static int nonGreedyPhase = (int)(mx * 2);  // not used
    static int firstMopper = (int)(mx);

    static int numSpawnedUnits = 0;
    static Direction dirMiddle;

    // force the spawning of a unit if possible, bypassing reserve checks, resets to false end of round
    static boolean forceSpawn = false;

    static MapLocation fstEnemyTower;  // first target
    static boolean fstIsDefense;

    static MapLocation sndEnemyTower;
    static boolean sndIsDefense;

    static int msgUpdateRoundNum = -99;

    static UnitType spawn = UnitType.SOLDIER;
    static boolean canSpawnSplasher = false;


    public static boolean canSpawnSplasherFn() throws GameActionException {
        if (forceSpawn || rc.getRoundNum() < 3 || numSpawnedUnits < 1)
            return true;
        if (rc.getType().getBaseType() != UnitType.LEVEL_ONE_PAINT_TOWER)
            return true;
        if (rc.getMoney() - UnitType.SPLASHER.moneyCost >= reserveChips) {
            if (rc.getRoundNum() < reservePaintPhase)
                return true;
            if (rc.getPaint() - UnitType.SPLASHER.paintCost >= reservePaint && rc.getRoundNum() < reserveMorePaintPhase)
                return true;
            if (rc.getPaint() - UnitType.SPLASHER.paintCost >= reserveMorePaint)
                return true;

        }

        return false;
    }

    public static boolean hasEnoughResources() throws GameActionException {
        if (forceSpawn || rc.getRoundNum() < 3 || numSpawnedUnits < 1)
            return true;
        if (rc.getType().getBaseType() != UnitType.LEVEL_ONE_PAINT_TOWER)
            return true;
        if (rc.getMoney() - spawn.moneyCost >= reserveChips && (canSpawnSplasher || rc.getRoundNum() < splasherPhase)) {
            if (rc.getRoundNum() < reservePaintPhase)
                return true;
            if (rc.getPaint() - spawn.paintCost >= reservePaint && rc.getRoundNum() < reserveMorePaintPhase)
                return true;
            if (rc.getPaint() - spawn.paintCost >= reserveMorePaint)
                return true;

        }

        return false;
    }

    public static void run() throws GameActionException {
        assert(rc.getType().isTowerType());

        // debugging stuff
        if (rc.getRoundNum() <= 1 && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            System.out.println("Number of towers " + rc.getNumberTowers());
            System.out.println("Siege phase " + siegePhase);
            System.out.println("Mopper phase " + mopperPhase);
        }

        if (fstEnemyTower != null) {
            if (rc.getTeam() == Team.A)
                rc.setIndicatorLine(rc.getLocation(), fstEnemyTower, 0, 0, 0);
            else
                rc.setIndicatorLine(rc.getLocation(), fstEnemyTower, 255, 255, 200);
        }
        if (sndEnemyTower != null) {
            if (rc.getTeam() == Team.A)
                rc.setIndicatorLine(rc.getLocation(), sndEnemyTower, 70, 70, 70);
            else
                rc.setIndicatorLine(rc.getLocation(), sndEnemyTower, 255, 255, 0);
        }
        /* */

        int nearbySoldiers = 0;  // allies
        int nearbyMoppers = 0;
        int nearbySplashers = 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                switch (robot.getType()) {
                    case SOLDIER:
                        nearbySoldiers++;
                        break;
                    case MOPPER:
                        nearbyMoppers++;
                        break;
                    case SPLASHER:
                        nearbySplashers++;
                        break;
                }
            }
        }

        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyRobot();
        dirMiddle = rc.getLocation().directionTo(new MapLocation(mapWidth / 2, mapHeight /2));

        int r = rng.nextInt(100);

        canSpawnSplasher = canSpawnSplasherFn();

        spawn = UnitType.SOLDIER;
        if (rc.getRoundNum() >= mopperPhase && rc.getPaint() < 700) {
            if (r < 20) {
                spawn = UnitType.MOPPER;
            }
        } else if (rc.getRoundNum() >= splasherPhase) {
            if (r < 20) {
                spawn = UnitType.MOPPER;
            }
            else
            if (r < 70) {
                spawn = UnitType.SPLASHER;
            }
        }


        if (nearestEnemyRobot != null && nearbyMoppers < 2) {
            // "clog will mog" reactionary mopper
            if (rc.getRoundNum() < mx * 2 || canSpawnSplasher) {
                rc.setIndicatorString("there is a enemy robot nearby, spawning mopper");
                spawn = UnitType.MOPPER;
                forceSpawn = true;
            }
        }


        // determine which tile to spawn this UnitType
        MapInfo[] nearbyDiamond = rc.senseNearbyMapInfos(4);
        int bestScore = (int) -2e9;
        MapLocation bestLoc = null;

        for (MapInfo tile : nearbyDiamond) {
            MapLocation tileLoc = tile.getMapLocation();
            // rc.setIndicatorDot(tileLoc, 255, 255, 0);

            if (rc.canSenseRobotAtLocation(tileLoc))  // can't spawn here
                continue;


            int score = 0;
            score += min(tileLoc.x * 10, 6*10);  // wall avoidance
            score += min((mapWidth - tileLoc.x) * 10, 7*10);
            score += min(tileLoc.y * 10, 6*10);
            score += min((mapHeight - tileLoc.y) * 10, 7*10);

            Direction summonDirection = rc.getLocation().directionTo(tileLoc);

            if(numSpawnedUnits == 0){
                if(summonDirection == dirMiddle)
                    score += 2000;
            }


            // Better score if not spawn units in this direction recently
            score += min(5, rc.getRoundNum() - lastSummonDirection[summonDirection.ordinal()]) * 100;
            score += min(5, rc.getRoundNum() - lastSummonDirection[summonDirection.rotateLeft().ordinal()]) * 100;
            score += min(5, rc.getRoundNum() - lastSummonDirection[summonDirection.rotateRight().ordinal()]) * 100;

            // or if oposite recently direction
            score +=  max(0, 10 - lastSummonDirection[summonDirection.opposite().ordinal()]) * 50;


            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 1)) {
                score -= 500;  // add a cost for spawning closer
            }

            if (spawn == UnitType.MOPPER) {
                if (nearestEnemyPaint != null)
                    score -= min(1, Utils.chessDistance(tileLoc, nearestEnemyPaint)) * 1500; // add a cost for
                // spawning
                // further from
                // enemy paint
                if (nearestEnemyRobot != null)
                    score -= Utils.chessDistance(tileLoc, nearestEnemyRobot) * 4000;    // add a cost for spawning
                // further from enemy robot

                if (!tile.getPaint().isAlly()) {  // add cost if it's not our paint
                    score -= 1500;
                }

            }

            if (rc.getRoundNum() > 100)
                score += rng.nextInt((int)(Math.abs(score) * 0.3 + 1));  // add a bit of randomness

            if (score > bestScore) {
                bestScore = score;
                bestLoc = tileLoc;
            }
        }

        MapLocation nextLoc = bestLoc;

        if (nextLoc != null)
            if (hasEnoughResources())
                if (rc.canBuildRobot(spawn, nextLoc)) {
                    lastSummonDirection[rc.getLocation().directionTo(nextLoc).ordinal()] = rc.getRoundNum();

                    rc.buildRobot(spawn, nextLoc);
                    if (spawn == UnitType.MOPPER)
                        spawnedFirstMopper = true;
                    numSpawnedUnits++;
                }

        rc.attack(null);
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot  : robots) {
            if (robot.getTeam() != rc.getTeam()) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                }
            }
        }
        // try to transfer paint to nearby friendly robots if we have action cooldown left
        RobotInfo[] superNearbyRobots = rc.senseNearbyRobots(2);
        for (RobotInfo robot  : superNearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                MapLocation robotLoc = robot.getLocation();
                int robotPaint = robot.getPaintAmount();
                int towerPaint = rc.getPaint();
                int transferAmt = min(towerPaint, robot.getType().paintCapacity - robotPaint);
                if (rc.canTransferPaint(robotLoc, transferAmt)) {
                    // can towers transfer paint?
                    assert(false);  // apparantely not? pending deletion
                    System.out.println("Tower transfered paint");
                    rc.transferPaint(robotLoc, transferAmt);
                }
            }
        }


        forceSpawn = false;
    }
}
