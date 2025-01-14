package slim_v1;
import battlecode.common.*;

// these Utils are NOT pure functions (i.e. they modify state)

public class ImpureUtils extends RobotPlayer {

    static void paintFloor() throws GameActionException {
        MapLocation floorTile = rc.getLocation();

        // canAttack is better than canPaint because it checks action cooldown?? (changing to this avoided an error)
        if (rc.canAttack(floorTile) && rc.senseMapInfo(floorTile).getPaint() == PaintType.EMPTY) {
            rc.attack(floorTile);
        }
    }
}
