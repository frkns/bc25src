package ryan;
import battlecode.common.*;

// these Utils are pure functions - no side effects, they don't change variables or modify game state in any way

public class Utils extends RobotPlayer {

    static MapLocation chooseTowerTarget() {  // choose a target for robot to move towards/ attack
        if (rc.getID() % 2 == 1) {
            if (fstTowerTarget != null && !visFstTowerTarget) {
                return fstTowerTarget;
            } else
            if (sndTowerTarget != null && !visSndTowerTarget) {
                return sndTowerTarget;
            }
        } else {
            if (sndTowerTarget != null && !visSndTowerTarget) {
                return sndTowerTarget;
            } else
            if (fstTowerTarget != null && !visFstTowerTarget) {
                return fstTowerTarget;
            }
        }

        return null;
    }

    static boolean isSendingWave() {
        return (rc.getRoundNum() / 20) % 3 == 0;
    }
    static boolean isAttackingBase() {
        if (isSendingWave() && rc.getRoundNum() >= attackBasePhase)
            return true;

        return false;
        }

        // Determines the type of tower to build based on patterns and game state
        static UnitType getBuildType(MapLocation ruinLoc) throws GameActionException {
            // Counter variables to track pattern mismatches
            int numWrongInPaint = 0; // Counts tiles that don't match paint pattern
            int numWrongInMoney = 0; // Counts tiles that don't match money pattern 
            int numWrongInDefense = 0; // Counts tiles that don't match defense pattern

            // Check each tile in 5x5 area around ruin (excluding center)
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i == 2 && j == 2)
                        continue;
                    MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                    if (!rc.canSenseLocation(loc))
                        continue;
                    PaintType paint = rc.senseMapInfo(loc).getPaint();
                    
                    // If enemy paint found, abort building
                    if (paint.isEnemy()) {
                        return null;
                    }
                    if (paint == PaintType.EMPTY) {
                        continue;
                    }

                    // Compare existing paint against desired patterns
                    if (paint == PaintType.ALLY_SECONDARY) {
                        // Increment counters if secondary paint is in wrong spot
                        if (!paintPattern[i][j]) numWrongInPaint++;
                        if (!moneyPattern[i][j]) numWrongInMoney++;
                        if (!defensePattern[i][j]) numWrongInDefense++;
                    } else if (paint == PaintType.ALLY_PRIMARY) {
                        // Increment counters if primary paint is in wrong spot
                        if (paintPattern[i][j]) numWrongInPaint++;
                        if (moneyPattern[i][j]) numWrongInMoney++;
                        if (defensePattern[i][j]) numWrongInDefense++;
                    }
                }
            }

            // Early game build order logic
            if (rc.getNumberTowers() <= Soldiers.strictFollowBuildOrderNumTowers) {
                return AuxConstants.buildOrder[rc.getNumberTowers()];
            }

            // Late game: always build defense towers after certain round
            if (rc.getRoundNum() >= alwaysBuildDefenseTowerPhase)
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;

            // If patterns need similar amounts of fixing, follow build order
            if (Math.abs(numWrongInPaint - numWrongInMoney) < 3 && Math.abs(numWrongInMoney - numWrongInDefense) < 3) {
                return AuxConstants.buildOrder[rc.getNumberTowers()];
            }

            // Otherwise build tower type that fixes most pattern mismatches
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
        return nearestEmpty;
    }

    static int chessDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
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


    static MapLocation int2loc(int val) {
        if (val == 0) {
            return null;
        }
        val -= 1;
        return new MapLocation(val / 64, val % 64);
    }

    static int loc2int(MapLocation loc) {
        if (loc == null)
            return 0;
        return loc.x * 64 + loc.y + 1;
    }
}
