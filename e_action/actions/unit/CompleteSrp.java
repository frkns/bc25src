package e_action.actions.unit;

import e_action.knowledge._Info;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class CompleteSrp extends Action {
    public RobotController rc;

    public MapLocation center = null;
    public MapLocation paintLoc = null;

    public CompleteSrp(){
        rc = Robot.rc;
        name = "COMPLETE SRP";
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Check all nearby tiles for a visible 5x5 in which a SRP can be drawn
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        center = null;

        int[][] corners = {
                {-2, -2},
                {2, 2},
                {-2, 2},
                {2, -2}
        };

        boolean[][] pattern = rc.getResourcePattern();
        int closest = Integer.MAX_VALUE;

        for(MapInfo tile : _Info.nearbyTiles) {

            if(tile.getMapLocation().y%3 == 2 &&  (tile.getMapLocation().x+((tile.getMapLocation().y-1)/3))%4 == 2) {
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

                for(MapInfo tile : _Info.nearbyTiles) {

                    if(tile.getMapLocation().x >= center.x -2 && tile.getMapLocation().x <= center.x + 2 && tile.getMapLocation().y >= center.y -2 && tile.getMapLocation().y <= center.y + 2) {

                        if(tile.hasRuin() || tile.isWall()) {
                            score = 0;
                            return;
                        }

                        if(tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                            score = 0;
                            return;
                        }
                        if(tile.getPaint() == PaintType.ALLY_SECONDARY && !pattern[tile.getMapLocation().x-center.x+2][tile.getMapLocation().y-center.y+2]) {
                            if(_Info.nearbyRuins.length > 0) {
                                score = 0;
                                return;
                            } else {
                                paintLoc = tile.getMapLocation();
                            }
                        }
                        if(tile.getPaint() == PaintType.EMPTY) {
                            paintLoc = tile.getMapLocation();
                        }
                        if(tile.getPaint() == PaintType.ALLY_PRIMARY && pattern[tile.getMapLocation().x-center.x+2][tile.getMapLocation().y-center.y+2]) {
                            paintLoc = tile.getMapLocation();
                        }
                    }
                }
                score = Constants.CompleteSrpScore;
                targetLoc = paintLoc;
                return;
            }
        } else {
            score = 0;
            return;
        }
        score = 0;
    }

    //If there is a valid pattern, move towards and fill in the pattern
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if(paintLoc != null) {
            PaintSrpGrid.fillInPattern(paintLoc);
        }
    }

    // Add helper functions here

}
