package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class ActionGivePaint extends RobotPlayer {
    public static void run() throws GameActionException {
        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        if (rc.getPaint() < 50) {
            Debug.println("\tX - ACTION_GIVE_PAINT    : not enough paint");
            return;
        }

        MapLocation target = null;

        for (RobotInfo ally : rc.senseNearbyRobots(8, rc.getTeam())) {
            if (ally.getType().isTowerType()) {
                continue;
            }

            if (ally.getPaintAmount() < 20) {
                // Direct acces
                if (ally.getLocation().isWithinDistanceSquared(rc.getLocation(), 2)) {
                    target = ally.getLocation();
                    break;
                }

                // After moving on time
                if (rc.canMove(rc.getLocation().directionTo(ally.getLocation()))) {
                    target = ally.getLocation();
                    break;
                }
            }
        }

        if (target == null) {
            Debug.println("\tX - ACTION_GIVE_PAINT    : no nearby Ally in need");
            return;
        }


        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_GIVE_PAINT    : Playing!");

        if (rc.canTransferPaint(target, 40)) {
            rc.transferPaint(target, 40);
            return;
        }

        rc.move(rc.getLocation().directionTo(target));
        rc.transferPaint(target, 40);
    }
}
