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
    public Boolean[][] currentPattern = null;
    public Boolean[][] moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
    public Boolean[][] paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
    public Boolean[][] defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);
    public boolean useSecondary;

    //uses the getTowerPattern methods to generate a grid of PaintTypes for each pattern
    public void initUnit() throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                if(rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER)[i][j]) {
                    moneyPattern[i][j] = PaintType.ALLY_SECONDARY;
                } else {
                    moneyPattern[i][j] = PaintType.ALLY_PRIMARY;
                }
                if(rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)[i][j]) {
                    paintPattern[i][j] = PaintType.ALLY_SECONDARY;
                } else {
                    paintPattern[i][j] = PaintType.ALLY_PRIMARY;
                }
                if(rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER)[i][j]) {
                    defensePattern[i][j] = PaintType.ALLY_SECONDARY;
                } else {
                    defensePattern[i][j] = PaintType.ALLY_PRIMARY;
                }
            }
        }
    }

    public CompleteTowerPattern() {
        rc = Robot.rc;
        name = "COMPLETE TOWER PATTERN";
        debugAction = false;
        Debug.print(3, Debug.INIT + name);
    }

    // Detects if towers can be constructed on nearby ruins
    public void calcScore() throws GameActionException {
        if (_Info.towerCenter != null) {
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
                        System.out.println("relX: " + relX + " relY: " + relY);
                        System.out.println("cursor: " + cursor);
                        System.out.println("towerCenter: " + _Info.towerCenter);
                        useSecondary = currentPattern[relX][relY] == PaintType.ALLY_SECONDARY;
                        if (!((rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || 
                            System.out.println("paint: " + rc.senseMapInfo(cursor).getPaint());
                            (rc.senseMapInfo(cursor).getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                            targetLoc = cursor;
                            score = Constants.CompleteTowerPatternScore;
                            break;
                        }
                        moveCursor();
                    }
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
        if (rc.canPaint(targetLoc)) {
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

    public PaintType[][] getTowerPattern(UnitType tower) {
        if(tower == UnitType.LEVEL_ONE_MONEY_TOWER) {
            return moneyPattern;
        } else if (tower == UnitType.LEVEL_ONE_PAINT_TOWER) {
            return paintPattern;
        } else {
            return defensePattern;
        }
    }
}
