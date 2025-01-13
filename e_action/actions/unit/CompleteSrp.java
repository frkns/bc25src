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

        for (MapInfo tile : _Info.nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();

            if (tileLoc.y % 3 == 2 && (tileLoc.x + ((tileLoc.y - 1) / 3)) % 4 == 2) {
                int dist = rc.getLocation().distanceSquaredTo(tileLoc);
                if (dist < closest) {
                    boolean found = true;

                    // Unrolled version of the inner loop (corners)
                    MapLocation adjacentLoc0 = new MapLocation(tileLoc.x + corners[0][0], tileLoc.y + corners[0][1]);
                    MapLocation adjacentLoc1 = new MapLocation(tileLoc.x + corners[1][0], tileLoc.y + corners[1][1]);
                    MapLocation adjacentLoc2 = new MapLocation(tileLoc.x + corners[2][0], tileLoc.y + corners[2][1]);
                    MapLocation adjacentLoc3 = new MapLocation(tileLoc.x + corners[3][0], tileLoc.y + corners[3][1]);

                    if (!rc.canSenseLocation(adjacentLoc0) ||
                            !rc.canSenseLocation(adjacentLoc1) ||
                            !rc.canSenseLocation(adjacentLoc2) ||
                            !rc.canSenseLocation(adjacentLoc3)) {
                        found = false;
                    }

                    if (found) {
                        closest = dist;
                        center = tileLoc;
                    }
                }
            }
        }

        if (center != null) {
            if (rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
                score=0;
                return;
            } else {
                for (MapInfo tile : _Info.nearbyTiles) {
                    MapLocation tileLoc = tile.getMapLocation();

                    if (tileLoc.x >= center.x - 2 && tileLoc.x <= center.x + 2 &&
                            tileLoc.y >= center.y - 2 && tileLoc.y <= center.y + 2) {

                        if (tile.hasRuin() || tile.isWall() ||
                                tile.getPaint() == PaintType.ENEMY_SECONDARY ||
                                tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                            score = 0;
                            return;
                        }

                        if (tile.getPaint() == PaintType.ALLY_SECONDARY &&
                                !pattern[tileLoc.x - center.x + 2][tileLoc.y - center.y + 2]) {
                            if (_Info.nearbyRuins.length > 0) {
                                score = 0;
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
