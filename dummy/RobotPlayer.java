package dummy;

import battlecode.common.*;

public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            // if (rc.canBuildRobot(UnitType.SOLDIER, rc.adjacentLocation(Direction.SOUTH))) {
            //     rc.buildRobot(UnitType.SOLDIER, rc.adjacentLocation(Direction.SOUTH));
            // }
            // if (!rc.getType().isTowerType() && rc.getRoundNum() == 10) {
            //     MapInfo[] n = rc.senseNearbyMapInfos();
            //     for (MapInfo m : n) {
            //         if (rc.canMark(m.getMapLocation()))
            //             rc.mark(m.getMapLocation(), false);
            //     }
            // }
            Clock.yield();
        }
    }
}
