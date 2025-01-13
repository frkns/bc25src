package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;
import e_action.knowledge._Info;

import battlecode.common.*;

import java.awt.*;

public class CompleteTowerPattern extends Action {
    public RobotController rc;

    //class attributes
    public boolean alwaysDraw = false;

    public MapLocation ruinLoc = null;

    public Boolean useSecondary = null;
    public MapLocation paintLocation = null;

    public PaintType[][] moneyPattern = new PaintType[5][5];
    public PaintType[][] paintPattern = new PaintType[5][5];
    public PaintType[][] defensePattern = new PaintType[5][5];

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

    // Detects if towers can be constructed on nearby ruins (i.e. is there enemy paint?)
    public void calcScore() throws GameActionException {

        ruinLoc = null;
        int distance = Integer.MAX_VALUE;

        Debug.print(3, Debug.CALCSCORE + name);
        for(MapLocation ruin : _Info.nearbyRuins) {
            if(!rc.isLocationOccupied(ruin)) {
                int dist = rc.getLocation().distanceSquaredTo(ruin);
                if(dist < distance) {
                    distance = dist;
                    ruinLoc = ruin;
                }
            }
        }
        if(ruinLoc != null) {
            for(MapInfo tile : _Info.nearbyTiles) {
                MapLocation tileLoc = tile.getMapLocation();

                if (isWithinRange(tileLoc,ruinLoc) && !tile.hasRuin()) {
                    if(tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                    score = 0;
                    return;
                    }
                }
            }

            UnitType tower = selectTower();

            PaintType[][] towerPaintPattern = getTowerPattern(tower);
            boolean[][] towerPattern = rc.getTowerPattern(tower);

            MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

            if (rc.canCompleteTowerPattern(tower, ruinLoc)) {
                rc.completeTowerPattern(tower, ruinLoc);
                ruinLoc = null;
                score = 0;
                return;
            }

            Boolean paint = null;
            MapLocation paintLoc = null;

            for (MapInfo tile : nearbyTiles) {

                if (isWithinRange(tile.getMapLocation(),ruinLoc)) {
                    //rc.setIndicatorString(towerPaintPattern[0][0]+"a");
                    if (tile.getPaint() != towerPaintPattern[tile.getMapLocation().x - ruinLoc.x + 2][tile.getMapLocation().y - ruinLoc.y + 2]) {
                        paint = towerPattern[tile.getMapLocation().x - ruinLoc.x + 2][tile.getMapLocation().y - ruinLoc.y + 2];
                        paintLoc = tile.getMapLocation();
                        break;
                    }
                }
            }

            if (paintLoc != null) {
                paintLocation = paintLoc;
                score = Constants.CompleteTowerPatternScore;
                useSecondary = paint;
                targetLoc = paintLoc;
            } else {
                score = Constants.CompleteTowerPatternScore;
                targetLoc = ruinLoc;
            }
        }

    }

    // If a pattern can be drawn on a nearby ruin, draw a tile and move towards that tile
    // If the pattern is completed, move towards the ruin
    public void play() throws GameActionException {
        if(paintLocation != null) {
            if(rc.canAttack(paintLocation)) {
                rc.attack(paintLocation,useSecondary);
                paintLocation = null;
            }
        }
    }
    //helper function for drawing the tower pattern


    // Select tower type to build based on chips and map size
    public UnitType selectTower() {
        int mapArea = _Info.MAP_AREA;
        int chipsRate = _Info.chipsRate;
        int round = rc.getRoundNum();

        if(mapArea < 1000) {
            if(_Info.chipsRate < 60 ) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            }
        } else if (mapArea < 2000) {
            if(_Info.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            }
        } else if (mapArea < 3000) {
            if(_Info.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else if(round < 300){
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        } else {
            if(_Info.chipsRate < 160 ) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else if (round < 300) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        }
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

    public boolean isWithinRange(MapLocation tileLoc, MapLocation ruinLoc) {
        return tileLoc.x >= ruinLoc.x - 2 && tileLoc.x <= ruinLoc.x + 2 &&
                tileLoc.y >= ruinLoc.y - 2 && tileLoc.y <= ruinLoc.y + 2;
    }
}
