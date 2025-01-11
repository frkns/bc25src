package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class BuildSRP extends Action {
    public RobotController rc;

    public MapLocation center = null;
    public MapInfo[] nearbyTiles;
    public MapLocation paintLoc = null;

    public BuildSRP(){
        rc = Robot.rc;
        name = "BUILD SRP";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    // Initialize variables specific to the function here


    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Check all nearby tiles for a visible 5x5 in which a SRP can be drawn
    public void calcScore() throws GameActionException {

        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        center = null;
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        int[][] corners = {
                {-2, -2},
                {2, 2},
                {-2, 2},
                {2, -2}
        };

        boolean[][] pattern = rc.getResourcePattern();
        int closest = Integer.MAX_VALUE;

        for(MapInfo tile : nearbyTiles) {

            int x = tile.getMapLocation().x;
            int y = tile.getMapLocation().y;
            if(y%3 == 2 &&  (x+((y-1)/3))%4 == 2) {
                if(rc.getLocation().distanceSquaredTo(tile.getMapLocation()) < closest) {
                    boolean found = true;

                    for(int i = 0 ; i < 4 ; i ++) {
                        if(!rc.canSenseLocation(new MapLocation(tile.getMapLocation().x + (corners[i][0]), tile.getMapLocation().y + (corners[i][1])))) {
                            found = false;break;
                        }
                    }
                    if(found) {
                        closest = tile.getMapLocation().distanceSquaredTo(tile.getMapLocation());
                        center = new MapLocation(tile.getMapLocation().x, tile.getMapLocation().y);
                    }
                }
            }
        }

        if(center != null) {
            if(rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
            } else {
                paintLoc = null;

                for(MapInfo tile : nearbyTiles) {
                    int x = tile.getMapLocation().x;
                    int y = tile.getMapLocation().y;

                    if(x >= center.x -2 && x <= center.x + 2 && y >= center.y -2 && y <= center.y + 2) {

                        if(tile.hasRuin() || tile.isWall()) {
                            score = 0;
                            cooldown_reqs = 0;
                            return;
                        }

                        if(tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                            score = 0;
                            cooldown_reqs = 0;
                            return;
                        }
                        if(tile.getPaint() == PaintType.ALLY_SECONDARY && !pattern[x-center.x+2][y-center.y+2]) {
                            if(Robot.nearbyRuins.length > 0) {
                                score = 0;
                                cooldown_reqs = 0;
                                return;
                            } else {
                                paintLoc = tile.getMapLocation();
                            }
                        }
                        if(tile.getPaint() == PaintType.EMPTY) {
                            paintLoc = tile.getMapLocation();
                        }
                        if(tile.getPaint() == PaintType.ALLY_PRIMARY && pattern[x-center.x+2][y-center.y+2]) {
                            paintLoc = tile.getMapLocation();
                        }
                    }
                }
                score = Constants.BuildSRP;
                cooldown_reqs = 3;
                return;
            }
        } else {
            score = 0;
            cooldown_reqs = 0;
            return;
        }
        score = 0;
        cooldown_reqs = 0;
    }

    public int getScore(){
        return score;
    }

    //If there is a valid pattern, move towards and fill in the pattern

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if(paintLoc != null) {

            Painting.fillInPattern(paintLoc);

            if(rc.canMove(rc.getLocation().directionTo(center))) {
                rc.move(rc.getLocation().directionTo(center));
            }
        }
    }

    // Add helper functions here

}
