package remake;

import battlecode.common.*;
import remake.fast.*;

class UnitFuncs extends RobotPlayer {

    static RobotController rc;
    static UnitType bunnyType;

    enum towerType {
        PAINT_TOWER,
        MONEY_TOWER,
        DEFENSE_TOWER
    }

    static towerType getTowerType(UnitType unit) {
        return switch (unit) {
            case LEVEL_ONE_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_TWO_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_THREE_PAINT_TOWER -> towerType.PAINT_TOWER;
            case LEVEL_ONE_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_TWO_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_THREE_MONEY_TOWER -> towerType.MONEY_TOWER;
            case LEVEL_ONE_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            case LEVEL_TWO_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            case LEVEL_THREE_DEFENSE_TOWER -> towerType.DEFENSE_TOWER;
            default -> null;
        };
    }

    // Paint refill
    static MapLocation nearestPaintTower = null;
    static FastLocSet paintTowerLocs = new FastLocSet();
    static double lowPaintPercentage = 0.3; // TODO make a function of distance from nearest recorded paint tower assume
                                            // will lose 2 paint per tile
    // Exploration
    static MapLocation spawnTowerLocation; // location of the tower that spawned me
    static Direction spawnDirection;
    // Rune filling (SOLDIER ONLY)
    static int state = 0; // 0 = wandering, 1 = filling rune, 2 = filling pattern

    // locates nearest ruin and attempts to build a pattern
    // run once every turn
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
            Pathfinder.move(paintLoc, false);
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

    /** Determine initial explore direction */
    static void init(RobotController r) throws GameActionException {
        rc = r;
        bunnyType = rc.getType();
        for (RobotInfo robot : rc.senseNearbyRobots(2)) {
            if (robot.getType().isTowerType()) {
                spawnTowerLocation = robot.getLocation();
                spawnDirection = rc.getLocation().directionTo(spawnTowerLocation).opposite();
                if (getTowerType(robot.getType()) == towerType.PAINT_TOWER) {
                    paintTowerLocs.add(robot.getLocation());
                }
            }
        }
    }

    /** SOLDIER */
    static void runSoldier() throws GameActionException {
        // System.out.println("Running soldier");

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();


                findRuinAndBuildTower(rc, UnitType.LEVEL_ONE_MONEY_TOWER);
                explore(spawnDirection);
            }
        }

    }

    // ** MOPPER */
    static void runMopper() throws GameActionException {
        System.out.println("Running mopper");
    }

    // ** SPLASHER */
    static void runSplasher() throws GameActionException {
        System.out.println("Running splasher");
    }

    static void refillPaint(RobotInfo[] nearbyRobots, int myPaint) throws GameActionException {
        nearestPaintTower = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                if (getTowerType(robot.getType()) == towerType.PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }
        // Efficiently iterate through paintTowerLocs
        if (nearestPaintTower == null) {
            for (int i = 1; i < paintTowerLocs.keys.length(); i += 3) {
                int x = (int) paintTowerLocs.keys.charAt(i);
                int y = (int) paintTowerLocs.keys.charAt(i + 1);
                MapLocation towerLoc = new MapLocation(x, y);
                if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(towerLoc) < rc.getLocation()
                        .distanceSquaredTo(nearestPaintTower)) {
                    nearestPaintTower = towerLoc;
                }
            }
        }

        if (nearestPaintTower != null) {
            int amt = bunnyType.paintCapacity - myPaint;
            if (rc.canTransferPaint(nearestPaintTower, -1 * amt)) {
                rc.transferPaint(nearestPaintTower, -1 * amt);
            }
            Pathfinder.move(nearestPaintTower, false);
        }

    }

    static void explore(Direction spawnDirection) throws GameActionException {
        Direction dirToMove = spawnDirection;
        MapLocation target = rc.getLocation().translate(dirToMove.dx * WIDTH, dirToMove.dy * HEIGHT);
        Pathfinder.move(target, true);
    }

    static MapInfo findNearbyRuin(RobotController rc, MapInfo[] nearbyTiles) throws GameActionException {
        return null;
    }

    static UnitType chooseTowerType(RobotController rc, MapLocation targetLoc) throws GameActionException {
        return null;
    }

    static void createTowerPattern(RobotController rc, MapLocation targetLoc) throws GameActionException {
    }
}