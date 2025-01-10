package old;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers
public class Splashers extends RobotPlayer{
    static int nearestPaintTowerDistance = 999999;
    static MapLocation nearestPaintTower = null;
    public static MapLocation target;

    public static void run (RobotController rc) throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
        MapInfo curTile = null;
        int distance = 0;
        RobotInfo curBot = null;

        for (RobotInfo robot : nearbyRobots){
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER
                    || robot.getType() == UnitType.LEVEL_TWO_PAINT_TOWER
                    || robot.getType() == UnitType.LEVEL_THREE_PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }


        int paint = rc.getPaint();
        if (nearestPaintTower != null && paint < 70) {
            rc.setIndicatorString("Getting some paint!");
            int mxPaint = rc.getType().paintCapacity;
            int amt = mxPaint - paint;
            if (rc.canTransferPaint(nearestPaintTower, -1 * amt)) {
                rc.transferPaint(nearestPaintTower, -1 * amt);
            }
            target = nearestPaintTower;
        } else
        if (target == null || rc.getLocation() == target) {
            if (rc.getRoundNum() % 2 == 0)
                target = Utils.randomEnemyLocation();
            else
                target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        }


        for (MapInfo tile : nearbyTiles){
            if (tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY){
                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) > distance && rc.canAttack(tile.getMapLocation())){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curTile = tile;
                }
            }

        }
        if (curTile != null){
            MapLocation targetLoc = curTile.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            if (paint >= 70 && rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY)))
                rc.move(dir);
            if(rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
            }
        }



        Direction dir = rc.getLocation().directionTo(target);
        int tries = 0;
        rc.setIndicatorString(String.valueOf(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint()));
        while(!rc.canMove(dir) && tries < 20) {
            tries = tries + 1;
            target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
            dir = rc.getLocation().directionTo(target);
        }
        if(rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY))) {
            rc.move(dir);
        }

        // MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        // if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
        //     rc.attack(rc.getLocation());
        // }
    }

}
