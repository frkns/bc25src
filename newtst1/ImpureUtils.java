package newtst1;
import battlecode.common.*;

// these Utils are NOT pure functions (i.e. they modify state / change global variables, etc.)

public class ImpureUtils extends RobotPlayer {

    static void updateNearestPaintTower() throws GameActionException {
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
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
            // begin EXP: less tile conflicts but also less SRPs built (seems bad)
            // if (abs_diff_x == 3 && abs_diff_y == 3) {  // corner + (1, 1)
            //     if (tile.isPassable() && tile.getPaint() != PaintType.ALLY_SECONDARY && tile.getPaint() != PaintType.EMPTY) {
            //         possibleSRP = false;
            //         break;
            //     }
            // }
            // end EXP
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                if (abs_diff_x == 3 && abs_diff_y == 3) {
                    continue;
                }
                if ((abs_diff_x == 3 && abs_diff_y == 1) || (abs_diff_x == 1 && abs_diff_y == 3)) {
                    continue;
                }
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
}
