package ryan;

import battlecode.common.*;

public class FillRuin extends RobotPlayer {

    public static boolean updateNearestWrongInRuin(UnitType towerType) throws GameActionException {
        Soldiers.numWrongTilesInRuin = 0;
        MapLocation ruinLoc = curRuin.getMapLocation();
        boolean[][] towerPattern = rc.getTowerPattern(towerType);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;
                MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return false;
                }
                if (paint == PaintType.EMPTY
                || (paint == PaintType.ALLY_SECONDARY && !towerPattern[i][j])
                || (paint == PaintType.ALLY_PRIMARY && towerPattern[i][j])) {
                    Soldiers.numWrongTilesInRuin++;
                    if (nearestWrongInRuin == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInRuin)) {
                        nearestWrongInRuin = loc;
                    }
                }
            }
        }
        return true;
    }

    public static void tryToPaintRuin(UnitType towerType) throws GameActionException {
        MapLocation ruinLoc = curRuin.getMapLocation();

        if (nearestWrongInRuin == null) {
            rc.setIndicatorString("complete: " + towerType);
            if (rc.canCompleteTowerPattern(towerType, ruinLoc)) {
                rc.completeTowerPattern(towerType, ruinLoc);
                isFillingRuin = false;
            }
            return;
        }

        boolean[][] towerPattern = rc.getTowerPattern(towerType);
        int delta_x = ruinLoc.x - nearestWrongInRuin.x;
        int delta_y = ruinLoc.y - nearestWrongInRuin.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            // why does this happen?
            // System.out.println("ruin deltas are off. curRuin: " + ruinLoc + ", nearestWrongInRuin: " + nearestWrongInRuin);
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        if (rc.canAttack(nearestWrongInRuin)) {
            rc.attack(nearestWrongInRuin, useSecondary);
            nearestWrongInRuin = null;
        }

        rc.setIndicatorString("complete: " + towerType);
        if (rc.canCompleteTowerPattern(towerType, ruinLoc)) {
            rc.completeTowerPattern(towerType, ruinLoc);
            isFillingRuin = false;
        }
    }
}
