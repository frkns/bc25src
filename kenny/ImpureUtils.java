package kenny;
import battlecode.common.*;

// these Utils are NOT pure functions (i.e. they modify state / change global variables, etc.)

public class ImpureUtils extends RobotPlayer {

    static void updateNearbyMask() throws GameActionException {  // can later change this to also update nearby enemies
        nearbyAlliesMask = new boolean[5][5];  // reset everything to false

        RobotInfo[] nearbyTiles5x5 = rc.senseNearbyRobots(9);  // includes 2 extra tiles which need to be skipped
        for (RobotInfo robot : nearbyTiles5x5) {
            if (rc.getTeam() == robot.getTeam()) {
                int i = robot.getLocation().x - rc.getLocation().x + 2;
                int j = robot.getLocation().y - rc.getLocation().y + 2;
                if (i < 0 || i > 4 || j < 0 || j > 4)
                    continue;  // skip
                nearbyAlliesMask[i][j] = true;
            }
        }
    }

    // really, after the change, this should be called updateNearestPaintTarget, because moppers/money/defense towers are inlcuded
    static void updateNearestPaintTower() throws GameActionException {
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam() && (robot.getType().isTowerType() || robot.getType() == UnitType.MOPPER)) {
                if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                        nearestPaintTowerIsPaintTower = true;
                    }
                } else if (!nearestPaintTowerIsPaintTower) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }
    }

    static void updateNearestEnemyTower() throws GameActionException {
        nearestEnemyTower = null;
        for (RobotInfo robot : nearbyRobots) {  // assumes non-defense tower
            if (robot.getTeam() != rc.getTeam() && robot.getType().isTowerType()) {
                MapLocation robotLoc = robot.getLocation();
                if (nearestEnemyTower == null || rc.getLocation().distanceSquaredTo(robotLoc) < rc.getLocation().distanceSquaredTo(nearestEnemyTower)) {
                    nearestEnemyTower = robot.getLocation();
                }
            }
        }
    }

    static void updateNearestEmptyTile() throws GameActionException {
        nearestEmptyTile = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            if (tile.getPaint() == PaintType.EMPTY && (nearestEmptyTile == null || rc.getLocation().distanceSquaredTo(tileLoc) < rc.getLocation().distanceSquaredTo(nearestEmptyTile))) {
                nearestEmptyTile = tileLoc;
            }
        }
    }

    static void updateNearestEnemyPaint() throws GameActionException {
        nearestEnemyPaint = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            if (tile.getPaint().isEnemy() && (nearestEnemyPaint == null || rc.getLocation().distanceSquaredTo(tileLoc) < rc.getLocation().distanceSquaredTo(nearestEnemyPaint))) {
                nearestEnemyPaint = tileLoc;
            }
        }
    }

    static void paintFloor() throws GameActionException {
        MapLocation floorTile = rc.getLocation();

        // canAttack is better than canPaint because it checks action cooldown?? (changing to this avoided an error)
        if (rc.canAttack(floorTile) && rc.senseMapInfo(floorTile).getPaint() == PaintType.EMPTY) {
            rc.attack(floorTile, false);  // primary
            // rc.attack(floorTile, true);  // secondary
        }
    }

    public static void withdrawPaintIfPossible(MapLocation withdrawTarget) throws GameActionException {
        if (rc.getLocation().isWithinDistanceSquared(withdrawTarget, 2)) {
            RobotInfo paintTower = rc.senseRobotAtLocation(withdrawTarget);
            if (paintTower == null)
                return;
            int paintTowerPaintAmt = paintTower.getPaintAmount();
            int transferAmt = Math.min(paintTowerPaintAmt, rc.getType().paintCapacity - rc.getPaint());
            if (rc.canTransferPaint(withdrawTarget, -1 * transferAmt))
                rc.transferPaint(withdrawTarget, -1 * transferAmt);
        }
    }

    public static void tryMarkSRP() throws GameActionException {
        int cx = rc.getLocation().x;
        int cy = rc.getLocation().y;
        if (cx - 2 < 0 || cy - 2 < 0 || cx + 2 >= mapWidth || cy + 2 >= mapHeight) {
            return;
        }
        boolean possibleSRP = true;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            if (!tile.isPassable()) {
                if (Utils.chessDistance(rc.getLocation(), tileLoc) <= 2) {
                    possibleSRP = false;
                    break;
                }
            }
            int abs_diff_x = Math.abs(cx - tileLoc.x);
            int abs_diff_y = Math.abs(cy - tileLoc.y);
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                if ((abs_diff_x == 4 && abs_diff_y == 0) || (abs_diff_x == 0 && abs_diff_y == 4)) {
                    continue;
                }
                possibleSRP = false;
                break;
            }
        }
        if (possibleSRP) {
            if (rc.canMark(rc.getLocation())) {
                rc.mark(rc.getLocation(), false);
            } else {
                System.out.println("Couldn't mark " + rc.getLocation());
            }
        }
    }

    public static void updateNearbyUnits() throws GameActionException {
        // post Sprint 1 balance change: avoid clumping of units
        nearbyFriendlyRobots = 1;  // fixes div 0 error and also includes ourself in the count
        nearbyEnemyRobots = 0;
        int sumx = rc.getLocation().x;
        int sumy = rc.getLocation().y;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                MapLocation loc = robot.getLocation();
                sumx += loc.x;
                sumy += loc.y;
                if (robot.getType().isTowerType()) {
                    if (rc.canUpgradeTower(loc)) {
                        rc.upgradeTower(loc);
                    }
                }
                nearbyFriendlyRobots++;
            } else {
                nearbyEnemyRobots++;
            }
        }
        avgClump = new MapLocation(sumx / nearbyFriendlyRobots, sumy / nearbyFriendlyRobots);
        rc.setIndicatorDot(avgClump, 0, 0, 255);
    }

    public static void checkAndCompleteNearbySRPs() throws GameActionException {
        // actually don't need this --
        // CAN BE REACHED can be reached because it might complete *another* robot's SRP
        // that they would've completed anyway on their turn

        // for (MapInfo tile : nearbyTiles) {
        //     // if (tile.isResourcePatternCenter()) {
        //     if (tile.getMark() == PaintType.ALLY_PRIMARY) {
        //         if (rc.canCompleteResourcePattern(tile.getMapLocation())) {
                       // CAN BE REACHED
        //             rc.completeResourcePattern(tile.getMapLocation());
        //             assert(rc.getLocation().distanceSquaredTo(tile.getMapLocation()) <= 9);
        //         }
        //     }
        // }
    }
}
