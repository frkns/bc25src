package smallrewrite;

import battlecode.common.*;

public class Phase1 extends RobotPlayer{
    public static Direction direction;
    public static MapLocation target;

    public static void run() throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        MapInfo curRuin = null;
        int distance = Integer.MAX_VALUE;
        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin() && !rc.canSenseRobotAtLocation(tile.getMapLocation())){
                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curRuin = tile;
                }
            }
        }
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for(RobotInfo robot : nearbyRobots){
            if(rc.canTransferPaint(robot.getLocation(),Math.max(-50, -1 * robot.getPaintAmount()))){
                rc.transferPaint(robot.getLocation(),Math.max(-50, -1 * robot.getPaintAmount()));
            }
        }

        boolean attack = false;

        UnitType towerType = UnitType.LEVEL_ONE_MONEY_TOWER;
        if (rc.getMoney() < 1000) {
            towerType = UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        if (rc.getRoundNum() < 10) {
            towerType = UnitType.LEVEL_ONE_PAINT_TOWER;
        }


        if(!FillPattern.play(rc, towerType, false)) {
            attack = true;
        };


        if(attack == true) {
            MapLocation fill = FillPattern.locatePattern(rc);
            if(fill != null) {
                FillPattern.fillInPattern(rc,fill);
            } else {
                FillPattern.fillInPattern(rc,rc.getLocation());
            }
        }

        // int r=0;
        // int x=0;
        // int y=0;
        // int last = 0;


        if (target == null || rc.getLocation() == target) {
            HeurisitcPath.move();
        }

    }
}
