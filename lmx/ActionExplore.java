package lmx;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ActionExplore extends RobotPlayer {
    static MapLocation target;
    static int lastTargetChangeRound;
    static int targetChangeWaitTime = 20;

    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_EXPLORE:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        Debug.println("\t0 - ACTION_EXPLORE       : Playing!");


        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // selecting a random target location on the map has an inherent bias towards the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our current quadrant
            /*if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() < stopQuadrantModifierPhase)
                target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else*/
            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            lastTargetChangeRound = rc.getRoundNum();
        }

        HeuristicPath.move(target, Heuristic.EXPLORE);
    }
}
