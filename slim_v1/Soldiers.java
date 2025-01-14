package slim_v1;

import battlecode.common.*;

public class Soldiers extends RobotPlayer {

    static MapLocation target;
    static int targetChangeWaitTime = 20;  // how long of not being able to reach target till we change it?

    static int lastTargetChangeRound = 9999;
    public static void run() throws GameActionException {
        // assumptions
        assert(200 == rc.getType().paintCapacity);

        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget == null) paintTarget = spawnTowerLocation;

        if (rc.getLocation().isWithinDistanceSquared(paintTarget, 2)) {
            RobotInfo paintTower = rc.senseRobotAtLocation(paintTarget);
            int paintTowerPaintAmt = paintTower.getPaintAmount();
            int transferAmt = Math.min(paintTowerPaintAmt, 200 - rc.getPaint());
            if (rc.canTransferPaint(paintTarget, -1 * transferAmt))
                rc.transferPaint(paintTarget, -1 * transferAmt);
        }

        if (rc.getPaint() < 100) {
            HeurisitcPath.targetIncentive = 1500;
            HeurisitcPath.move(paintTarget);

            ImpureUtils.paintFloor();
        }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            if (target != null) rc.setIndicatorDot(target, 0, 255, 0);
            target = new MapLocation(rng.nextInt(mapWidth-1), rng.nextInt(mapHeight-1));
            lastTargetChangeRound = rc.getRoundNum();
        }


        rc.setIndicatorDot(target, 255, 0, 0);

        if (rc.isMovementReady()) {
            HeurisitcPath.targetIncentive = 500;
            HeurisitcPath.move(target);
        }

        ImpureUtils.paintFloor();
    }
}
