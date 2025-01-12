package e_action.utils;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;
import battlecode.common.RobotController;
import e_action.Robot;

public class PaintSrpGrid {

    public static RobotController rc = Robot.rc;

    // SOLDIER ONLY
    // Fills in the specified tile with a color such that the map gets tiled with resource patterns
    public static void fillInPattern(MapLocation loc) throws GameActionException {
        boolean[][] pattern = rc.getResourcePattern();

        if(rc.canSenseLocation(loc) && rc.canAttack(loc) && !rc.senseMapInfo(loc).hasRuin()) {
            boolean useSecondary = pattern[(loc.x+(loc.y/3))%4][4-(loc.y%3)];
            rc.setIndicatorString(loc+"+"+useSecondary);
            if(!((rc.senseMapInfo(loc).getPaint() == PaintType.ALLY_SECONDARY && useSecondary) || (rc.senseMapInfo(loc).getPaint() == PaintType.ALLY_PRIMARY && !useSecondary))) {
                rc.attack(loc, useSecondary);
                rc.setIndicatorString(loc+"e");
            }
        }
    }
}
