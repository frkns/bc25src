package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.Pathfinder;
import architecture.Tools.Utils;
import battlecode.common.*;

public class ActionExplore extends RobotPlayer {
    static MapLocation target;
    static int lastTargetChangeRound;
    static int targetChangeWaitTime = 20;

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_EXPLORE:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        if(!rc.isMovementReady()){
            Debug.println("\tX - ACTION_EXPLORE       : Not movement ready");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        Debug.println("\t0 - ACTION_EXPLORE       : Playing!");

        // Check for help signal
        for(RobotInfo ally: rc.senseNearbyRobots(-1, rc.getTeam())){
            MapLocation loc = ally.getLocation();
            if(rc.senseMapInfo(loc).getMark() == PaintType.ALLY_SECONDARY){
                Debug.println("\t\tHelp signal found at " + loc);
                target = loc;
            }
        }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = new MapLocation(rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
            lastTargetChangeRound = rc.getRoundNum();
        }

        Pathfinder.move(target);
        rc.setIndicatorLine(rc.getLocation(), target, 196, 20, 236); // Pink
    }
}
