package e_action.actions.unit;

import e_action.knowledge._Info;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;
import e_action.utils.fast.FastLocSet;

public class CompleteSrp extends Action {
    public RobotController rc;

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
            center = SRP.findUnvalidatedCenter();
        }
        if (center != null) {
            if (rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
                _Info.illegalOrCompletedCenters.add(center);
                center = null;
                score = 0;
                rc.setTimelineMarker("Completed SRP",0,225,0);
            } else if (_Info.robotLoc.isWithinDistanceSquared(center, 4)) { // Robot can see all 4 corners of potential SRP
                if (SRP.centerIsValid(center)){
                    spawnCursor();
                    targetLoc = cursor;
                    score = Constants.CompleteSrpScore;
                } else {
                    _Info.illegalOrCompletedCenters.add(center);
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
                if(rc.canAttack(cursor)) {
                    rc.attack(cursor, useSecondary);
                }
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
}
