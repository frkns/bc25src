package heuristic_test;

import battlecode.common.*;


//phase 3 for soldiers
//build defence towers and attack the enemy
public class Phase3 extends RobotPlayer{
    public static MapLocation target;
    public static Direction direction;

    public static void run (RobotController rc) throws GameActionException {

        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(20);
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(20);

        RobotInfo curTower = null;



        int distance = Integer.MAX_VALUE;

        for (RobotInfo robot : nearbyRobots){

            if(rc.canUpgradeTower(robot.getLocation())) {
                rc.upgradeTower(robot.getLocation());
            }

            // if (rc.getTeam() != robot.getTeam() && robot.getType().isTowerType() && rc.canAttack(robot.getLocation())) {
            //     rc.attack(robot.getLocation());
            // }

            if (robot.getTeam() != rc.getTeam() && robot.getType() != UnitType.MOPPER && robot.getType() != UnitType.SOLDIER && robot.getType() != UnitType.SPLASHER) {
                if(robot.getLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = robot.getLocation().distanceSquaredTo(rc.getLocation());
                    curTower = robot;
                }
            }
        }

        if(curTower != null) {
            rc.setIndicatorString("Tower nearby");
            MapLocation targetLoc = curTower.getLocation();

            Direction dir = rc.getLocation().directionTo(targetLoc);
            if (rc.canMove(dir))
                rc.move(dir);

            if(rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
            }
        }

        MapInfo curRuin = null;
        distance = Integer.MAX_VALUE;

        for (MapInfo tile : nearbyTiles){
            // if (tile.getMapLocation().equals(target)){
            //     target = null;
            // }
            // for some reason throws an exception
            if (tile.hasRuin() && /*!rc.isLocationOccupied(tile.getMapLocation())*/
                !rc.canSenseRobotAtLocation(tile.getMapLocation())) {

                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curRuin = tile;
                }
            }
        }

        boolean attack = false;
        if(!FillPattern.play(rc,UnitType.LEVEL_ONE_DEFENSE_TOWER,false)) {
            attack = true;
        }
        //System.out.println("ran");

        if(attack == true) {
            MapLocation fill = FillPattern.locatePattern(rc);
            if(fill != null) {
                FillPattern.fillInPattern(rc,fill);
            } else {
                FillPattern.fillInPattern(rc,rc.getLocation());
            }
        }

        if (target == null) {
            target = new MapLocation(rng.nextInt(width-1),rng.nextInt(height-1));
        }
        if(rc.getLocation().equals(target)) {
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
        if(rc.canMove(dir)) {
            rc.move(dir);
        }

        // HeurisitcPath.move(target);

        for(MapInfo tile : nearbyTiles) {
            if(rc.canCompleteResourcePattern(tile.getMapLocation())) {
                rc.completeResourcePattern(tile.getMapLocation());
            }
        }

    }
}
