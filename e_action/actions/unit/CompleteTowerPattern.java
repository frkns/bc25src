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
    public boolean isMoney = false;
    public boolean isPaint = false;
    public boolean isDefense = false;
    public boolean isTower = false;

    public boolean hasPaint = false;

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
                if(rc.getLocation().distanceSquaredTo(ruin) < distance) {
                    distance = rc.getLocation().distanceSquaredTo(ruin);
                    ruinLoc = ruin;
                }
            }
        }
        if(ruinLoc != null) {

            isMoney = false;
            isPaint = false;
            isDefense = false;

            for(MapInfo tile : _Info.nearbyTiles) {
                if (tile.getMapLocation().x >= ruinLoc.x - 2 && tile.getMapLocation().x <= ruinLoc.x + 2 && tile.getMapLocation().y >= ruinLoc.y - 2 && tile.getMapLocation().y <= ruinLoc.y + 2 && !tile.hasRuin()) {
                    if(tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                    score = 0;
                    return;
                    }
                }
            }

            //defense checks
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+1, ruinLoc.y)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+1, ruinLoc.y)).getPaint() == PaintType.ALLY_SECONDARY) {
                isDefense = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-1, ruinLoc.y)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-1, ruinLoc.y)).getPaint() == PaintType.ALLY_SECONDARY) {
                isDefense = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x, ruinLoc.y+1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x, ruinLoc.y+1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isDefense = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x, ruinLoc.y-1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x, ruinLoc.y-1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isDefense = true;
            }

            //paint checks
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+2, ruinLoc.y+2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+2, ruinLoc.y+2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-2, ruinLoc.y-2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-2, ruinLoc.y-2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-2, ruinLoc.y+2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-2, ruinLoc.y+2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+2, ruinLoc.y-2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+2, ruinLoc.y-2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }

            //money checks
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-2, ruinLoc.y+1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-2, ruinLoc.y+1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isMoney = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-1, ruinLoc.y+2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-1, ruinLoc.y+2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isMoney = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+1, ruinLoc.y+2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+1, ruinLoc.y+2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isMoney = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+2, ruinLoc.y+1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+2, ruinLoc.y+1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isMoney = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+2, ruinLoc.y-1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+2, ruinLoc.y-1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x+1, ruinLoc.y-2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x+1, ruinLoc.y-2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-1, ruinLoc.y-2)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-1, ruinLoc.y-2)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }
            if(rc.canSenseLocation(new MapLocation(ruinLoc.x-2, ruinLoc.y-1)) && rc.senseMapInfo(new MapLocation(ruinLoc.x-2, ruinLoc.y-1)).getPaint() == PaintType.ALLY_SECONDARY) {
                isPaint = true;
            }

        }

        UnitType tower = selectTower();

        if(ruinLoc != null) {
            Debug.print(3, Debug.PLAY + name);
            if(alwaysDraw) {
                drawRuin(tower,ruinLoc);
            }
            if (isMoney) {
                drawRuin(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                //rc.setIndicatorString("1");
            } else if (isPaint) {
                drawRuin(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                //rc.setIndicatorString("2");
            } else if (isDefense) {
                drawRuin(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc);
                //rc.setIndicatorString("3");
            } else {
                drawRuin(tower, ruinLoc);
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
    public void drawRuin(UnitType tower, MapLocation ruin) throws GameActionException {
        PaintType[][] towerPaintPattern = getTowerPattern(tower);
        boolean[][] towerPattern = rc.getTowerPattern(tower);

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        if (rc.canCompleteTowerPattern(tower, ruin)) {
            rc.completeTowerPattern(tower, ruin);
            ruinLoc = null;
        }

        Boolean paint = null;
        MapLocation paintLoc = null;

        for (MapInfo tile : nearbyTiles) {

            if (tile.getMapLocation().x >= ruin.x - 2 && tile.getMapLocation().x <= ruin.x + 2 && tile.getMapLocation().y >= ruin.y - 2 && tile.getMapLocation().y <= ruin.y + 2 && !tile.hasRuin()) {
               //rc.setIndicatorString(towerPaintPattern[0][0]+"a");
                if (tile.getPaint() != towerPaintPattern[tile.getMapLocation().x - ruin.x + 2][tile.getMapLocation().y - ruin.y + 2]) {
                    paint = towerPattern[tile.getMapLocation().x - ruin.x + 2][tile.getMapLocation().y - ruin.y + 2];
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
}
