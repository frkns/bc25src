package heuristic_test;
import battlecode.common.*;

// these Utils are NOT pure functions (i.e. they modify state)

public class ImpureUtils extends RobotPlayer {
    static void paintFloor() throws GameActionException {
        MapLocation maybeNewLoc = rc.getLocation();
        if (rc.canPaint(maybeNewLoc) && rc.senseMapInfo(maybeNewLoc).getPaint() == PaintType.EMPTY) {
            rc.attack(maybeNewLoc);
        }
    }
}
