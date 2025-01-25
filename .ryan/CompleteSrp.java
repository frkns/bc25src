package ryan;

import battlecode.common.*;

public class CompleteSrp extends RobotPlayer {

    public static MapLocation getNearestWrongInSrp(MapLocation Srploc) throws GameActionException {
        Soldiers.numWrongTilesInSrp = 0;
        boolean[][] resourcePattern = rc.getResourcePattern();
        MapLocation nearestWrongInSrp = null;
        for (int i = 4; i >= 0; i--) {
            for (int j = 4; j >= 0; j--) {
                MapLocation loc = new MapLocation(Srploc.x + i - 2, Srploc.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return (new MapLocation(-1, -1)); // Indicates enemy paint, while null means there are simply no wrong tiles in range.
                }
                if (paint == PaintType.EMPTY
                || (paint == PaintType.ALLY_SECONDARY && !resourcePattern[i][j])
                || (paint == PaintType.ALLY_PRIMARY && resourcePattern[i][j])) {
                    Soldiers.numWrongTilesInSrp++;
                    if (nearestWrongInSrp == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInSrp)) {
                        nearestWrongInSrp = loc;
                    }
                }
            }
        }
        return nearestWrongInSrp;
    }

    public static void paintSrp(MapLocation SrpLoc, MapLocation nearestWrongInSrp) throws GameActionException {
        boolean[][] resourcePattern = rc.getResourcePattern();
        int delta_x = SrpLoc.x - nearestWrongInSrp.x;
        int delta_y = SrpLoc.y - nearestWrongInSrp.y;
        int mask_x = 2 - delta_x; 
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            Debug.println("Srp deltas are off. SrpLoc: " + SrpLoc + ", getNearestWrongInSrp: " + getNearestWrongInSrp(SrpLoc));
            return;
        }
        boolean useSecondary = resourcePattern[mask_x][mask_y];

        if (rc.canAttack(nearestWrongInSrp)) {
            rc.attack(nearestWrongInSrp, useSecondary);
        }
    }
}
