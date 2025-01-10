package important;

import battlecode.common.*;

public class FillPattern extends RobotPlayer {

    static int state = 0; //0 = wandering, 1 = filling rune, 2 = filling pattern

    //locates nearest ruin and attempts to build a pattern
    //run once every turn
    public static boolean play(RobotController rc, UnitType tower) throws GameActionException {

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
            if (rc.canMove(rc.getLocation().directionTo(ruin))) {
                rc.move(rc.getLocation().directionTo(ruin));
            }
        }
    }

    public static void fillInPattern(RobotController rc ,MapLocation loc) throws GameActionException {
        boolean[][] pattern = rc.getResourcePattern();

        if(rc.canSenseLocation(loc) && rc.canAttack(loc) && !rc.senseMapInfo(loc).hasRuin()) {
            boolean useSecondary = pattern[loc.x%5][loc.y%5];
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

        int[][] corners = {
                {0, 0},
                {0, 4},
                {4, 0},
                {4, 4}
        };

        boolean[][] pattern = rc.getResourcePattern();
        int closest = Integer.MAX_VALUE;

        for(MapInfo tile : nearbyTiles) {
            int x = tile.getMapLocation().x%5;
            int y = tile.getMapLocation().y%5;
            if(x== 4|| x == 0) {
                if(y == 4 || y == 0) {
                    boolean found = true;

                    for(int i = 0 ; i < 4 ; i ++) {
                        if(x != corners[i][0] || y != corners[i][1]) {
                            if(!rc.canSenseLocation(new MapLocation(tile.getMapLocation().x + (corners[i][0] - x), tile.getMapLocation().y + (corners[i][1] - y)))) {
                                found = false;
                                break;
                            }
                        }
                    }
                    if(found) {
                        rc.setIndicatorString(found+"");
                        MapLocation check = new MapLocation(tile.getMapLocation().x + (2 - x), tile.getMapLocation().y + (2 - y));
                        if(rc.getLocation().distanceSquaredTo(check) < closest) {
                            closest = rc.getLocation().distanceSquaredTo(check);
                            center = check;
                        }
                    }
                }
            }
        }

        MapLocation loc = null;
        if(center != null) {
            if(rc.canCompleteResourcePattern(center)) {
                rc.completeResourcePattern(center);
            } else {
                rc.setIndicatorString(center + "a");
                MapLocation paintLoc = null;
                for (int i = 5; --i >= 0; ) {
                    for (int j = 5; --j >= 0; ) {
                        loc = new MapLocation(center.x + i - 2, center.y + j - 2);
                        PaintType paint = rc.senseMapInfo(loc).getPaint();

                        if (rc.senseMapInfo(loc).hasRuin()) {
                            return null;
                        }
                        if (paint == PaintType.ALLY_SECONDARY) {
                            if (!pattern[i][j]) {
                                return null;
                            }
                        }
                        if (paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) {
                            return null;
                        }

                        if (paint == PaintType.EMPTY || (paint == PaintType.ALLY_PRIMARY && pattern[i][j])) {
                            paintLoc = loc;
                        }
                    }
                }
                if (rc.canMove(rc.getLocation().directionTo(center))) {
                    rc.move(rc.getLocation().directionTo(center));
                }
                rc.setIndicatorString(paintLoc + "c");
                return paintLoc;
            }
        } else {
            return null;
        }
        return null;
    }
}
