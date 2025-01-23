package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.*;

// these Utils are NOT pure functions (i.e. they modify state / change global variables, etc.)

public class ImpureUtils extends RobotPlayer {
    public static void updateNearestEmptyRuins() throws GameActionException{
        nearestEmptyRuin = null;
        int minDistance = 3600;

        for(MapLocation ruin: nearbyRuins){
            if(!rc.canSenseRobotAtLocation(ruin)){
                int distance = rc.getLocation().distanceSquaredTo(ruin);
                if(distance < minDistance){
                    minDistance = distance;
                    nearestEmptyRuin = ruin;
                }
            }
        }
    }

    static void tryUpgradeNearbyTowers() throws GameActionException {
        for (RobotInfo robot : nearbyRobots) {
            if (rc.canUpgradeTower(robot.getLocation())) {
                if (rc.getMoney() < 6000) {
                    if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                        if (robot.getHealth() <= AuxConstants.defenseTowerHealth[robot.getType().level - 1] * 1/4) {
                            continue;
                        }
                    } else {
                        if (robot.getHealth() <= AuxConstants.paintMoneyTowerHealth[robot.getType().level - 1] * 3/4) {
                            continue;
                        }
                    }
                }
                // if it's a defense tower do not upgrade if its health <= ~1/4 full (ok after a certain money threshold)
                // if not a defense tower, do not upgrade if its health <= ~3/4 full (ok after a certain money threshold)
                rc.upgradeTower(robot.getLocation());
            }
        }
    }

    public static void updateNearbyMask(boolean alsoUpdateEnemies) throws GameActionException {
        nearbyFriendlyRobots = 1;  // fixes div 0 error and also includes ourself in the count
        nearbyEnemyRobots = 0;

        nearbyAlliesMask = new boolean[5][5];  // reset everything to false
        if (alsoUpdateEnemies)
            nearbyEnemiesMask = new boolean[5][5];  // reset everything to false

        RobotInfo[] nearbyTiles5x5 = rc.senseNearbyRobots(8);
        for (RobotInfo robot : nearbyTiles5x5) {
            int i = robot.getLocation().x - rc.getLocation().x + 2;
            int j = robot.getLocation().y - rc.getLocation().y + 2;
            if (rc.getTeam() == robot.getTeam()) {
                nearbyFriendlyRobots++;
                nearbyAlliesMask[i][j] = true;
            } else if (alsoUpdateEnemies) {
                nearbyEnemyRobots++;
                nearbyEnemiesMask[i][j] = true;
            }
        }
    }


    // really, after the change, this should be called updateNearestPaintTarget, because moppers/money/defense towers are inlcuded
    public static void updateNearestPaintTower() throws GameActionException {
        if (nearestPaintTower != null && rc.getLocation().distanceSquaredTo(nearestPaintTower) <= 20) {
            // if tower is destroyed or it's not a paint tower and there's no paint left
            if (!rc.canSenseRobotAtLocation(nearestPaintTower) || rc.senseRobotAtLocation(nearestPaintTower).getPaintAmount() == 0) {
                nearestPaintTower = null;
                nearestPaintTowerIsPaintTower = false;
            }
        }
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam() && (robot.getType().isTowerType())) {
                if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                        nearestPaintTowerIsPaintTower = true;
                    }
                } else if (!nearestPaintTowerIsPaintTower) {
                    if ((nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) && robot.getPaintAmount() > 0) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }
    }


    public static void updateNearestEnemyRobot() throws GameActionException {
        nearestEnemyRobot = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() != rc.getTeam() && !robot.getType().isTowerType()) {
                MapLocation robotLoc = robot.getLocation();
                if (nearestEnemyRobot == null || rc.getLocation().distanceSquaredTo(robotLoc) < rc.getLocation().distanceSquaredTo(nearestEnemyRobot)) {
                    nearestEnemyRobot = robot.getLocation();
                    nearestEnemyRobotInfo = robot;
                }
            }
        }
    }


    public static void updateNearestEnemyTower() throws GameActionException {
        // nearestEnemyTower = null;
        // sndNearestEnemyTower = null;
        if (nearestEnemyTower != null && rc.getLocation().isWithinDistanceSquared(nearestEnemyTower, 20) && (!rc.canSenseRobotAtLocation(nearestEnemyTower) || rc.senseRobotAtLocation(nearestEnemyTower).getTeam() == rc.getTeam())) {
            nearestEnemyTower = null;  // invalidation
        }
        if (sndNearestEnemyTower != null && rc.getLocation().isWithinDistanceSquared(sndNearestEnemyTower, 20) && (!rc.canSenseRobotAtLocation(sndNearestEnemyTower) || rc.senseRobotAtLocation(sndNearestEnemyTower).getTeam() == rc.getTeam())) {
            sndNearestEnemyTower = null;  // invalidation
        }
        for (RobotInfo robot : nearbyRobots) {  // assumes non-defense tower
            if (robot.getTeam() != rc.getTeam() && robot.getType().isTowerType()) {
                MapLocation robotLoc = robot.getLocation();
                int distanceSquared = rc.getLocation().distanceSquaredTo(robotLoc);
                // Check if this tower is closer than the current nearest tower
                if (nearestEnemyTower == null || distanceSquared < rc.getLocation().distanceSquaredTo(nearestEnemyTower)) {
                    // Update the second nearest tower to be the current nearest tower
                    sndNearestEnemyTower = nearestEnemyTower;
                    sndNearestEnemyTowerType = nearestEnemyTowerType;
                    // Update the nearest tower to be this tower
                    nearestEnemyTower = robotLoc;
                    nearestEnemyTowerType = robot.getType().getBaseType();
                }
                // Check if this tower is closer than the current second nearest tower but not closer than the nearest tower
                else if (sndNearestEnemyTower == null || distanceSquared < rc.getLocation().distanceSquaredTo(sndNearestEnemyTower)) {
                    // Update the second nearest tower to be this tower
                    sndNearestEnemyTower = robotLoc;
                    sndNearestEnemyTowerType = robot.getType().getBaseType();
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


    public static void updateNearestEnemyPaint() throws GameActionException {
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
        Debug.println("Withdraw from " + withdrawTarget);
        if (rc.getLocation().isWithinDistanceSquared(withdrawTarget, 2)) {
            RobotInfo paintTower = rc.senseRobotAtLocation(withdrawTarget);
            if (paintTower == null)
                return;
            int paintTowerPaintAmt = paintTower.getPaintAmount();
            int transferAmt = Math.min(paintTowerPaintAmt, rc.getType().paintCapacity - rc.getPaint());
            if (rc.canTransferPaint(withdrawTarget, -transferAmt))
                rc.transferPaint(withdrawTarget, -transferAmt);
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
        // pending deletion
        // // post Sprint 1 balance change: avoid clumping of units
        // nearbyFriendlyRobots = 1;  // fixes div 0 error and also includes ourself in the count
        // nearbyEnemyRobots = 0;
        // int sumx = rc.getLocation().x;
        // int sumy = rc.getLocation().y;
        // for (RobotInfo robot : nearbyRobots) {
        //     if (robot.getTeam() == rc.getTeam()) {
        //         MapLocation loc = robot.getLocation();
        //         sumx += loc.x;
        //         sumy += loc.y;
        //         if (robot.getType().isTowerType()) {
        //             if (rc.canUpgradeTower(loc)) {
        //                 rc.upgradeTower(loc);
        //             }
        //         }
        //         nearbyFriendlyRobots++;
        //     } else {
        //         nearbyEnemyRobots++;
        //     }
        // }
        // avgClump = new MapLocation(sumx / nearbyFriendlyRobots, sumy / nearbyFriendlyRobots);
        // rc.setIndicatorDot(avgClump, 0, 0, 255);
    }
}
