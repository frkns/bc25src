package smallrewrite;

import battlecode.common.*;
import org.apache.lucene.index.Payload;

import java.awt.*;
import java.util.Map;

public class FillPattern extends RobotPlayer{

    static int state = 0; //0 = wandering, 1 = filling rune, 2 = filling pattern
    static MapLocation lastRuin = null;

    //locates nearest ruin and attempts to build a pattern
    //run once every turn
    //temporary: added the alwaysDrawRuin which forces the drawing of the specified ruin (temporary fix to the code drawing an undesired ruin)
    public static boolean play(RobotController rc, UnitType tower, boolean alwaysDrawRuin) throws GameActionException {

        //rc.setIndicatorString(state+"");

        MapLocation[] ruins = rc.senseNearbyRuins(20);

        boolean[][] moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        boolean[][] paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        boolean[][] defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);

        boolean[][] towerPattern = rc.getTowerPattern(tower);


        int distance = Integer.MAX_VALUE;
        MapLocation ruinLoc = null;
        for(MapLocation ruin : ruins) {
            if(!rc.isLocationOccupied(ruin)) {
                if(rc.getLocation().distanceSquaredTo(ruin) < distance) {
                    distance = rc.getLocation().distanceSquaredTo(ruin);
                    ruinLoc = ruin;
                }
            }
        }

        if(lastRuin != null && rc.getChips() >= 1000) {
            if(rc.canMove(rc.getLocation().directionTo(lastRuin))) {
                rc.move(rc.getLocation().directionTo(lastRuin));
            } else {
                return false;
            }
        }

        if(ruinLoc != null) {

            state = 2;

            boolean isMoney = true;
            boolean isPaint = true;
            boolean isDefense = true;
            boolean isTower = true;
            //System.out.println("-------");

            boolean hasPaint = false;

            for(int i=5 ; --i>=0;) {
                for(int j=5 ; --j>=0;) {
                    MapLocation loc = new MapLocation(ruinLoc.x+i-2, ruinLoc.y+j-2);
                    //System.out.println(loc);
                    if(rc.canSenseLocation(loc) && !rc.senseMapInfo(loc).hasRuin()) {
                        PaintType paint = rc.senseMapInfo(loc).getPaint();

                        if(paint!=null) {
                            hasPaint = true;
                        }

                        if(paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) {
                            return false; //skip building
                        }
                        if(paint == PaintType.ALLY_SECONDARY && !moneyPattern[i][j]) {
                            isMoney = false;
                        }
                        if(paint == PaintType.ALLY_SECONDARY && !paintPattern[i][j]) {
                            isPaint = false;
                        }
                        if(paint == PaintType.ALLY_SECONDARY && !defensePattern[i][j]) {
                            isDefense = false;
                        }
                        if(paint == PaintType.ALLY_SECONDARY && !towerPattern[i][j]) {
                            isTower = false;
                        }
                    }
                }
            }

            if(alwaysDrawRuin) {
                drawRuin(rc,tower,ruinLoc);
                return true;
            }

            if(!hasPaint) {
                drawRuin(rc,tower,ruinLoc);
                return true;
            } else {
                if(isTower) {
                    drawRuin(rc,tower,ruinLoc);
                    return true;
                    //rc.setIndicatorString("0");
                } else if(isMoney) {
                    drawRuin(rc,UnitType.LEVEL_ONE_MONEY_TOWER,ruinLoc);
                    return true;
                    //rc.setIndicatorString("1");
                } else if(isPaint) {
                    drawRuin(rc,UnitType.LEVEL_ONE_PAINT_TOWER,ruinLoc);
                    return true;
                    //rc.setIndicatorString("2");
                } else if(isDefense) {
                    drawRuin(rc,UnitType.LEVEL_ONE_DEFENSE_TOWER,ruinLoc);
                    return true;
                    //rc.setIndicatorString("3");
                } else {
                    drawRuin(rc,tower,ruinLoc);
                    return true;
                    //rc.setIndicatorString("4");
                }
            }

        } else {
            state = 0;
            return false;
        }
    }

    public static void drawRuin(RobotController rc, UnitType tower, MapLocation ruin) throws GameActionException {
        boolean[][] towerPattern = rc.getTowerPattern(tower);

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        if(rc.canCompleteTowerPattern(tower,ruin)) {
            rc.completeTowerPattern(tower,ruin);
            lastRuin = null;
        }

        Boolean paint = null;
        MapLocation paintLoc = null;

        for(MapInfo tile : nearbyTiles) {
            int x = tile.getMapLocation().x;
            int y = tile.getMapLocation().y;

            if(x >= ruin.x -2 && x <= ruin.x + 2 && y >= ruin.y -2 && y <= ruin.y + 2 && !tile.hasRuin()) {
                if(tile.getPaint() == PaintType.EMPTY) {
                    paint = towerPattern[x-ruin.x+2][y-ruin.y+2];
                    paintLoc = tile.getMapLocation();
                    break;
                }
                if(tile.getPaint() == PaintType.ALLY_SECONDARY && !towerPattern[x-ruin.x+2][y-ruin.y+2]) {
                    paint = false;
                    paintLoc = tile.getMapLocation();
                    break;
                }
                if(tile.getPaint() == PaintType.ALLY_PRIMARY && towerPattern[x-ruin.x+2][y-ruin.y+2]) {
                    paint = true;
                    paintLoc = tile.getMapLocation();
                    break;
                }
            }
        }

        if(paintLoc != null) {
            if(rc.canAttack(paintLoc)) {
                rc.attack(paintLoc, paint);
            }
            if(rc.canMove(rc.getLocation().directionTo(paintLoc))) {
                rc.move(rc.getLocation().directionTo(paintLoc));
            }
        } else {
            if(rc.getChips() >= 1000) {
                if (rc.canMove(rc.getLocation().directionTo(ruin))) {
                    rc.move(rc.getLocation().directionTo(ruin));
                }
                return;
            }
            lastRuin = ruin;
        }
    }


    public static void fillInPattern(RobotController rc ,MapLocation loc) throws GameActionException {
        boolean[][] pattern = rc.getResourcePattern();

        if(rc.canSenseLocation(loc) && rc.canAttack(loc) && !rc.senseMapInfo(loc).hasRuin()) {
            boolean useSecondary = pattern[(loc.x+(loc.y/3))%4][4-(loc.y%3)];
            rc.setIndicatorString(loc+"+"+useSecondary);
            if(!((rc.senseMapInfo(loc).getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || (rc.senseMapInfo(loc).getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                rc.attack(loc, useSecondary);
                rc.setIndicatorString(loc+"e");
            }
        }
    }


    public static MapLocation locatePattern(RobotController rc) throws GameActionException {
        MapLocation center = null;
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        boolean ruin = false;

        int[][] corners = {
                {-2, -2},
                {2, 2},
                {-2, 2},
                {2, -2}
        };

        boolean[][] pattern = rc.getResourcePattern();
        int closest = Integer.MAX_VALUE;

        for(MapInfo tile : nearbyTiles) {

            if(tile.hasRuin() && !rc.isLocationOccupied(tile.getMapLocation())) {
                ruin = true;
            }

            int x = tile.getMapLocation().x;
            int y = tile.getMapLocation().y;
            if(y%3 == 2 &&  (x+((y-1)/3))%4 == 2) {
                if(rc.getLocation().distanceSquaredTo(tile.getMapLocation()) < closest) {
                    boolean found = true;

                    for(int i = 0 ; i < 4 ; i ++) {
                        if(!rc.canSenseLocation(new MapLocation(tile.getMapLocation().x + (corners[i][0]), tile.getMapLocation().y + (corners[i][1])))) {
                            found = false;break;
                        }
                    }
                    if(found) {
                        closest = tile.getMapLocation().distanceSquaredTo(tile.getMapLocation());
                        center = new MapLocation(tile.getMapLocation().x, tile.getMapLocation().y);
                    }
                }
            }
        }

        if(center != null) {
            if(rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
            } else {
                MapLocation paintLoc = null;

                for(MapInfo tile : nearbyTiles) {
                    int x = tile.getMapLocation().x;
                    int y = tile.getMapLocation().y;

                    if(x >= center.x -2 && x <= center.x + 2 && y >= center.y -2 && y <= center.y + 2) {

                        if(tile.hasRuin()) {
                            return null;
                        }

                        if(tile.getPaint() == PaintType.ENEMY_SECONDARY || tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                            return null;
                        }
                        if(tile.getPaint() == PaintType.ALLY_SECONDARY && !pattern[x-center.x+2][y-center.y+2]) {
                            if(ruin) {
                                return null;
                            } else {
                                paintLoc = tile.getMapLocation();
                            }
                        }
                        if(tile.getPaint() == PaintType.EMPTY) {
                            paintLoc = tile.getMapLocation();
                        }
                        if(tile.getPaint() == PaintType.ALLY_PRIMARY && pattern[x-center.x+2][y-center.y+2]) {
                            paintLoc = tile.getMapLocation();
                        }
                    }
                }
                if(paintLoc != null) {
                    if(rc.canMove(rc.getLocation().directionTo(center))) {
                        rc.move(rc.getLocation().directionTo(center));
                    }
                }
                return paintLoc;
            }
        } else {
            return null;
        }
        return null;
    }
}
