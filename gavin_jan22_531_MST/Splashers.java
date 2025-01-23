package gavin_jan22_531_MST;

import battlecode.common.*;

public class Splashers extends RobotPlayer{

    public static MapLocation target;

    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;

    public static void run() throws GameActionException {
        ImpureUtils.updateNearbyMask(true);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();


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
            HeuristicPath.refill(target);  // 1.
            return;
        }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            lastTargetChangeRound = rc.getRoundNum();
        }

        if (rc.isMovementReady())
            HeuristicPath.splasherMove(target);

        // find best attack location

        int scoreThreshold = 1100;  // score must reach this number in order to be considered

        int[] locScores = new int[8];
        MapLocation[] locs = new MapLocation[8];  // locations to splash, it should form a diamond

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

        nearbyTiles = rc.senseNearbyMapInfos(18);
        for (MapInfo tile : nearbyTiles) {
            if (tile.isWall())
                continue;

            MapLocation tileLoc = tile.getMapLocation();
            if (rc.canSenseRobotAtLocation(tileLoc)) {
                if (rc.senseRobotAtLocation(tileLoc).getTeam() != rc.getTeam()) {
                    for (int i = 8; i-- > 0;) {
                        if (locs[i].isWithinDistanceSquared(tileLoc, 2)) {
                            locScores[i] += 500;  // add score for being able to hit an enemy tower
                        }
                    }
                }
                continue;
            }

            if (tile.hasRuin())
                continue;

            if (tile.getPaint().isEnemy()) {
                for (int i = 8; i-- > 0;) {
                    if (locs[i].isWithinDistanceSquared(tileLoc, 2)) {
                        locScores[i] += 200;  // add score for being able to paint over enemy paint
                    }
                }
            } else if (tile.getPaint() == PaintType.EMPTY) {
                for (int i = 8; i-- > 0;) {
                    if (locs[i].isWithinDistanceSquared(tileLoc, 4)) {
                        locScores[i] += 100;  // add score for painting neutral
                    }
                }
            }
        }

        int mxScore = scoreThreshold;
        MapLocation mxLoc = null;
        for (int i = 8; i-- > 0;) {
            if (locScores[i] >= mxScore) {
                mxScore = locScores[i];
                mxLoc = locs[i];
            }
        }
        if (mxLoc != null)
            rc.attack(mxLoc);

    }

}
