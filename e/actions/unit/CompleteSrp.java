package e.actions.unit;

import battlecode.common.*;
import e.Robot;
import e.actions.Action;
import e.knowledge._Info;
import e.utils.Constants;
import e.utils.Debug;


// The robot will only build if it can see all 4 corners of the SRP to verify there is no enemy paint blocking it
// This function is dependent on the FindSrpCenter Interest to prevent it from wandering way from the srpCenter of the SRP being built
public class CompleteSrp extends Action {
    public RobotController rc;


    public MapInfo [] srpTiles = null;
    public MapLocation cursor = null; // Keeps track of the last painted tile and keeps shifting until it finds another tile it can paint
    public int cursorVerticalDirection; // Keeps track of the direction the cursor is moving in. 1 = NORTH, -1 = SOUTH

    public boolean[][] pattern;
    public boolean useSecondary;
    public boolean completed;
    public boolean marked;


    public CompleteSrp(){
        rc = Robot.rc;
        name = "CompleteSrp";
        debugAction = true;
    }

    public void initUnit(){
        Debug.print(1, Debug.INIT + name, debugAction);
        pattern = rc.getResourcePattern();
    }


    // The robot will only build if it can see all 4 corners of the SRP to verify there is no enemy paint blocking it
    public void calcScore() throws GameActionException {
        Debug.print(3, "[CompleteSrp] Starting calcScore", debugAction);

        if (_Info.srpCenter != null) {
            Debug.print(3, "[CompleteSrp] Found SRP center at: " + _Info.srpCenter, debugAction);
            
            if (rc.canCompleteResourcePattern(_Info.srpCenter)) {
                Debug.print(3, "[CompleteSrp] Completing resource pattern!", debugAction);
                rc.completeResourcePattern(_Info.srpCenter);
                if (!_Info.processedBlockerTiles.contains(_Info.srpCenter)) _Info.blockerTiles.add(_Info.srpCenter);
                markInvalid();
            } else if (_Info.robotLoc.isWithinDistanceSquared(_Info.srpCenter, 4)) {
                Debug.print(3, "[CompleteSrp] Within range to validate pattern", debugAction);
                
                if (srpTiles == null) {
                    srpTiles = rc.senseNearbyMapInfos(_Info.srpCenter, 8);
                }
                if (validatePotentialCenters()){
                    Debug.print(3, "[CompleteSrp] Pattern validated, checking cursor", debugAction);
                    if (marked == false && rc.canMark(_Info.srpCenter)) {
                        rc.mark(_Info.srpCenter, true);
                        marked = true;
                        }
                    if (cursor == null) {
                        spawnCursor();
                    }
                    while (cursor.isWithinDistanceSquared(_Info.srpCenter, 8)){
                        int relX = cursor.x - _Info.srpCenter.x + 2;
                        int relY = cursor.y - _Info.srpCenter.y + 2;
                        useSecondary = pattern[relX][relY];
                        MapInfo cursorMapInfo = rc.senseMapInfo(cursor);
                        if (!((cursorMapInfo.getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || 
                            (cursorMapInfo.getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                            Debug.print(3, "[CompleteSrp] Found unpainted tile at: " + cursor, debugAction);
                            targetLoc = cursor;
                            score = Constants.CompleteSrpScore;
                            completed = false;
                            break;
                        }
                        moveCursor();
                    }
                    spawnCursor(); // If we have reached the end of pattern, we reset the cursor at the beginning
                } else {
                    Debug.print(3, "[CompleteSrp] Pattern validation failed", debugAction);
                    markInvalid();
                }
            }
        }
    }

    //If there is a valid pattern, move towards and fill in the pattern
    public void play() throws GameActionException {
        Debug.print(3, "[CompleteSrp] Starting play action", debugAction);

        if (rc.canAttack(targetLoc)){
            Debug.print(3, "[CompleteSrp] Painting at: " + targetLoc + " secondary=" + useSecondary, debugAction);
            rc.attack(targetLoc, useSecondary);
        }
    }
    
    public void spawnCursor() throws GameActionException{
        cursor = new MapLocation(_Info.srpCenter.x - 2, _Info.srpCenter.y - 2); // Bottom left
        cursorVerticalDirection = 1;
        completed = true; // This gets set to false the moment we must paint a tile
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

    public boolean validatePotentialCenters() throws GameActionException {
        Debug.print(3, "[CompleteSrp] Validating center at: " + _Info.srpCenter, debugAction);
        
        if (_Info.srpCenter.x <= 1 || _Info.srpCenter.x >= rc.getMapWidth() - 2 ||
            _Info.srpCenter.y <= 1 || _Info.srpCenter.y >= rc.getMapHeight() - 2) {
            Debug.print(3, "[CompleteSrp] Center too close to map edge", debugAction);
            return false;
        }
        for (MapLocation ruin : _Info.nearbyRuins){
            if (!_Info.processedBlockerTiles.contains(ruin)) _Info.blockerTiles.add(ruin);
            if (_Info.srpCenter.isWithinDistanceSquared(ruin, 8)){
                Debug.print(3, "[CompleteSrp] Found blocking ruin at: " + ruin, debugAction);
                return false;
            }
        }
        for (MapInfo tile : srpTiles) {
            if (tile.isWall() ||
                    tile.getPaint().isEnemy()) {
                Debug.print(3, "[CompleteSrp] Found blocking wall or enemy paint at: " + tile.getMapLocation(), debugAction);
                if (!_Info.processedBlockerTiles.contains(tile.getMapLocation())) _Info.blockerTiles.add(tile.getMapLocation());
                return false;
            }
        }
        Debug.print(3, "[CompleteSrp] Pattern validation successful", debugAction);
        return true;
    }
    
    public void markInvalid() throws GameActionException {
        if (_Info.srpCenter == null) return;

        Debug.print(3, "[CompleteSrp] Marking center invalid: " + _Info.srpCenter, debugAction);
        _Info.invalidSrpCenters.add(_Info.srpCenter);
        _Info.srpCenter = null;
        srpTiles = null;
        cursor = null;
        score = 0;
    }
}
