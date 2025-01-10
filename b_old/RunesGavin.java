package b_old;

import battlecode.common.*;

public class RunesGavin {

    static int state = 0;

    public static void findRuinAndBuildTower(RobotController rc, UnitType tower) throws GameActionException {
        rc.setIndicatorString("state " + state);

        MapLocation[] ruins = rc.senseNearbyRuins(20);

        boolean[][] moneyPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        boolean[][] paintPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        boolean[][] defensePattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);

        boolean[][] towerPattern = rc.getTowerPattern(tower);

        int distance = Integer.MAX_VALUE;
        MapLocation ruinLoc = null;
        for (MapLocation ruin : ruins) {
            if (!rc.isLocationOccupied(ruin)) {
                if (rc.getLocation().distanceSquaredTo(ruin) < distance) {
                    distance = rc.getLocation().distanceSquaredTo(ruin);
                    ruinLoc = ruin;
                }
            }
        }

        if (ruinLoc != null) {
            state = 2;

            boolean isMoney = true;
            boolean isPaint = true;
            boolean isDefense = true;
            boolean isTower = true;
            // System.out.println("-------");

            boolean hasPaint = false;

            for (int i = 5; --i >= 0;) {
                for (int j = 5; --j >= 0;) {
                    MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                    // System.out.println(loc);
                    if (rc.canSenseLocation(loc)) {
                        Boolean paint = null;
                        boolean blocked = false;
                        switch (rc.senseMapInfo(loc).getPaint()) {
                            case PaintType.ALLY_SECONDARY -> paint = true;
                            case PaintType.ENEMY_PRIMARY -> blocked = true;
                            case PaintType.ENEMY_SECONDARY -> blocked = true;
                        }

                        if (blocked) {
                            state = 0;
                            // System.out.println("blocked");
                            return;
                        }

                        if (paint != null) {
                            hasPaint = true;
                            if (paint == true && moneyPattern[i][j] == false) {
                                // rc.setIndicatorString(i+","+j);
                                isMoney = false;
                            }
                            if (paint == true && paintPattern[i][j] == false) {
                                // rc.setIndicatorString(i+","+j);
                                isPaint = false;
                            }
                            if (paint == true && defensePattern[i][j] == false) {
                                // rc.setIndicatorString(i+","+j);
                                isDefense = false;
                            }
                            if (paint == true && towerPattern[i][j] == false) {
                                isTower = false;
                            }
                        }
                    }
                }
            }
            if (!hasPaint) {
                drawRuin(rc, tower, ruinLoc);
            } else {
                if (isTower) {
                    drawRuin(rc, tower, ruinLoc);
                    rc.setIndicatorString("0");
                } else if (isMoney) {
                    drawRuin(rc, UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                    rc.setIndicatorString("1");
                } else if (isPaint) {
                    drawRuin(rc, UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                    rc.setIndicatorString("2");
                } else if (isDefense) {
                    drawRuin(rc, UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc);
                    rc.setIndicatorString("3");
                } else {
                    drawRuin(rc, tower, ruinLoc);
                    rc.setIndicatorString("4");
                }
            }

        } else {
            state = 0;
        }
    }

    public static void drawRuin(RobotController rc, UnitType tower, MapLocation ruin) throws GameActionException {
        boolean[][] towerPattern = rc.getTowerPattern(tower);
        boolean found = false;
        MapLocation paintLoc = null;
        boolean useSeconday = false;
        // rc.setIndicatorString("ran");
        int round = rc.getRoundNum();
        int ID = rc.getID();
        for (int i = 5; --i >= 0;) {
            for (int j = 5; --j >= 0;) {
                MapLocation loc = new MapLocation(ruin.x + i - 2, ruin.y + j - 2);
                if (rc.canSenseLocation(loc)) {
                    if (!rc.senseMapInfo(loc).hasRuin()) {
                        PaintType paint = rc.senseMapInfo(loc).getPaint();
                        // System.out.println("ran");
                        switch (paint) {
                            case PaintType.EMPTY:
                                found = true;
                                // System.out.println(found);
                                // rc.setIndicatorString(loc+"");
                                paintLoc = loc;
                                useSeconday = towerPattern[i][j];
                                break;
                            case PaintType.ALLY_PRIMARY:
                                if (towerPattern[i][j] == true) {
                                    found = true;
                                    // System.out.println(found);
                                    // rc.setIndicatorString(loc+"");
                                    paintLoc = loc;
                                    useSeconday = towerPattern[i][j];
                                }
                                break;
                            case PaintType.ALLY_SECONDARY:
                                if (towerPattern[i][j] == false) {
                                    found = true;
                                    // System.out.println(found);
                                    // rc.setIndicatorString(loc+"");
                                    paintLoc = loc;
                                    useSeconday = towerPattern[i][j];
                                }
                                break;
                        }

                        if (found == true) {
                            break;
                        }
                    }
                }
            }
            if (found == true) {
                break;
            }
        }

        if (found == true) {
            if (rc.canMove(rc.getLocation().directionTo(paintLoc))) {
                rc.move(rc.getLocation().directionTo(paintLoc));
            }
            if (rc.canAttack(paintLoc)) {
                rc.attack(paintLoc, useSeconday);
            }
        } else {
            if (rc.canCompleteTowerPattern(tower, ruin)) {
                rc.completeTowerPattern(tower, ruin);
                state = 0;
            }
            if (rc.canMove(rc.getLocation().directionTo(ruin))) {
                rc.move(rc.getLocation().directionTo(ruin));
            }
            if (rc.canCompleteTowerPattern(tower, ruin)) {
                rc.completeTowerPattern(tower, ruin);
                state = 0;
            }

        }
    }
}
