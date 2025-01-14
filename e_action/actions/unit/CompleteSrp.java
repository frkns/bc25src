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

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        // 2 conditions to check if a tile is the center of potential SRP, assuming we start tiling from the bottom left.
        // y % 3 == 2
        // (x + (y/3)) % 4 == 2       (The x coordinate is shifted by to the left by 1 for every 3 tiles up)
        boolean[][] pattern = rc.getResourcePattern();
        if (center == null){
            int dy = (2 - _Info.robotLoc.y % 3) % 3; // Distance downwards to the nearest center
            int x = _Info.robotLoc.x + ((_Info.robotLoc.y + dy) / 3);
            int dx = (2 - x % 4 + 4) % 4; // Distance leftwards to the nearest
            MapLocation nearestCenter = new MapLocation(
                _Info.robotLoc.x + dx,
                _Info.robotLoc.y + dy
            );
            if (_Info.robotLoc.isWithinDistanceSquared(nearestCenter, 4)) {
                center = nearestCenter;
            }
        } else { // Found SRP
            if (rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
                center = null;
                score=0;
                return;
            } else {
                for (MapInfo tile : _Info.nearbyTiles) {
                    MapLocation tileLoc = tile.getMapLocation();

                    // If tile is a part of the SRP
                    if (tileLoc.x >= center.x - 2 && tileLoc.x <= center.x + 2 &&
                            tileLoc.y >= center.y - 2 && tileLoc.y <= center.y + 2) {

                        if (tile.hasRuin() || tile.isWall() ||
                                tile.getPaint() == PaintType.ENEMY_SECONDARY ||
                                tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                            center = null;
                            score = 0;
                            return;
                        }

                        if (tile.getPaint() == PaintType.ALLY_SECONDARY &&
                                !pattern[tileLoc.x - center.x + 2][tileLoc.y - center.y + 2]) {
                            if (_Info.nearbyRuins.length > 0) {
                                score = 0;
                                center = null;
                                return;
                            } else {
                                paintLoc = tileLoc;
                            }
                        }
                        if (tile.getPaint() == PaintType.EMPTY ||
                                (tile.getPaint() == PaintType.ALLY_PRIMARY &&
                                        pattern[tileLoc.x - center.x + 2][tileLoc.y - center.y + 2])) {
                            paintLoc = tileLoc;
                        }
                    }
                }

                if (paintLoc != null) {
                    score = Constants.CompleteSrpScore;
                    targetLoc = paintLoc;
                    return;
                }
            }
        }
        score = 0;
        center = null;
    }

    //If there is a valid pattern, move towards and fill in the pattern
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if(paintLoc != null) {
            PaintSrpGrid.fillInPattern(paintLoc);
            paintLoc = null;
        }
    }

    // Add helper functions here

}
