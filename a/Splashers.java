package a;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers
public class Splashers extends RobotPlayer{
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
            if (rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY)))
                rc.move(dir);
            if(rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
            }
        }

        if (target == null) {
            target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        }
        if(rc.getLocation() == target) {
            target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
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

        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }
    }

}
