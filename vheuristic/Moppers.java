package vheuristic;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers
public class Moppers extends RobotPlayer{
    public static MapLocation target;
    public static void run (RobotController rc) throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
        MapInfo curTile = null;
        int distance = Integer.MAX_VALUE;
        RobotInfo curBot = null;


        for (MapInfo tile : nearbyTiles){
            if (tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY){
                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curTile = tile;
                }
            }

        }

        MapInfo[] adj = rc.senseNearbyMapInfos(2);
        RobotInfo[] adjRobots = rc.senseNearbyRobots(2);
        for (RobotInfo robot : adjRobots) {
            MapLocation loc = robot.getLocation();
            if (robot.getTeam() != rc.getTeam() && rc.senseMapInfo(loc).getPaint().isEnemy() && rc.canAttack(loc)) {
                rc.attack(loc);
            }
        }
        for (MapInfo tile : adj) {
            if (tile.getPaint().isEnemy() && rc.canAttack(tile.getMapLocation())) {
                rc.attack(tile.getMapLocation());
            }
        }

        HeurisitcPath.enemyPaintPenalty = 10000;
        HeurisitcPath.neutralPaintPenalty = 10000;
    HeurisitcPath.targetIncentive = 2000;
        if (curTile != null){
            MapLocation targetLoc = curTile.getMapLocation();
            // Direction dir = rc.getLocation().directionTo(targetLoc);
            // if (rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY)))
            //     rc.move(dir);
            HeurisitcPath.move(targetLoc);
        } else {
            HeurisitcPath.move(Utils.mirror(spawnTowerLocation));
        }

        adj = rc.senseNearbyMapInfos(2);
        adjRobots = rc.senseNearbyRobots(2);
        for (RobotInfo robot : adjRobots) {
            MapLocation loc = robot.getLocation();
            if (robot.getTeam() != rc.getTeam() && rc.senseMapInfo(loc).getPaint().isEnemy() && rc.canAttack(loc)) {
                rc.attack(loc);
            }
        }
        for (MapInfo tile : adj) {
            if (tile.getPaint().isEnemy() && rc.canAttack(tile.getMapLocation())) {
                rc.attack(tile.getMapLocation());
            }
        }


        // for (RobotInfo robot : nearbyRobots){

        //     if (robot.getTeam() != rc.getTeam() && robot.getType() != UnitType.MOPPER && robot.getType() != UnitType.SOLDIER && robot.getType() != UnitType.SPLASHER) {
        //         if(robot.getLocation().distanceSquaredTo(rc.getLocation()) < distance){
        //             distance = robot.getLocation().distanceSquaredTo(rc.getLocation());
        //             curBot = robot;
        //         }
        //     }
        // }
        // if(curBot!= null) {
        //     MapLocation targetLoc = curBot.getLocation();

        //     if(rc.canAttack(targetLoc)) {
        //         rc.mopSwing(rc.getLocation().directionTo(targetLoc));
        //     }
        // }

        // if (target == null) {
        //     target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        // }
        // if(rc.getLocation() == target) {
        //     target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        // }

        // Direction dir = rc.getLocation().directionTo(target);
        // int tries = 0;
        // rc.setIndicatorString(String.valueOf(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint()));
        // while(!rc.canMove(dir) && tries < 20) {
        //     tries = tries + 1;
        //     target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        //     dir = rc.getLocation().directionTo(target);
        // }
        // if(rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY))) {
        //     rc.move(dir);
        // }

        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }
    }

}
