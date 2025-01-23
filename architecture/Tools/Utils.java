package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;
import battlecode.common.UnitType;

// these Utils are pure functions - no side-effects, they don't change variables or modify game state in any way

public class Utils extends RobotPlayer {

    public static UnitType getBuildType(MapLocation ruinLoc) throws GameActionException {
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



    public static int chessDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
    }

    static boolean isWithinBounds(MapLocation loc) {  // SRP/ Ruin bounds
        return !(loc.x - 2 < 0 || loc.y - 2 < 0 || loc.x + 2 >= mapWidth || loc.y + 2 >= mapHeight);
    }

    public static MapLocation mirror(MapLocation loc) {  // rotational
        return new MapLocation(mapWidth - loc.x - 1, mapHeight - loc.y - 1);
    }
    public static MapLocation verticalMirror(MapLocation loc) {
        return new MapLocation(loc.x, mapHeight - loc.y - 1);
    }
    public static MapLocation horizontalMirror(MapLocation loc) {
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