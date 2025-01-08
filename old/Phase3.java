package old;

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
            // for some reason throws an exception
            if (tile.hasRuin() && /*!rc.isLocationOccupied(tile.getMapLocation())*/
                !rc.canSenseRobotAtLocation(tile.getMapLocation())) {

                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curRuin = tile;
                }
            }
        }

        if (curRuin != null){
            //rc.setIndicatorString(String.valueOf(curRuin.getMapLocation().x) + "," + curRuin.getMapLocation().y);
            //rc.setIndicatorString("Ruin nearby");
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);

            boolean fill = true;
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
                if((patternTile.getMark() != PaintType.EMPTY)&& (patternTile.getPaint() ==  PaintType.ENEMY_PRIMARY || patternTile.getPaint() ==  PaintType.ENEMY_SECONDARY)){
                    //rc.setIndicatorString("Cannot build");
                    fill = false;
                    break;
                }
            }

            if (rc.canMove(dir) && fill) {
                rc.move(dir);
            }

            if(rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)) {
                // Mark the pattern we need to draw to build a tower here if we haven't already.
                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
                    rc.markTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
                    System.out.println("Trying to build a tower at " + targetLoc);
                }
                // Fill in any spots in the pattern with the appropriate paint.

                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                        if (rc.canAttack(patternTile.getMapLocation()))
                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                    }
                }

                // Complete the ruin if we can.
                if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
                    rc.setTimelineMarker("Tower built", 0, 255, 0);
                    System.out.println("Built a tower at " + targetLoc + "!");
                }
            }
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
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

        for(MapInfo tile : nearbyTiles) {
            if(rc.canCompleteResourcePattern(tile.getMapLocation())) {
                rc.completeResourcePattern(tile.getMapLocation());
            }
        }

        if(rc.canAttack(rc.getLocation()) && rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY) {
            rc.attack(rc.getLocation());
        } else {
            for (MapInfo tile : nearbyTiles){
                if (tile.getPaint() == PaintType.EMPTY){
                    if(rc.canAttack(tile.getMapLocation())){
                        rc.attack(tile.getMapLocation());
                        break;
                    }
                }
            }
        }


    }
}
