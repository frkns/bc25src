package architecture;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;
import battlecode.common.UnitType;

// these Utils are pure functions - no side-effects, they don't change variables or modify game state in any way

public class Utils extends RobotPlayer {

    static UnitType getBuildType(MapLocation ruinLoc) throws GameActionException {
        int numWrongInPaint = 0; // *relative counting* empty tiles are skipped
        int numWrongInMoney = 0;
        int numWrongInDefense = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;
                MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    // return null;
                    continue;
                }
                if (paint == PaintType.EMPTY) {
                    continue;
                }

                if (paint == PaintType.ALLY_SECONDARY) {
                    if (!paintPattern[i][j])
                        numWrongInPaint++;
                    if (!moneyPattern[i][j])
                        numWrongInMoney++;
                    if (!defensePattern[i][j])
                        numWrongInDefense++;
                } else
                if (paint == PaintType.ALLY_PRIMARY) {
                    if (paintPattern[i][j])
                        numWrongInPaint++;
                    if (moneyPattern[i][j])
                        numWrongInMoney++;
                    if (defensePattern[i][j])
                        numWrongInDefense++;
                }

                // if (paint == PaintType.EMPTY
                //         || (paint == PaintType.ALLY_SECONDARY && !paintPattern[i][j])
                //         || (paint == PaintType.ALLY_PRIMARY && paintPattern[i][j])) {
                //     numWrongInPaint++;
                // }
                // if (paint == PaintType.EMPTY
                //         || (paint == PaintType.ALLY_SECONDARY && !moneyPattern[i][j])
                //         || (paint == PaintType.ALLY_PRIMARY && moneyPattern[i][j])) {
                //     numWrongInMoney++;
                // }
                // if (paint == PaintType.EMPTY
                //         || (paint == PaintType.ALLY_SECONDARY && !defensePattern[i][j])
                //         || (paint == PaintType.ALLY_PRIMARY && defensePattern[i][j])) {
                //     numWrongInDefense++;
                // }

            }
        }

        // do not early return so we can return null if there is enemy paint
        if (rc.getNumberTowers() <= strictFollowBuildOrderNumTowers)
            return AuxConstants.buildOrder[rc.getNumberTowers()];  // follow the build order
        if (nearbyEnemyRobots > 0)
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        // if roughly same num of wrong tiles, follow the build order
        if (Math.abs(numWrongInPaint - numWrongInMoney) < 3 && Math.abs(numWrongInMoney - numWrongInDefense) < 3) {
            return AuxConstants.buildOrder[rc.getNumberTowers()];
        }
        if (numWrongInMoney <= numWrongInPaint && numWrongInMoney <= numWrongInDefense)
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        if (numWrongInPaint <= numWrongInDefense)
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        return UnitType.LEVEL_ONE_DEFENSE_TOWER;
    }

    // returns null if it's already dotted and there's no enemy paint on the ruin,
    // otherwise returns nearest empty location
    static MapLocation nearestEmptyOnRuinIfEnemyOrIsUndotted(MapLocation ruinLoc) throws GameActionException {
        // if (rc.canSenseRobotAtLocation(ruinLoc)) {
        // RobotInfo ruinLocInfo = rc.senseRobotAtLocation(ruinLoc);
        // if (ruinLocInfo.getTeam() == rc.getTeam())
        // return null; // this "ruin" is acutally an ally tower
        // }
        // ^ this code checks to see if ruinLoc has an ally tower on it (update: removed
        // because we want to dot it anyway)
        boolean hasEnemyPaint = false;
        boolean hasAllyPaint = false;
        MapLocation nearestEmpty = null;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;
                MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    hasEnemyPaint = true;
                }
                else if (paint.isAlly()) {
                    hasAllyPaint = true;
                }
                else  {
                    assert(paint == PaintType.EMPTY);
                    if (nearestEmpty == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestEmpty)) {
                        nearestEmpty = loc;
                    }
                }
            }
        }

        if (hasAllyPaint && !hasEnemyPaint)  // we have >= 1 paint there and there is no enemy paint
            return null;
        if (hasAllyPaint && hasEnemyPaint)  // remove this to full paint ruins with enemy paint on them (but it will be really bad on specific maps)
            return null;
        return nearestEmpty;
    }

    static int currentQuadrant() throws GameActionException {  // numbered like the cartesian plane, except 0-indexed
        MapLocation loc = rc.getLocation();
        if (loc.x > mapWidth/2) {
            // Q1 or Q4
            if (loc.y > mapHeight/2)
                return 0;
            return 3;
        }
        // Q2 or Q3
        if (loc.y > mapHeight/2)
            return 1;
        return 2;
    }
    static MapLocation randomLocationInQuadrant(int Q) {  // 0-indexed
        int offsetx = rng.nextInt(mapWidth/2) - mapWidth/4;
        int offsety = rng.nextInt(mapHeight/2) - mapHeight/4;

        if (offsetx > 0) offsetx -= 2;  // doing this because i don't want to do the math to get it exact
        else offsetx += 2;
        if (offsety > 0) offsety -= 2;
        else offsety += 2;

        return new MapLocation(quadrantCenters[Q].x + offsetx, quadrantCenters[Q].y + offsety);
    }

    // static int leastExploredQuadrant() {
    //     int q = 0;
    //     int xp = roundsSpentInQuadrant[q];
    //     if (roundsSpentInQuadrant[1] < xp) {
    //         q = 1;
    //         xp = roundsSpentInQuadrant[q];
    //     }
    //     if (roundsSpentInQuadrant[2] < xp) {
    //         q = 2;
    //         xp = roundsSpentInQuadrant[q];
    //     }
    //     if (roundsSpentInQuadrant[3] < xp) {
    //         q = 3;
    //         xp = roundsSpentInQuadrant[q];
    //     }
    //     return q;
    // }

    static int chessDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
    }

    static boolean isWithinBounds(MapLocation loc) {  // SRP/ Ruin bounds
        return !(loc.x - 2 < 0 || loc.y - 2 < 0 || loc.x + 2 >= mapWidth || loc.y + 2 >= mapHeight);
    }

    static MapLocation mirror(MapLocation loc) {  // rotational
        return new MapLocation(mapWidth - loc.x - 1, mapHeight - loc.y - 1);
    }
    static MapLocation verticalMirror(MapLocation loc) {
        return new MapLocation(loc.x, mapHeight - loc.y - 1);
    }
    static MapLocation horizontalMirror(MapLocation loc) {
        return new MapLocation(mapWidth - loc.x - 1, loc.y);
    }

    static boolean selfDestructRequirementsMet() {
        if (rc.getRoundNum() < selfDestructPhase)
            return false;
        if (nearbyFriendlyRobots < selfDestructFriendlyRobotsThreshold)
            return false;
        if (nearbyEnemyRobots > selfDestructEnemyRobotsThreshold)
            return false;
        if (rc.getPaint() > selfDestructPaintThreshold)
            return false;

        return true;
    }
}
