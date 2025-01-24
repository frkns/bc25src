package ref_best;

import battlecode.common.*;
import ref_best.Pathfinder;

public class Splashers extends RobotPlayer {

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
            HeuristicPath.refill(target); // 1.
            return;
        }

        // if (nearestPaintTower != null && Utils.manhattanDistance(rc.getLocation(),
        // nearestPaintTower) > refillDistLimit) {
        // isRefilling = false;
        // }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            lastTargetChangeRound = rc.getRoundNum();
        }

        if (rc.isMovementReady()) {
            MapLocation tt = Utils.chooseTowerTarget();
            if (tt != null && rc.getNumberTowers() > 8 && rc.isActionReady()) {
                if (rc.getID() % 2 == 0)
                    Pathfinder.move(tt);
                else
                    Pathfinder.move(target);
            } else {
                HeuristicPath.splasherMove(target);
            }
        }

        // find best attack location

        int scoreThreshold = 1200; // score must reach this number in order to be considered
        if (rc.getMoney() > 5000) {
            scoreThreshold = 800;
        }

        int[] locScores = new int[9];
        MapLocation[] locs = new MapLocation[9]; // locations to splash, it should form a diamond, updated to include
                                                 // center

        /* */
        // Unroll the first loop (i from 8 down to 0)
        // i=7
        Direction dir7 = directions[7];
        MapLocation newLoc7 = rc.adjacentLocation(dir7);
        if (dir7 == Direction.NORTH || dir7 == Direction.SOUTH || dir7 == Direction.WEST || dir7 == Direction.EAST) {
            newLoc7 = newLoc7.add(dir7);
        }
        locs[7] = newLoc7;
        if (!rc.canAttack(newLoc7)) {
            locScores[7] = (int) -2e9;
        }

        // i=6
        Direction dir6 = directions[6];
        MapLocation newLoc6 = rc.adjacentLocation(dir6);
        if (dir6 == Direction.NORTH || dir6 == Direction.SOUTH || dir6 == Direction.WEST || dir6 == Direction.EAST) {
            newLoc6 = newLoc6.add(dir6);
        }
        locs[6] = newLoc6;
        if (!rc.canAttack(newLoc6)) {
            locScores[6] = (int) -2e9;
        }

        // i=5
        Direction dir5 = directions[5];
        MapLocation newLoc5 = rc.adjacentLocation(dir5);
        if (dir5 == Direction.NORTH || dir5 == Direction.SOUTH || dir5 == Direction.WEST || dir5 == Direction.EAST) {
            newLoc5 = newLoc5.add(dir5);
        }
        locs[5] = newLoc5;
        if (!rc.canAttack(newLoc5)) {
            locScores[5] = (int) -2e9;
        }

        // i=4
        Direction dir4 = directions[4];
        MapLocation newLoc4 = rc.adjacentLocation(dir4);
        if (dir4 == Direction.NORTH || dir4 == Direction.SOUTH || dir4 == Direction.WEST || dir4 == Direction.EAST) {
            newLoc4 = newLoc4.add(dir4);
        }
        locs[4] = newLoc4;
        if (!rc.canAttack(newLoc4)) {
            locScores[4] = (int) -2e9;
        }

        // i=3
        Direction dir3 = directions[3];
        MapLocation newLoc3 = rc.adjacentLocation(dir3);
        if (dir3 == Direction.NORTH || dir3 == Direction.SOUTH || dir3 == Direction.WEST || dir3 == Direction.EAST) {
            newLoc3 = newLoc3.add(dir3);
        }
        locs[3] = newLoc3;
        if (!rc.canAttack(newLoc3)) {
            locScores[3] = (int) -2e9;
        }

        // i=2
        Direction dir2 = directions[2];
        MapLocation newLoc2 = rc.adjacentLocation(dir2);
        if (dir2 == Direction.NORTH || dir2 == Direction.SOUTH || dir2 == Direction.WEST || dir2 == Direction.EAST) {
            newLoc2 = newLoc2.add(dir2);
        }
        locs[2] = newLoc2;
        if (!rc.canAttack(newLoc2)) {
            locScores[2] = (int) -2e9;
        }

        // i=1
        Direction dir1 = directions[1];
        MapLocation newLoc1 = rc.adjacentLocation(dir1);
        if (dir1 == Direction.NORTH || dir1 == Direction.SOUTH || dir1 == Direction.WEST || dir1 == Direction.EAST) {
            newLoc1 = newLoc1.add(dir1);
        }
        locs[1] = newLoc1;
        if (!rc.canAttack(newLoc1)) {
            locScores[1] = (int) -2e9;
        }

        // i=0
        Direction dir0 = directions[0];
        MapLocation newLoc0 = rc.adjacentLocation(dir0);
        if (dir0 == Direction.NORTH || dir0 == Direction.SOUTH || dir0 == Direction.WEST || dir0 == Direction.EAST) {
            newLoc0 = newLoc0.add(dir0);
        }
        locs[0] = newLoc0;
        if (!rc.canAttack(newLoc0)) {
            locScores[0] = (int) -2e9;
        }

        // Center location
        locs[8] = rc.getLocation();
        if (!rc.canAttack(rc.getLocation())) {
            locScores[8] = (int) -2e9;
        }

        // Nearby tiles processing (kept as loop since variable length)
        nearbyTiles = rc.senseNearbyMapInfos(18);
        for (MapInfo tile : nearbyTiles) {
            if (tile.isWall())
                continue;

            MapLocation tileLoc = tile.getMapLocation();
            if (rc.canSenseRobotAtLocation(tileLoc)) {
                RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                if (robot.getTeam() != rc.getTeam() && robot.getType().isTowerType()) {
                    // Unroll inner loop for tower scoring
                    if (locs[8].isWithinDistanceSquared(tileLoc, 4))
                        locScores[8] += 1500;
                    if (locs[7].isWithinDistanceSquared(tileLoc, 4))
                        locScores[7] += 1500;
                    if (locs[6].isWithinDistanceSquared(tileLoc, 4))
                        locScores[6] += 1500;
                    if (locs[5].isWithinDistanceSquared(tileLoc, 4))
                        locScores[5] += 1500;
                    if (locs[4].isWithinDistanceSquared(tileLoc, 4))
                        locScores[4] += 1500;
                    if (locs[3].isWithinDistanceSquared(tileLoc, 4))
                        locScores[3] += 1500;
                    if (locs[2].isWithinDistanceSquared(tileLoc, 4))
                        locScores[2] += 1500;
                    if (locs[1].isWithinDistanceSquared(tileLoc, 4))
                        locScores[1] += 1500;
                    if (locs[0].isWithinDistanceSquared(tileLoc, 4))
                        locScores[0] += 1500;
                }
                continue;
            }

            if (tile.hasRuin())
                continue;

            if (tile.getPaint().isEnemy()) {
                // Unroll enemy paint scoring
                if (locs[8].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[8] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[8] += 50;
                }
                if (locs[7].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[7] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[7] += 50;
                }
                if (locs[6].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[6] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[6] += 50;
                }
                if (locs[5].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[5] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[5] += 50;
                }
                if (locs[4].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[4] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[4] += 50;
                }
                if (locs[3].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[3] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[3] += 50;
                }
                if (locs[2].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[2] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[2] += 50;
                }
                if (locs[1].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[1] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[1] += 50;
                }
                if (locs[0].isWithinDistanceSquared(tileLoc, 2)) {
                    locScores[0] += 200;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[0] += 50;
                }
            } else if (tile.getPaint() == PaintType.EMPTY) {
                // Unroll neutral paint scoring
                if (locs[8].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[8] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[8] += 50;
                }
                if (locs[7].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[7] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[7] += 50;
                }
                if (locs[6].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[6] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[6] += 50;
                }
                if (locs[5].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[5] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[5] += 50;
                }
                if (locs[4].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[4] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[4] += 50;
                }
                if (locs[3].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[3] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[3] += 50;
                }
                if (locs[2].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[2] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[2] += 50;
                }
                if (locs[1].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[1] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[1] += 50;
                }
                if (locs[0].isWithinDistanceSquared(tileLoc, 4)) {
                    locScores[0] += 100;
                    if (rc.canSenseRobotAtLocation(tileLoc))
                        locScores[0] += 50;
                }
            }
        }

        // Unroll max score calculation
        int mxScore = scoreThreshold;
        MapLocation mxLoc = null;

        if (locScores[8] >= mxScore) {
            mxScore = locScores[8];
            mxLoc = locs[8];
        }
        if (locScores[7] >= mxScore) {
            mxScore = locScores[7];
            mxLoc = locs[7];
        }
        if (locScores[6] >= mxScore) {
            mxScore = locScores[6];
            mxLoc = locs[6];
        }
        if (locScores[5] >= mxScore) {
            mxScore = locScores[5];
            mxLoc = locs[5];
        }
        if (locScores[4] >= mxScore) {
            mxScore = locScores[4];
            mxLoc = locs[4];
        }
        if (locScores[3] >= mxScore) {
            mxScore = locScores[3];
            mxLoc = locs[3];
        }
        if (locScores[2] >= mxScore) {
            mxScore = locScores[2];
            mxLoc = locs[2];
        }
        if (locScores[1] >= mxScore) {
            mxScore = locScores[1];
            mxLoc = locs[1];
        }
        if (locScores[0] >= mxScore) {
            mxScore = locScores[0];
            mxLoc = locs[0];
        }

        if (mxLoc != null)
            rc.attack(mxLoc);
        /* */

    }

}
