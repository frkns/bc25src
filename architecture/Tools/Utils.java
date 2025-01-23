package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.*;

// these Utils are pure functions - no side-effects, they don't change variables or modify game state in any way

public class Utils extends RobotPlayer {
    public static UnitType[] TOWERS = {UnitType.LEVEL_ONE_PAINT_TOWER, UnitType.LEVEL_ONE_DEFENSE_TOWER, UnitType.LEVEL_ONE_PAINT_TOWER};

    public static UnitType getBuildType(MapLocation ruinLoc) throws GameActionException {
        int minCost = 1000;
        UnitType bestType = null;

        // Update nearby units
        boolean isMopperNearby = false;
        boolean isSoldierNearby = false;
        for(RobotInfo ally: rc.senseNearbyRobots(ruinLoc, 36, rc.getTeam())){
            switch (ally.type){
                case UnitType.SOLDIER: isSoldierNearby = true; break;
                case UnitType.MOPPER: isMopperNearby = true; break;
            }
        }


        for(UnitType tower: TOWERS){
            PatternReport repport = CheckPattern.analyseTowerPatern(ruinLoc, tower);

            int cost = repport.numWrongTiles;

            if(repport.nearestWrongEnemie != null && !isMopperNearby){
                cost += 5;
            }

            if(tower == AuxConstants.buildOrder[rc.getNumberTowers()]){
                cost -= 5;
            }

            if(cost < minCost){
                minCost = cost;
                bestType = tower;
            }
        }

        return bestType;
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