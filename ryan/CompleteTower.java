package ryan;

import battlecode.common.*;

public class CompleteTower extends RobotPlayer {


    public static MapLocation getNearestWrongInRuin(UnitType towerType, MapLocation ruinLoc) throws GameActionException {
        Soldiers.numWrongTilesInRuin = 0;
        boolean[][] towerPattern = rc.getTowerPattern(towerType);
        MapLocation nearestWrongInRuin = null;

        for (int i = 4; i >= 0; i--) {
            for (int j = 4; j >= 0; j--) {
                if (i == 2 && j == 2)
                    continue;
                MapLocation loc = new MapLocation(ruinLoc.x + i - 2, ruinLoc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return new MapLocation(-1, -1);
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
        return nearestWrongInRuin;
    }

    public static void paintTower(UnitType towerType, MapLocation ruinLoc, MapLocation nearestWrongInRuin) throws GameActionException {
        boolean[][] towerPattern = rc.getTowerPattern(towerType);
        int delta_x = ruinLoc.x - nearestWrongInRuin.x;
        int delta_y = ruinLoc.y - nearestWrongInRuin.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            Debug.println("Invalid mask_x or mask_y");
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        if (rc.canAttack(nearestWrongInRuin)) {
            rc.attack(nearestWrongInRuin, useSecondary);
        }
    }
}
