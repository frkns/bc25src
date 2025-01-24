package ryan;

import battlecode.common.*;

public class Splashers extends RobotPlayer{

    public static MapLocation target;

    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;

    public static void run() throws GameActionException {
        MapRecorder.initTurn();
        // nearest paint tower is updated by default
        ImpureUtils.updateNearbyMask(true);
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
        MapLocation paintTarget = nearestPaintSource;
        if (paintTarget != null) {
            ImpureUtils.withdrawPaintIfPossible(paintTarget);
        }
        if (isRefilling && paintTarget != null) {
            target = paintTarget;
        }

        if (isRefilling && target != null) {
            Pathfinder.move(target);
            return;
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
        MapRecorder.recordSym(1000);
    }
}
