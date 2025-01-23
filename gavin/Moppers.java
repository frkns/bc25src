package gavin;

import battlecode.common.*;

public class Moppers extends RobotPlayer {

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;

    // should we stand in place this turn? resets to false at the end of every turn
    static boolean mopperStand = false; // unused at the moment

    // experimental
    // static int stopQuadrantModifierPhase = mx * 2; // pending deletion?
    /* */

    static MapLocation nearestEnemyPaintOnRuin;

    public static void run() throws GameActionException {

        // no bug nav for mopper
        // wallAdjacent = false;
        // for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
        //     if (tile.isWall()) {
        //         wallAdjacent = true;
        //         break;
        //     }
        // }
        // if (wallAdjacent) {
        //     if (wallRounds++ == 0 && target != null) {
        //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        //     }
        // } else {
        //     wallRounds = 0;
        //     if (target != null)
        //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        // }


        ImpureUtils.tryUpgradeNearbyTowers();

        ImpureUtils.updateNearbyMask(true);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyPaintOnRuin();
        ImpureUtils.updateNearestEnemyRobot();

        // if (Utils.selfDestructRequirementsMet()) {
        //     System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum()
        //             + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
        //     rc.disintegrate();
        // }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // selecting a random target location on the map has an inherent bias towards
            // the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our
            // current quadrant
            /*
             * if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() <
             * stopQuadrantModifierPhase)
             * target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
             * else
             */ {
                target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            }
            lastTargetChangeRound = rc.getRoundNum();
        }

        // if (nearestEnemyPaint != null &&
        // rc.getLocation().distanceSquaredTo(nearestEnemyPaint) <= 2)
        // mopperStand = true;

        if (!mopperStand)
            HeuristicPath.mopperMove(target);

        // mop vs mop swing relative scoring logic
        // let's suppose that removing enemy paint from a tile has a value of ~10 paint

        // mop (10 action cooldown):
        // * enemy robot mop = -10 enemy and +5 mopper, net = +15 = 15
        // * enemy robot on enemy paint = +15 + 10 = 25
        // * enemy paint = 0 + 10 = 10
        // mop swing (20 action cooldown):
        // * can hit up to 6 and remove 5 from each for a max of +30, but has double
        // action cooldown

        // also, we should take into account the removing of enemy paint from a ruin

        // "mop" tile action scoring, scores are multiplied by 10 for easy calcs
        MapLocation bestAttackTarget = null;
        int bestScore = 0;
        MapInfo[] veryNearbyTiles = rc.senseNearbyMapInfos(2);
        for (MapInfo tile : veryNearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            if (!rc.canAttack(tileLoc))
                continue;
            int score = 0;
            boolean hasEnemyPaint = tile.getPaint().isEnemy();
            if (rc.canSenseRobotAtLocation(tileLoc)) {
                RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                if (robot.getTeam() != rc.getTeam()) {
                    if (robot.getPaintAmount() > 0) {
                        score += 50;  // this tile has an enemy robot with paint
                    }
                    if (rc.getPaint() < 100) {
                        score += 100;  // if we're not full paint
                    }
                }
            }
            if (hasEnemyPaint) {
                score += 100; // this tile has enemy paint there
                if (tileLoc.equals(nearestEnemyPaintOnRuin)) {
                    score += 130; // this tile is also part of a ruin
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestAttackTarget = tileLoc;
            }
        }
        if (bestAttackTarget != null) {
            rc.attack(bestAttackTarget);
        }

        ImpureUtils.updateNearbyMask(true);

        // for (int i = 0; i < 5; i++) {
        // for (int j = 0; j < 5; j++) {
        // int x = rc.getLocation().x + i - 2;
        // int y = rc.getLocation().y + j - 2;
        // rc.setIndicatorDot(new MapLocation(x, y), 0, 0, 0);
        // if (nearbyAlliesMask[i][j]) {
        // rc.setIndicatorDot(new MapLocation(x, y), 0, 255, 0);
        // }
        // }
        // }

        Direction bestSwingDir = null;
        int bestSwingScore = 0;

        int score = 0;
        for (int i = 1; i < 4; i++) {
            score += (nearbyEnemiesMask[i][0] ? 1 : 0) + (nearbyEnemiesMask[i][1] ? 1 : 0);
        }
        if (score > bestSwingScore) {
            if (rc.canMopSwing(Direction.SOUTH)) {
                bestSwingScore = score;
                bestSwingDir = Direction.SOUTH;
            }
        }
        score = 0;
        for (int i = 1; i < 4; i++) {
            score += (nearbyEnemiesMask[i][3] ? 1 : 0) + (nearbyEnemiesMask[i][4] ? 1 : 0);
            // int x = rc.getLocation().x + i - 2;
            // int y = rc.getLocation().y + 3 - 2;
            // rc.setIndicatorDot(new MapLocation(x, y), 0, 0, 0);
            // x = rc.getLocation().x + i - 2;
            // y = rc.getLocation().y + 4 - 2;
            // rc.setIndicatorDot(new MapLocation(x, y), 0, 0, 0);
        }
        if (score > bestSwingScore) {
            if (rc.canMopSwing(Direction.NORTH)) {
                bestSwingScore = score;
                bestSwingDir = Direction.NORTH;
            }
        }
        score = 0;
        for (int j = 1; j < 4; j++) {
            score += (nearbyEnemiesMask[0][j] ? 1 : 0) + (nearbyEnemiesMask[1][j] ? 1 : 0);
        }
        if (score > bestSwingScore) {
            if (rc.canMopSwing(Direction.WEST)) {
                bestSwingScore = score;
                bestSwingDir = Direction.WEST;
            }
        }
        score = 0;
        for (int j = 1; j < 4; j++) {
            score += (nearbyEnemiesMask[3][j] ? 1 : 0) + (nearbyEnemiesMask[4][j] ? 1 : 0);
        }
        if (score > bestSwingScore) {
            if (rc.canMopSwing(Direction.EAST)) {
                bestSwingScore = score;
                bestSwingDir = Direction.EAST;
            }
        }

        // if (rc.canMopSwing(Direction.NORTH)) {
        // assert (rc.canMopSwing(Direction.SOUTH)); // assertion fails!
        // assert (rc.canMopSwing(Direction.EAST));
        // assert (rc.canMopSwing(Direction.WEST));
        // }

        // must be able to hit 2 robots with sweep for it to trigger
        if (bestSwingScore >= 2 && rc.canMopSwing(bestSwingDir)) {
            rc.mopSwing(bestSwingDir);
            rc.setIndicatorDot(rc.getLocation().add(bestSwingDir), 255, 255, 255);
            rc.setIndicatorDot(rc.getLocation().add(bestSwingDir).add(bestSwingDir), 255, 255, 255);
        }


        // try to transfer paint if we are filled
        if (rc.getPaint() > 90) {
            bestScore = 0;
            MapLocation bestLoc = null;
            int bestTransferAmt = 0;

            RobotInfo[] adjRobots = rc.senseNearbyRobots(2);
            for (RobotInfo robot : adjRobots) {
                if (robot.getTeam() == rc.getTeam()) {
                    score = 0;
                    int transferAmt = Math.min(50, robot.getType().paintCapacity - robot.getPaintAmount());
                    score += transferAmt;
                    if (robot.getType() == UnitType.SPLASHER) {
                        score += 15;
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        bestLoc = robot.getLocation();
                        bestTransferAmt = transferAmt;
                    }
                }
            }

            if (bestScore > 0) {
                if (rc.canTransferPaint(bestLoc, bestTransferAmt)) {
                    rc.transferPaint(bestLoc, bestTransferAmt);
                }
            }

        }
    }
}
