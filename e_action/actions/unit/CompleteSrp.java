package e_action.actions.unit;

import e_action.knowledge._Info;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;
import e_action.utils.fast.FastLocSet;

public class CompleteSrp extends Action {
    public RobotController rc;

    public FastLocSet illegalOrCompletedCenters = new FastLocSet();
    public MapLocation center = null;
    public MapLocation cursor; // Keeps track of the last painted tile and keeps shifting until it finds another tile it can paint
    public int cursorVerticalDirection; // Keeps track of the direction the cursor is moving in. 1 = NORTH, -1 = SOUTH
    public int cursorHorizontalDirection; // Keeps track of the direction the cursor is moving in. 1 = EAST, -1 = WEST
    public boolean[][] pattern;
    


    public CompleteSrp(){
        rc = Robot.rc;
        name = "COMPLETE SRP";
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        pattern = rc.getResourcePattern();
    }

    // The robot will only build if it can see all 4 corners of the SRP to verify there is no enemy paint blocking it
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        if (center == null){
            center = findUnvalidatedCenter();
        }
        if (center != null) {
            if (rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
                illegalOrCompletedCenters.add(center);
            } else if (_Info.robotLoc.isWithinDistanceSquared(center, 4)) { // Robot can see all 4 corners of potential SRP
                if (centerIsValid(center)){
                    spawnCursor();
                    targetLoc = cursor;
                    score = Constants.CompleteSrpScore;
                } else {
                    illegalOrCompletedCenters.add(center);
                    center = null;
                    score = 0;
                };
            } else{
                score = 0;
            }
        }
    }

    //If there is a valid pattern, move towards and fill in the pattern
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        while (cursor.isWithinDistanceSquared(center, 8)){
            boolean useSecondary = pattern[(cursor.x+(cursor.y/3))%4][4-(cursor.y%3)];
            if(!((rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || (rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                rc.attack(cursor, useSecondary);
                break;
            } else {moveCursor();}
        }
    }
    
    public void spawnCursor() throws GameActionException{
        int robotDist1 = _Info.robotLoc.distanceSquaredTo(new MapLocation(center.x - 2, center.y + 2));
        int robotDist2 = _Info.robotLoc.distanceSquaredTo(new MapLocation(center.x + 2, center.y - 2));
        int robotDist3 = _Info.robotLoc.distanceSquaredTo(new MapLocation(center.x + 2, center.y + 2));
        int robotDist4 = _Info.robotLoc.distanceSquaredTo(new MapLocation(center.x - 2, center.y - 2));

        // Find closest corner
        if (robotDist1 <= robotDist2 && robotDist1 <= robotDist3 && robotDist1 <= robotDist4) {
            cursor = new MapLocation(center.x - 2, center.y + 2); // Top left
            cursorVerticalDirection = -1;
            cursorHorizontalDirection = 1;
        } else if (robotDist2 <= robotDist1 && robotDist2 <= robotDist4) {
            cursor = new MapLocation(center.x + 2, center.y - 2);  // Bottom right
            cursorVerticalDirection = 1;
            cursorHorizontalDirection = -1;
        } else if (robotDist3 <= robotDist4) {
            cursor = new MapLocation(center.x + 2, center.y + 2);  // Top right
            cursorVerticalDirection = -1;
            cursorHorizontalDirection = -1;
        } else {
            cursor = new MapLocation(center.x - 2, center.y - 2); // Bottom left
            cursorVerticalDirection = 1;
            cursorHorizontalDirection = 1;
        }
    }
    /**
     * The cursor moves vertically within a band of 5 tiles,
     * then shifts horizontally and reverses vertical direction when reaching the band limits.
     * 
     * Pattern example:
     * ↓→↑→↓→↑
     */
    public void moveCursor() throws GameActionException {
        if (cursor == null) return;

        MapLocation nextLoc = cursor.translate(0, cursorVerticalDirection);
        // If the vertical shift would move the cursor out of bounds, shift horizontally instead and reverse the vertical direction
        if (!nextLoc.isWithinDistanceSquared(center, 8)) {
            cursor = cursor.translate(cursorHorizontalDirection, 0);
            cursorVerticalDirection *= -1;
        } else {
            cursor = nextLoc;
        }
    }

    // Returns the first unvalidated center it sees
    public MapLocation findUnvalidatedCenter() throws GameActionException{
        // Calculate the vertical offset needed to reach the next valid y-coordinate (y % 3 == 2)
        int dy = (2 - _Info.robotLoc.y % 3) % 3;
        // Calculate the shifted x-coordinate based on y position
        int x = _Info.robotLoc.x + ((_Info.robotLoc.y + dy) / 3);
        // Calculate the horizontal offset needed to reach the next valid x-coordinate ((x + y/3) % 4 == 2)
        int dx = (2 - x % 4) % 4;

        // Generate four potential center locations by applying offsets in all combinations
        MapLocation center1 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y + dy);
        MapLocation center2 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y - dy);
        MapLocation center3 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y + dy);
        MapLocation center4 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y - dy);

        // Calculate distances to each center, setting to MAX_VALUE if center is illegal or completed
        int d1 = illegalOrCompletedCenters.contains(center1) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center1);
        int d2 = illegalOrCompletedCenters.contains(center2) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center2);
        int d3 = illegalOrCompletedCenters.contains(center3) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center3);
        int d4 = illegalOrCompletedCenters.contains(center4) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center4);

        // Return the closest valid center, or null if none are valid
        if (d1 <= d2 && d1 <= d3 && d1 <= d4 && d1 != Integer.MAX_VALUE) return center1;
        if (d2 <= d3 && d2 <= d4 && d2 != Integer.MAX_VALUE) return center2;
        if (d3 <= d4 && d3 != Integer.MAX_VALUE) return center3;
        if (d4 != Integer.MAX_VALUE) return center4;
        return null;
    }

    public boolean centerIsValid(MapLocation center) throws GameActionException {
        for (MapInfo tile : _Info.nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            // If tile is a part of the SRP
            if (tileLoc.x >= center.x - 2 && tileLoc.x <= center.x + 2 &&
                    tileLoc.y >= center.y - 2 && tileLoc.y <= center.y + 2) {
                if (tile.hasRuin() || tile.isWall() ||
                        tile.getPaint() == PaintType.ENEMY_SECONDARY ||
                        tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                    return false;
                }
            }
        }
        return true;
    }


}
