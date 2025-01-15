package slim_v1;

import battlecode.common.*;

public class Moppers extends RobotPlayer{

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = Math.max(mapWidth, mapHeight);


    public static void run() throws GameActionException {

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            if (target != null) rc.setIndicatorDot(target, 0, 0, 0);
            target = new MapLocation(rng.nextInt(mapWidth-1), rng.nextInt(mapHeight-1));
            lastTargetChangeRound = rc.getRoundNum();
        }
        rc.setIndicatorDot(target, 200, 200, 200);

        HeuristicPath.targetIncentive = 500;
        HeuristicPath.move(target);
        nearbyTiles = rc.senseNearbyMapInfos();

    }

}
