package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;
import e_action.knowledge._Info;

import battlecode.common.*;

public class CompleteTowerPattern_NEEDSUPDATE extends Action {
    public RobotController rc;

    //class attributes
    public boolean alwaysDraw = false;

    public MapLocation ruinLoc = null;
    public boolean isMoney = true;
    public boolean isPaint = true;
    public boolean isDefense = true;
    public boolean isTower = true;

    public boolean hasPaint = false;


    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);

    }

    public CompleteTowerPattern_NEEDSUPDATE() {
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

            boolean[][] moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
            boolean[][] paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
            boolean[][] defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);


            MapInfo[] NearbyTiles = rc.senseNearbyMapInfos(); //I could not find a variable for this in the Robot class. Please replace if there is one

            for(MapInfo tile : NearbyTiles) {
                MapLocation loc = tile.getMapLocation();
                int x = tile.getMapLocation().x;
                int y = tile.getMapLocation().y;

                //System.out.println(loc);
                if(x >= ruinLoc.x -2 && x <= ruinLoc.x + 2 && y >= ruinLoc.y -2 && y <= ruinLoc.y + 2 && !tile.hasRuin()) {
                    PaintType paint = rc.senseMapInfo(loc).getPaint();

                    if(paint!=null) {
                        hasPaint = true;
                    }

                    if(paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) {
                        score = 0;
                        return;
                    }
                    if(paint == PaintType.ALLY_SECONDARY && !moneyPattern[x-ruinLoc.x+2][y-ruinLoc.y+2]) {
                        isMoney = false;
                    }
                    if(paint == PaintType.ALLY_SECONDARY && !paintPattern[x-ruinLoc.x+2][y-ruinLoc.y+2]) {
                        isPaint = false;
                    }
                    if(paint == PaintType.ALLY_SECONDARY && !defensePattern[x-ruinLoc.x+2][y-ruinLoc.y+2]) {
                        isDefense = false;
                    }
                }
            }
            score = Constants.CompleteTowerPatternScore;
        }
    }

    // If a pattern can be drawn on a nearby ruin, draw a tile and move towards that tile
    // If the pattern is completed, move towards the ruin
    public void play() throws GameActionException {

        UnitType tower = selectTower();

        if(ruinLoc != null) {
            Debug.print(3, Debug.PLAY + name);
            if(alwaysDraw) {
                drawRuin(tower,ruinLoc);
            }

            if(!hasPaint) {
                drawRuin(tower,ruinLoc);
            } else {
                if (isTower) {
                    drawRuin(tower, ruinLoc);
                    //rc.setIndicatorString("0");
                } else if (isMoney) {
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
    }
    //helper function for drawing the tower pattern
    public void drawRuin(UnitType tower, MapLocation ruin) throws GameActionException {
        boolean[][] towerPattern = rc.getTowerPattern(tower);

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        if (rc.canCompleteTowerPattern(tower, ruin)) {
            rc.completeTowerPattern(tower, ruin);
            ruinLoc = null;
        }

        Boolean paint = null;
        MapLocation paintLoc = null;

        for (MapInfo tile : nearbyTiles) {
            int x = tile.getMapLocation().x;
            int y = tile.getMapLocation().y;

            if (x >= ruin.x - 2 && x <= ruin.x + 2 && y >= ruin.y - 2 && y <= ruin.y + 2 && !tile.hasRuin()) {
                if (tile.getPaint() == PaintType.EMPTY) {
                    paint = towerPattern[x - ruin.x + 2][y - ruin.y + 2];
                    paintLoc = tile.getMapLocation();
                    break;
                }
                if (tile.getPaint() == PaintType.ALLY_SECONDARY && !towerPattern[x - ruin.x + 2][y - ruin.y + 2]) {
                    paint = false;
                    paintLoc = tile.getMapLocation();
                    break;
                }
                if (tile.getPaint() == PaintType.ALLY_PRIMARY && towerPattern[x - ruin.x + 2][y - ruin.y + 2]) {
                    paint = true;
                    paintLoc = tile.getMapLocation();
                    break;
                }
            }
        }

        if (paintLoc != null) {
            if (rc.canAttack(paintLoc)) {
                rc.attack(paintLoc, paint);
            }
            if (rc.canMove(rc.getLocation().directionTo(paintLoc))) {
                rc.move(rc.getLocation().directionTo(paintLoc));
            }
        } else {
            if (rc.canMove(rc.getLocation().directionTo(ruin))) {
                rc.move(rc.getLocation().directionTo(ruin));
            }
        }
    }

    // Select tower type to build based on chips and map size
    public UnitType selectTower() {
        int mapArea = _Info.MAP_AREA;
        int chipsRate = _Info.chipsRate;
        int round = rc.getRoundNum();

        if(mapArea < 1000) {
            if(_Info.chipsRate < 60 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            }
        } else if (mapArea < 2000) {
            if(_Info.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            }
        } else if (mapArea < 3000) {
            if(_Info.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else if(round < 300){
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        } else {
            if(_Info.chipsRate < 160 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else if (round < 300) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        }
    }
}
