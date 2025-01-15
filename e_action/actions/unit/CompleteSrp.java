package e_action.actions.unit;

import e_action.knowledge._Info;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;


// The robot will only build if it can see all 4 corners of the SRP to verify there is no enemy paint blocking it
// This function is dependent on the FindSrpCenter Interest to prevent it from wandering way from the srpCenter of the SRP being built
public class CompleteSrp extends Action {
    public RobotController rc;


    public MapInfo [] srpTiles = null;
    public MapLocation cursor = null; // Keeps track of the last painted tile and keeps shifting until it finds another tile it can paint
    public int cursorVerticalDirection; // Keeps track of the direction the cursor is moving in. 1 = NORTH, -1 = SOUTH

    public boolean[][] pattern;
    public boolean useSecondary;


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

        if (_Info.srpCenter != null) {
            if (rc.canCompleteResourcePattern(_Info.srpCenter)) {
                rc.completeResourcePattern(_Info.srpCenter);
                markInvalid();
            } else if (_Info.robotLoc.isWithinDistanceSquared(_Info.srpCenter, 4)) { // Robot can see all 4 corners of potential SRP
                if (srpTiles == null) {
                    srpTiles = rc.senseNearbyMapInfos(_Info.srpCenter, 8);
                }
                if (centerIsValid()){
                    if (cursor == null) {
                        spawnCursor();
                    }
                    while (cursor.isWithinDistanceSquared(_Info.srpCenter, 8)){
                        useSecondary = pattern[(cursor.x+(cursor.y/3))%4][4-(cursor.y%3)];
                        if(!((rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || (rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                            targetLoc = cursor;
                            score = Constants.CompleteSrpScore;
                            break;
                        }
                        moveCursor();
                    }
                    spawnCursor(); // Reached end of pattern
                } else {
                    markInvalid();
                };
            } else { // We have a target center but we are not close enough to verify if there is enemy paint, walls, or ruins blocking us
                score = 0;
            }
        }
    }

    //If there is a valid pattern, move towards and fill in the pattern
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        if (rc.canAttack(targetLoc)){
            rc.attack(targetLoc, useSecondary);
        }
    }
    
    public void spawnCursor() throws GameActionException{
        cursor = new MapLocation(_Info.srpCenter.x - 2, _Info.srpCenter.y - 2); // Bottom left
        cursorVerticalDirection = 1;
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
        if (!nextLoc.isWithinDistanceSquared(_Info.srpCenter, 8)) {
            cursor = cursor.translate(1, 0);
            cursorVerticalDirection *= -1;
        } else {
            cursor = nextLoc;
        }
    }

    public boolean centerIsValid() throws GameActionException {
        for (MapLocation ruin : _Info.nearbyRuins){
            if (_Info.srpCenter.isWithinDistanceSquared(ruin, 8)){
                rc.setIndicatorDot(_Info.srpCenter, 255, 0, 0);
                return false;
            }
        }
        if (_Info.srpCenter.x <= 1 || _Info.srpCenter.x >= rc.getMapWidth() - 2 ||
            _Info.srpCenter.y <= 1 || _Info.srpCenter.y >= rc.getMapHeight() - 2) {
            return false;
        }
        for (MapInfo tile : srpTiles) {
            if (tile.isWall() ||
                    tile.getPaint().isEnemy()) {
                return false;
            }
        }

        return true;
    }
    
    public void markInvalid() throws GameActionException {
        _Info.invalidSrpCenters.add(_Info.srpCenter);
        _Info.srpCenter = null;
        srpTiles = null;
        cursor = null;
        score = 0;
    }


}
