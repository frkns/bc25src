package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;
import e_action.knowledge._Info;

import battlecode.common.*;

public class CompleteTowerPattern extends Action {

    public RobotController rc;


    public MapInfo [] towerTiles = null;
    public MapLocation cursor = null; // Keeps track of the last painted tile and keeps shifting until it finds another tile it can paint
    public int cursorVerticalDirection; // Keeps track of the direction the cursor is moving in. 1 = NORTH, -1 = SOUTH

    
    public UnitType currentTower;
    public boolean[][] currentPattern = null;
    public boolean[][] moneyPattern;
    public boolean[][] paintPattern; 
    public boolean[][] defensePattern;
    public boolean useSecondary;

    //uses the getTowerPattern methods to generate a grid of PaintTypes for each pattern
    public void initUnit() throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);
    }

    public CompleteTowerPattern() {
        rc = Robot.rc;
        name = "COMPLETE TOWER PATTERN";
        debugAction = false;
        Debug.print(3, Debug.INIT + name);
    }

    // Detects if towers can be constructed on nearby ruins
    public void calcScore() throws GameActionException {
        if (_Info.towerCenter != null && rc.getChips() > 1000) {
            if (currentTower != null && rc.canCompleteTowerPattern(currentTower, _Info.towerCenter)) {
                rc.completeTowerPattern(currentTower, _Info.towerCenter);
                markInvalid();
            } else if (_Info.robotLoc.isWithinDistanceSquared(_Info.towerCenter, 4)) {
                if (towerTiles == null) {
                    towerTiles = rc.senseNearbyMapInfos(_Info.towerCenter, 8);
                }
                if (_Info.towerCenter == null) {
                    _Info.towerCenter = _Info.towerCenter;
                }
                if (centerIsValid()) {
                    if (currentPattern == null) {
                        currentTower = selectTower();
                        currentPattern = getTowerPattern(currentTower);
                    }
                    if (cursor == null) {
                        spawnCursor();
                    }
                    while (cursor.isWithinDistanceSquared(_Info.towerCenter, 8)) {
                        int relX = cursor.x - _Info.towerCenter.x + 2;
                        int relY = cursor.y - _Info.towerCenter.y + 2;
                        useSecondary = currentPattern[relX][relY];
                        MapInfo cursorMapInfo = rc.senseMapInfo(cursor);
                        if (!((cursorMapInfo.getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || 
                            (cursorMapInfo.getPaint() == PaintType.ALLY_PRIMARY && !useSecondary)) && !cursor.equals(_Info.towerCenter)) {
                            targetLoc = cursor;
                            score = Constants.CompleteTowerPatternScore;
                            break;
                        }
                        moveCursor();
                    }
                    spawnCursor();
                } else {
                    markInvalid();
                }
            } else {
                score = 0;
            }
        }
    }


    // If a pattern tile needs to be painted, move towards it and paint
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name);
        if (rc.canAttack(targetLoc)) {
            rc.attack(targetLoc, useSecondary);
        }
    }

    public boolean centerIsValid() throws GameActionException {
        for (MapInfo tile : towerTiles) {
            if (tile.getPaint().isEnemy()) {
                return false;
            }
        }
        return true;
    }

    public void markInvalid() throws GameActionException {
        _Info.invalidTowerCenters.add(_Info.towerCenter);
        _Info.towerCenter = null;
        currentPattern = null;
        towerTiles = null;
        cursor = null;
        score = 0;
    }

    public void spawnCursor() throws GameActionException{
        cursor = new MapLocation(_Info.towerCenter.x - 2, _Info.towerCenter.y - 2); // Bottom left
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
        if (!nextLoc.isWithinDistanceSquared(_Info.towerCenter, 8)) {
            cursor = cursor.translate(1, 0);
            cursorVerticalDirection *= -1;
        } else {
            cursor = nextLoc;
        }
    }

    // Select tower type to build based on chips and map size
    public UnitType selectTower() {
        int mapArea = _Info.MAP_AREA;
        int round = rc.getRoundNum();

        if (_Info.chipsRate < 150){
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        } else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
        // } else {
        //     return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        // }
        // if(mapArea < 1000) {
        //     if(_Info.chipsRate < 60 ) {
        //         return UnitType.LEVEL_ONE_MONEY_TOWER;
        //     } else {
        //         return UnitType.LEVEL_ONE_PAINT_TOWER;
        //     }
        // } else if (mapArea < 2000) {
        //     if(_Info.chipsRate < 100 ) {
        //         return UnitType.LEVEL_ONE_MONEY_TOWER;
        //     } else {
        //         return UnitType.LEVEL_ONE_PAINT_TOWER;
        //     }
        // } else if (mapArea < 3000) {
        //     if(_Info.chipsRate < 100 ) {
        //         return UnitType.LEVEL_ONE_MONEY_TOWER;
        //     } else if(round < 300){
        //         return UnitType.LEVEL_ONE_PAINT_TOWER;
        //     } else {
        //         return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        //     }
        // } else {
        //     if(_Info.chipsRate < 160 ) {
        //         return UnitType.LEVEL_ONE_MONEY_TOWER;
        //     } else if (round < 300) {
        //         return UnitType.LEVEL_ONE_PAINT_TOWER;
        //     } else {
        //         return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        //     }
        // }
    }

    public boolean[][] getTowerPattern(UnitType tower) {
        if(tower == UnitType.LEVEL_ONE_MONEY_TOWER) {
            return moneyPattern;
        } else if (tower == UnitType.LEVEL_ONE_PAINT_TOWER) {
            return paintPattern;
        } else {
            return defensePattern;
        }
    }

}
