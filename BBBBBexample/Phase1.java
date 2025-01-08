package BBBBBexample;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers

public class Phase1 extends RobotPlayer{
    public static Direction direction;

    public static MapLocation target;

    public static void run (RobotController rc) throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
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
        if (curRuin != null){
            //rc.setIndicatorString(String.valueOf(curRuin.getMapLocation().x) + "," + curRuin.getMapLocation().y);
            //rc.setIndicatorString("Ruin nearby");
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);

            boolean fill = true;
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 20)){
                if((patternTile.getMark() != PaintType.EMPTY)&& (patternTile.getPaint() ==  PaintType.ENEMY_PRIMARY || patternTile.getPaint() ==  PaintType.ENEMY_SECONDARY)){
                    //rc.setIndicatorString("Cannot build");
                    fill = false;
                    break;
                }
            }

            if (rc.canMove(dir) && fill) {
                rc.move(dir);
            }

            if(rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)) {
                // Mark the pattern we need to draw to build a tower here if we haven't already.
                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                    rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                    System.out.println("Trying to build a tower at " + targetLoc);
                }
                // Fill in any spots in the pattern with the appropriate paint.

                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 20)){
                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                        if (rc.canAttack(patternTile.getMapLocation()))
                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                    }
                }

                // Complete the ruin if we can.
                if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                    rc.setTimelineMarker("Tower built", 0, 255, 0);
                    System.out.println("Built a tower at " + targetLoc + "!");
                }
            }
        }
        int r=0;
        int x=0;
        int y=0;
        int last = 0;


        if (target == null || rc.getLocation() == target) {
            r = rng.nextInt(4);
            last = r;
            switch (r){
                case 3:
                    y=0;
                    x=rng.nextInt(width);
                    break;
                case 2:
                    y = rc.getMapHeight()-1;
                    x=rng.nextInt(width);
                    break;
                case 1:
                    x = 0;
                    y=rc.getMapHeight()-1;
                    break;
                case 0:
                    x = rc.getMapWidth()-1;
                    y=rng.nextInt(height);
                    break;
            }
            target = new MapLocation(x,y);
        }

        Direction dir = rc.getLocation().directionTo(target);
        if(rc.senseMapInfo(rc.getLocation()).getMark() == PaintType.ENEMY_PRIMARY || rc.senseMapInfo(rc.getLocation()).getMark() == PaintType.ENEMY_SECONDARY) {
            for(MapInfo tile : rc.senseNearbyMapInfos(target, 8)){
                if(tile.getPaint().isAlly()){
                    dir=rc.getLocation().directionTo(tile.getMapLocation());
                }
            }
            if(rc.canMove(dir)){
                rc.move(dir);
            }
        } else {
            int tries = 0;
            rc.setIndicatorString(String.valueOf(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint()));
            while(!rc.canMove(dir) && tries < 20 && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY))) {
                tries = tries + 1;
                while(r == last) {
                    r = rng.nextInt(4);
                }

                switch (r){
                    case 3:
                        y=0;
                        x=rng.nextInt(width);
                        break;
                    case 2:
                        y = rc.getMapHeight()-1;
                        x=rng.nextInt(width);
                        break;
                    case 1:
                        x = 0;
                        y=rc.getMapHeight()-1;
                        break;
                    case 0:
                        x = rc.getMapWidth()-1;
                        y=rng.nextInt(height);
                        break;
                }
                target = new MapLocation(x,y);
                dir = rc.getLocation().directionTo(target);
            }
            if(rc.canMove(dir) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_PRIMARY)) && !(rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().equals(PaintType.ENEMY_SECONDARY))) {
                rc.move(dir);
            }
        }

        if (rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY){
            if(rc.canAttack(rc.getLocation())){
                rc.attack(rc.getLocation());
            }
        }
    }

}
