package ref_best;

import battlecode.common.*;
import ref_best.Pathfinder;

public class Splashers extends RobotPlayer{

    public static MapLocation target;

    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;

    public static void run() throws GameActionException {
        // nearest paint tower is updated by default
        ImpureUtils.updateNearbyMask(false);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();

        if (fstTowerTarget != null) {
            MapLocation tileLoc = fstTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visFstTowerTarget = true;
            }
        }
        if (sndTowerTarget != null) {
            MapLocation tileLoc = sndTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visSndTowerTarget = true;
            }
        }

        isRefilling = rc.getPaint() < 100;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget != null) {
            ImpureUtils.withdrawPaintIfPossible(paintTarget);
        }
        if (isRefilling && paintTarget != null) {
            target = paintTarget;
            sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        }
        wallAdjacent = false;
        for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
            if (tile.isWall()) {
                wallAdjacent = true;
                break;
            }
        }
        if (wallAdjacent) {
            if (wallRounds++ == 0 && target != null) {
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
            }
        } else {
            wallRounds = 0;
            if (target != null)
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        }

        if (isRefilling && target != null) {
            // if (Utils.manhattanDistance(rc.getLocation(), target) > 50)
            // Pathfinder.move(target);
            // else
            HeuristicPath.refill(target);  // 1.
            return;
        }

        // if (nearestPaintTower != null && Utils.manhattanDistance(rc.getLocation(), nearestPaintTower) > refillDistLimit) {
        //     isRefilling = false;
        // }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            lastTargetChangeRound = rc.getRoundNum();
        }

        if (rc.isMovementReady()) {
            // if (nearestEnemyTower != null) {
                HeuristicPath.splasherMove(target);
            // } else {
                // Pathfinder.move(target);
            // }
        }

        // find best attack location

        int scoreThreshold = 1200;  // score must reach this number in order to be considered

        int[] locScores = new int[9];
        MapLocation[] locs = new MapLocation[9];  // locations to splash, it should form a diamond, updated to include center

        for (int i = 8; i-- > 0;) {
            Direction dir = directions[i];
            MapLocation newLoc = rc.adjacentLocation(dir);
            if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.WEST || dir == Direction.EAST) {
                newLoc = newLoc.add(dir);
            }
            locs[i] = newLoc;
            if (!rc.canAttack(newLoc)) {
                locScores[i] = (int) -2e9;  // if we can't attack it set it to -inf
            }
        }

        locs[8] = rc.getLocation();  // center
        if (!rc.canAttack(rc.getLocation())) {
            locScores[8] = (int) -2e9;  // if we can't attack it set it to -inf
        }

        nearbyTiles = rc.senseNearbyMapInfos(18);
        for (MapInfo tile : nearbyTiles) {
            if (tile.isWall())
                continue;

            MapLocation tileLoc = tile.getMapLocation();
            if (rc.canSenseRobotAtLocation(tileLoc)) {
                RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                if (robot.getTeam() != rc.getTeam() && robot.getType().isTowerType()) {
                    for (int i = 9; i-- > 0;) {
                        if (locs[i].isWithinDistanceSquared(tileLoc, /*2*/4)) {
                            locScores[i] += 1500;  // add score for being able to hit an enemy tower
                        }
                    }
                }
                continue;
            }

            if (tile.hasRuin())
                continue;

            if (tile.getPaint().isEnemy()) {
                for (int i = 9; i-- > 0;) {
                    if (locs[i].isWithinDistanceSquared(tileLoc, 2)) {
                        locScores[i] += 200;  // add score for being able to paint over enemy paint
                        if (rc.canSenseRobotAtLocation(tileLoc)) {
                            locScores[i] += 50;  // if there is a robot there (on either team) add some score
                        }
                    }
                }
            } else if (tile.getPaint() == PaintType.EMPTY) {
                for (int i = 9; i-- > 0;) {
                    if (locs[i].isWithinDistanceSquared(tileLoc, 4)) {
                        locScores[i] += 100;  // add score for painting neutral
                        if (rc.canSenseRobotAtLocation(tileLoc)) {
                            locScores[i] += 50;  // if there is a robot there (on either team) add some score
                        }
                    }
                }
            }
        }

        int mxScore = scoreThreshold;
        MapLocation mxLoc = null;
        for (int i = 9; i-- > 0;) {
            if (locScores[i] >= mxScore) {
                mxScore = locScores[i];
                mxLoc = locs[i];
            }
        }
        if (mxLoc != null)
            rc.attack(mxLoc);

    }

}
