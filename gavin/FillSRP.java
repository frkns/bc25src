package gavin;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;

public class FillSRP extends RobotPlayer {

    // returns null if there is enemy paint on it
    public static MapLocation pureNearestWrongInSRP(MapLocation SRPloc) throws GameActionException {
        MapLocation nearest = null;
        boolean[][] resourcePattern = rc.getResourcePattern();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                MapLocation loc = new MapLocation(SRPloc.x + i - 2, SRPloc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return null;
                }
                if (paint == PaintType.EMPTY
                || (paint == PaintType.ALLY_SECONDARY && !resourcePattern[i][j])
                || (paint == PaintType.ALLY_PRIMARY && resourcePattern[i][j])) {
                    if (nearest == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearest)) {
                        nearest = loc;
                    }
                }
            }
        }
        return nearest;
    }

    public static boolean updateNearestWrongInSRP() throws GameActionException {
        Soldiers.numWrongTilesInSRP = 0;
        MapLocation SRPloc = curSRP;
        boolean[][] resourcePattern = rc.getResourcePattern();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                MapLocation loc = new MapLocation(SRPloc.x + i - 2, SRPloc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return false;
                }
                if (paint == PaintType.EMPTY
                || (paint == PaintType.ALLY_SECONDARY && !resourcePattern[i][j])
                || (paint == PaintType.ALLY_PRIMARY && resourcePattern[i][j])) {
                    Soldiers.numWrongTilesInSRP++;
                    if (nearestWrongInSRP == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInSRP)) {
                        nearestWrongInSRP = loc;
                    }
                }
            }
        }
        return true;
    }

    public static void tryComplete() throws GameActionException {
        rc.setIndicatorString("complete: SRP @ " + curSRP);
        if (rc.canCompleteResourcePattern(curSRP)) {
            rc.completeResourcePattern(curSRP);
        }
        isFillingSRP = false;
        Soldiers.lastSRPloc = curSRP;
        Soldiers.lastSRProundNum = rc.getRoundNum();
    }

    public static void tryToPaintSRP() throws GameActionException {
        MapLocation SRPloc = curSRP;

        if (nearestWrongInSRP == null) {
            tryComplete();
            return;
        }

        boolean[][] resourcePattern = rc.getResourcePattern();
        int delta_x = curSRP.x - nearestWrongInSRP.x;
        int delta_y = curSRP.y - nearestWrongInSRP.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            // why does this happen??
            // System.out.println("SRP deltas are off. curSRP: " + curSRP + ", nearestWrongInSRP: " + nearestWrongInSRP);
            return;
        }
        boolean useSecondary = resourcePattern[mask_x][mask_y];

        if (rc.canAttack(nearestWrongInSRP)) {
            rc.attack(nearestWrongInSRP, useSecondary);
            nearestWrongInSRP = null;
        }

        tryComplete();
    }
}
